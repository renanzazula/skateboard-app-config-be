package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.in.RemoveAppLogoUseCase;
import com.skateboard.appconfig.application.port.out.LoadAppConfigPort;
import com.skateboard.appconfig.application.port.out.ObjectStoragePort;
import com.skateboard.appconfig.application.port.out.SaveAppConfigPort;
import com.skateboard.appconfig.domain.model.AppConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RemoveAppLogoServiceTest {

    @Mock
    private LoadAppConfigPort loadAppConfigPort;

    @Mock
    private SaveAppConfigPort saveAppConfigPort;

    @Mock
    private ObjectStoragePort objectStoragePort;

    private RemoveAppLogoService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new RemoveAppLogoService(loadAppConfigPort, saveAppConfigPort, objectStoragePort);
        when(saveAppConfigPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void removesTheLogoAndDeletesTheObjectWhenOneExists() {
        AppConfig config = AppConfig.createDefaults();
        config.updateAppLogo("logo/app-logo.png");
        when(loadAppConfigPort.getOrCreate()).thenReturn(config);

        AppConfig result = service.execute(new RemoveAppLogoUseCase.Command("admin-1"));

        assertThat(result.getAppLogoKey()).isNull();
        assertThat(result.getUpdatedBy()).isEqualTo("admin-1");
        verify(objectStoragePort).delete("logo/app-logo.png");
    }

    @Test
    void skipsDeletionWhenThereIsNoExistingLogo() {
        when(loadAppConfigPort.getOrCreate()).thenReturn(AppConfig.createDefaults());

        service.execute(new RemoveAppLogoUseCase.Command("admin-1"));

        verify(objectStoragePort, never()).delete(any());
    }
}
