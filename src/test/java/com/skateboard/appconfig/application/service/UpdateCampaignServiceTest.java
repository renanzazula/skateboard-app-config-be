package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.in.UpdateCampaignUseCase;
import com.skateboard.appconfig.application.port.out.CampaignRepositoryPort;
import com.skateboard.appconfig.domain.exception.CampaignInvalidStateTransitionException;
import com.skateboard.appconfig.domain.exception.CampaignNotFoundException;
import com.skateboard.appconfig.domain.model.Campaign;
import com.skateboard.appconfig.domain.model.CampaignAudience;
import com.skateboard.appconfig.domain.model.CampaignFrequencyType;
import com.skateboard.appconfig.domain.model.CampaignScreenDraft;
import com.skateboard.appconfig.domain.model.CampaignStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UpdateCampaignServiceTest {

    private static final Instant START = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant END = Instant.parse("2026-02-01T00:00:00Z");

    @Mock
    private CampaignRepositoryPort campaignRepositoryPort;

    private UpdateCampaignService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new UpdateCampaignService(campaignRepositoryPort);
        when(campaignRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private static Campaign campaign(CampaignFrequencyType frequency, Integer maxPerDay) {
        return Campaign.create(UUID.randomUUID(), "before", "old notes", START, END, 1, CampaignAudience.ALL,
                frequency, maxPerDay, "admin-1");
    }

    private static UpdateCampaignUseCase.Command command(UUID campaignId, String name, Instant startAt,
                                                         Instant endAt, CampaignFrequencyType frequency,
                                                         Integer maxPerDay) {
        return new UpdateCampaignUseCase.Command("admin-2", campaignId, name, "new notes", startAt, endAt, 9,
                CampaignAudience.AUTHENTICATED, frequency, maxPerDay);
    }

    @Test
    void rejectsAnUnknownCampaign() {
        UUID id = UUID.randomUUID();
        when(campaignRepositoryPort.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(command(id, "n", START, END, CampaignFrequencyType.ALWAYS, null)))
                .isInstanceOf(CampaignNotFoundException.class);
    }

    @Test
    void appliesTheNewConfigurationAndStampsTheEditingAdmin() {
        Campaign existing = campaign(CampaignFrequencyType.ALWAYS, null);
        when(campaignRepositoryPort.findById(existing.getId())).thenReturn(Optional.of(existing));

        Campaign updated = service.execute(command(existing.getId(), "after",
                Instant.parse("2026-03-01T00:00:00Z"), Instant.parse("2026-04-01T00:00:00Z"),
                CampaignFrequencyType.MAX_PER_DAY, 4));

        assertThat(updated.getName()).isEqualTo("after");
        assertThat(updated.getDescription()).isEqualTo("new notes");
        assertThat(updated.getPriority()).isEqualTo(9);
        assertThat(updated.getAudience()).isEqualTo(CampaignAudience.AUTHENTICATED);
        assertThat(updated.getStartAt()).isEqualTo(Instant.parse("2026-03-01T00:00:00Z"));
        assertThat(updated.getMaxDisplaysPerDay()).isEqualTo(4);
        assertThat(updated.getUpdatedBy()).isEqualTo("admin-2");
        verify(campaignRepositoryPort).save(existing);
    }

    @Test
    void switchingAwayFromMaxPerDayClearsTheStaleDailyCap() {
        Campaign existing = campaign(CampaignFrequencyType.MAX_PER_DAY, 5);
        when(campaignRepositoryPort.findById(existing.getId())).thenReturn(Optional.of(existing));

        Campaign updated = service.execute(command(existing.getId(), "n", START, END,
                CampaignFrequencyType.ONCE_PER_DAY, 5));

        assertThat(updated.getFrequencyType()).isEqualTo(CampaignFrequencyType.ONCE_PER_DAY);
        assertThat(updated.getMaxDisplaysPerDay()).isNull();
    }

    @Test
    void anEndBeforeStartIsRejectedAndNothingIsSaved() {
        Campaign existing = campaign(CampaignFrequencyType.ALWAYS, null);
        when(campaignRepositoryPort.findById(existing.getId())).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.execute(command(existing.getId(), "n", END, START,
                CampaignFrequencyType.ALWAYS, null)))
                .isInstanceOf(IllegalArgumentException.class);

        verify(campaignRepositoryPort, never()).save(any());
    }

    @Test
    void aPublishedCampaignCanStillBeEditedAndKeepsItsPublishedStatus() {
        Campaign existing = campaign(CampaignFrequencyType.ALWAYS, null);
        existing.addScreen(new CampaignScreenDraft(3, null, "#101010", null, null, null, null, null, null, null,
                false, null, null, null, null), "admin-1");
        existing.publish("admin-1");
        when(campaignRepositoryPort.findById(existing.getId())).thenReturn(Optional.of(existing));

        Campaign updated = service.execute(command(existing.getId(), "live edit", START, END,
                CampaignFrequencyType.ALWAYS, null));

        assertThat(updated.getStatus()).isEqualTo(CampaignStatus.PUBLISHED);
        assertThat(updated.getName()).isEqualTo("live edit");
    }

    @Test
    void anArchivedCampaignIsFrozenAndNothingIsSaved() {
        Campaign existing = campaign(CampaignFrequencyType.ALWAYS, null);
        existing.archive("admin-1");
        when(campaignRepositoryPort.findById(existing.getId())).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.execute(command(existing.getId(), "n", START, END,
                CampaignFrequencyType.ALWAYS, null)))
                .isInstanceOf(CampaignInvalidStateTransitionException.class);

        assertThat(existing.getName()).isEqualTo("before");
        verify(campaignRepositoryPort, never()).save(any());
    }
}
