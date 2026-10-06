package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.in.UpdateGuestApplicationConfigUseCase;
import com.skateboard.appconfig.application.port.out.LoadGuestApplicationConfigPort;
import com.skateboard.appconfig.application.port.out.SaveGuestApplicationConfigPort;
import com.skateboard.appconfig.domain.model.GuestApplicationConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UpdateGuestApplicationConfigServiceTest {

    @Mock
    private LoadGuestApplicationConfigPort loadGuestApplicationConfigPort;

    @Mock
    private SaveGuestApplicationConfigPort saveGuestApplicationConfigPort;

    private UpdateGuestApplicationConfigService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new UpdateGuestApplicationConfigService(loadGuestApplicationConfigPort, saveGuestApplicationConfigPort);
    }

    @Test
    void enablingWithARecipientPersistsTheUpdate() {
        GuestApplicationConfig config = GuestApplicationConfig.createDefaults();
        UUID recipient = UUID.randomUUID();
        when(loadGuestApplicationConfigPort.getOrCreate()).thenReturn(config);
        when(saveGuestApplicationConfigPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        GuestApplicationConfig updated = service.execute(new UpdateGuestApplicationConfigUseCase.Command(
                "admin-1", true, Set.of(recipient), "Subject", "Body"));

        assertThat(updated.isEnabled()).isTrue();
        assertThat(updated.getRecipientIds()).containsExactly(recipient);
        assertThat(updated.getUpdatedBy()).isEqualTo("admin-1");
    }

    @Test
    void enablingWithNoRecipientsIsRejectedAndNotSaved() {
        GuestApplicationConfig config = GuestApplicationConfig.createDefaults();
        when(loadGuestApplicationConfigPort.getOrCreate()).thenReturn(config);

        UpdateGuestApplicationConfigUseCase.Command command = new UpdateGuestApplicationConfigUseCase.Command(
                "admin-1", true, Set.of(), "Subject", "Body");

        assertThatThrownBy(() -> service.execute(command)).isInstanceOf(IllegalArgumentException.class);
        verify(saveGuestApplicationConfigPort, never()).save(any());
    }
}
