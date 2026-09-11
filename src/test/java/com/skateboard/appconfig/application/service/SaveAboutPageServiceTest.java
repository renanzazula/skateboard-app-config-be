package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.in.SaveAboutPageUseCase;
import com.skateboard.appconfig.application.port.out.LoadAboutPagePort;
import com.skateboard.appconfig.application.port.out.SaveAboutPagePort;
import com.skateboard.appconfig.domain.model.AboutPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SaveAboutPageServiceTest {

    @Mock
    private LoadAboutPagePort loadAboutPagePort;

    @Mock
    private SaveAboutPagePort saveAboutPagePort;

    private SaveAboutPageService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new SaveAboutPageService(loadAboutPagePort, saveAboutPagePort);
        when(saveAboutPagePort.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void createsThePageOnFirstSave() {
        when(loadAboutPagePort.find()).thenReturn(Optional.empty());

        AboutPage saved = service.execute(new SaveAboutPageUseCase.Command(
                "admin-1", "About Us", "Our story", AboutPage.Status.PUBLISHED,
                List.of(Map.of("type", "text", "data", Map.of("html", "hi")))));

        assertThat(saved.getTitle()).isEqualTo("About Us");
        assertThat(saved.getSubtitle()).isEqualTo("Our story");
        assertThat(saved.isPublished()).isTrue();
        assertThat(saved.getBlocks()).hasSize(1);
        assertThat(saved.getUpdatedBy()).isEqualTo("admin-1");
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void updatesTheExistingPageInPlace() {
        AboutPage existing = AboutPage.createEmpty();
        when(loadAboutPagePort.find()).thenReturn(Optional.of(existing));

        AboutPage saved = service.execute(new SaveAboutPageUseCase.Command(
                "admin-2", "New title", null, AboutPage.Status.DRAFT, List.of()));

        assertThat(saved.getId()).isEqualTo(existing.getId());
        assertThat(saved.getTitle()).isEqualTo("New title");
        assertThat(saved.getSubtitle()).isNull();
        assertThat(saved.isPublished()).isFalse();
    }

    @Test
    void rejectsABlankTitle() {
        when(loadAboutPagePort.find()).thenReturn(Optional.empty());

        SaveAboutPageUseCase.Command command = new SaveAboutPageUseCase.Command(
                "admin-1", "   ", null, AboutPage.Status.DRAFT, List.of());
        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(IllegalArgumentException.class);
        verify(saveAboutPagePort, never()).save(any());
    }
}
