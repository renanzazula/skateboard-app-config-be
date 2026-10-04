package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.in.SaveTermsUseCase;
import com.skateboard.appconfig.application.port.out.LoadTermsPort;
import com.skateboard.appconfig.application.port.out.SaveTermsPort;
import com.skateboard.appconfig.domain.model.Terms;
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

class SaveTermsServiceTest {

    @Mock
    private LoadTermsPort loadTermsPort;

    @Mock
    private SaveTermsPort saveTermsPort;

    private SaveTermsService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new SaveTermsService(loadTermsPort, saveTermsPort);
        when(saveTermsPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void createsThePageOnFirstSave() {
        when(loadTermsPort.find()).thenReturn(Optional.empty());

        Terms saved = service.execute(new SaveTermsUseCase.Command(
                "admin-1", "Terms & Conditions", "By using this app...", Terms.Status.PUBLISHED));

        assertThat(saved.getTitle()).isEqualTo("Terms & Conditions");
        assertThat(saved.getBody()).isEqualTo("By using this app...");
        assertThat(saved.isPublished()).isTrue();
        assertThat(saved.getUpdatedBy()).isEqualTo("admin-1");
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void updatesTheExistingPageInPlace() {
        Terms existing = Terms.createEmpty();
        when(loadTermsPort.find()).thenReturn(Optional.of(existing));

        Terms saved = service.execute(new SaveTermsUseCase.Command(
                "admin-2", "New title", "New body", Terms.Status.DRAFT));

        assertThat(saved.getId()).isEqualTo(existing.getId());
        assertThat(saved.getTitle()).isEqualTo("New title");
        assertThat(saved.getBody()).isEqualTo("New body");
        assertThat(saved.isPublished()).isFalse();
    }

    @Test
    void rejectsABlankTitle() {
        when(loadTermsPort.find()).thenReturn(Optional.empty());

        SaveTermsUseCase.Command command = new SaveTermsUseCase.Command(
                "admin-1", "   ", "body", Terms.Status.DRAFT);
        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(IllegalArgumentException.class);
        verify(saveTermsPort, never()).save(any());
    }

    @Test
    void rejectsABlankBody() {
        when(loadTermsPort.find()).thenReturn(Optional.empty());

        SaveTermsUseCase.Command command = new SaveTermsUseCase.Command(
                "admin-1", "Terms & Conditions", "   ", Terms.Status.DRAFT);
        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(IllegalArgumentException.class);
        verify(saveTermsPort, never()).save(any());
    }
}
