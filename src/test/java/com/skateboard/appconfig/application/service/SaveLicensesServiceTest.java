package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.in.SaveLicensesUseCase;
import com.skateboard.appconfig.application.port.out.LoadLicensesPort;
import com.skateboard.appconfig.application.port.out.SaveLicensesPort;
import com.skateboard.appconfig.domain.model.Licenses;
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

class SaveLicensesServiceTest {

    @Mock
    private LoadLicensesPort loadLicensesPort;

    @Mock
    private SaveLicensesPort saveLicensesPort;

    private SaveLicensesService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new SaveLicensesService(loadLicensesPort, saveLicensesPort);
        when(saveLicensesPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void createsThePageOnFirstSave() {
        when(loadLicensesPort.find()).thenReturn(Optional.empty());

        Licenses saved = service.execute(new SaveLicensesUseCase.Command(
                "admin-1", "Open-source Licenses", "This app uses...", Licenses.Status.PUBLISHED));

        assertThat(saved.getTitle()).isEqualTo("Open-source Licenses");
        assertThat(saved.getBody()).isEqualTo("This app uses...");
        assertThat(saved.isPublished()).isTrue();
        assertThat(saved.getUpdatedBy()).isEqualTo("admin-1");
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void updatesTheExistingPageInPlace() {
        Licenses existing = Licenses.createEmpty();
        when(loadLicensesPort.find()).thenReturn(Optional.of(existing));

        Licenses saved = service.execute(new SaveLicensesUseCase.Command(
                "admin-2", "New title", "New body", Licenses.Status.DRAFT));

        assertThat(saved.getId()).isEqualTo(existing.getId());
        assertThat(saved.getTitle()).isEqualTo("New title");
        assertThat(saved.getBody()).isEqualTo("New body");
        assertThat(saved.isPublished()).isFalse();
    }

    @Test
    void rejectsABlankTitle() {
        when(loadLicensesPort.find()).thenReturn(Optional.empty());

        SaveLicensesUseCase.Command command = new SaveLicensesUseCase.Command(
                "admin-1", "   ", "body", Licenses.Status.DRAFT);
        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(IllegalArgumentException.class);
        verify(saveLicensesPort, never()).save(any());
    }

    @Test
    void rejectsABlankBody() {
        when(loadLicensesPort.find()).thenReturn(Optional.empty());

        SaveLicensesUseCase.Command command = new SaveLicensesUseCase.Command(
                "admin-1", "Open-source Licenses", "   ", Licenses.Status.DRAFT);
        assertThatThrownBy(() -> service.execute(command))
                .isInstanceOf(IllegalArgumentException.class);
        verify(saveLicensesPort, never()).save(any());
    }
}
