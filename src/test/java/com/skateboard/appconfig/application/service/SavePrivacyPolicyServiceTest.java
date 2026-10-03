package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.in.SavePrivacyPolicyUseCase;
import com.skateboard.appconfig.application.port.out.LoadPrivacyPolicyPort;
import com.skateboard.appconfig.application.port.out.SavePrivacyPolicyPort;
import com.skateboard.appconfig.domain.model.PrivacyPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SavePrivacyPolicyServiceTest {

    @Mock
    private LoadPrivacyPolicyPort loadPrivacyPolicyPort;

    @Mock
    private SavePrivacyPolicyPort savePrivacyPolicyPort;

    private SavePrivacyPolicyService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new SavePrivacyPolicyService(loadPrivacyPolicyPort, savePrivacyPolicyPort);
        when(savePrivacyPolicyPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void createsThePageOnFirstSave() {
        when(loadPrivacyPolicyPort.find()).thenReturn(Optional.empty());

        PrivacyPolicy saved = service.execute(new SavePrivacyPolicyUseCase.Command(
                "admin-1", "Privacy Policy", "We collect...", PrivacyPolicy.Status.PUBLISHED));

        assertThat(saved.getTitle()).isEqualTo("Privacy Policy");
        assertThat(saved.getBody()).isEqualTo("We collect...");
        assertThat(saved.isPublished()).isTrue();
        assertThat(saved.getUpdatedBy()).isEqualTo("admin-1");
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void updatesTheExistingPageInPlace() {
        PrivacyPolicy existing = PrivacyPolicy.createEmpty();
        when(loadPrivacyPolicyPort.find()).thenReturn(Optional.of(existing));

        PrivacyPolicy saved = service.execute(new SavePrivacyPolicyUseCase.Command(
                "admin-2", "New title", "New body", PrivacyPolicy.Status.DRAFT));

        assertThat(saved.getId()).isEqualTo(existing.getId());
        assertThat(saved.getTitle()).isEqualTo("New title");
        assertThat(saved.getBody()).isEqualTo("New body");
        assertThat(saved.isPublished()).isFalse();
    }

    @Test
    void rejectsABlankTitle() {
        when(loadPrivacyPolicyPort.find()).thenReturn(Optional.empty());

        SavePrivacyPolicyUseCase.Command command = new SavePrivacyPolicyUseCase.Command(
                "admin-1", "   ", "body", PrivacyPolicy.Status.DRAFT);
        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(IllegalArgumentException.class);
        verify(savePrivacyPolicyPort, never()).save(any());
    }

    @Test
    void rejectsABlankBody() {
        when(loadPrivacyPolicyPort.find()).thenReturn(Optional.empty());

        SavePrivacyPolicyUseCase.Command command = new SavePrivacyPolicyUseCase.Command(
                "admin-1", "Privacy Policy", "   ", PrivacyPolicy.Status.DRAFT);
        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(IllegalArgumentException.class);
        verify(savePrivacyPolicyPort, never()).save(any());
    }
}
