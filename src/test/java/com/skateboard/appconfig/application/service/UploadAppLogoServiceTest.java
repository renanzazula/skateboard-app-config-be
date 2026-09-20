package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.in.UploadAppLogoUseCase;
import com.skateboard.appconfig.application.port.out.LoadAppConfigPort;
import com.skateboard.appconfig.application.port.out.ObjectStoragePort;
import com.skateboard.appconfig.application.port.out.SaveAppConfigPort;
import com.skateboard.appconfig.domain.model.AppConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UploadAppLogoServiceTest {

    @Mock
    private LoadAppConfigPort loadAppConfigPort;

    @Mock
    private SaveAppConfigPort saveAppConfigPort;

    @Mock
    private ObjectStoragePort objectStoragePort;

    private UploadAppLogoService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new UploadAppLogoService(loadAppConfigPort, saveAppConfigPort, objectStoragePort);
        when(saveAppConfigPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void rejectsAnUnsupportedMimeType() {
        when(loadAppConfigPort.getOrCreate()).thenReturn(AppConfig.createDefaults());

        assertThatThrownBy(() -> service.execute(
                new UploadAppLogoUseCase.Command("admin-1", new byte[]{1}, "image/gif")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void uploadsAndUpdatesTheLogoKeyWithNoPreviousLogo() {
        when(loadAppConfigPort.getOrCreate()).thenReturn(AppConfig.createDefaults());

        AppConfig result = service.execute(new UploadAppLogoUseCase.Command("admin-1", new byte[]{1, 2}, "image/png"));

        assertThat(result.getAppLogoKey()).isEqualTo("logo/app-logo.png");
        assertThat(result.getUpdatedBy()).isEqualTo("admin-1");
        verify(objectStoragePort).put("logo/app-logo.png", new byte[]{1, 2}, "image/png");
        verify(objectStoragePort, never()).delete(any());
    }

    @Test
    void deletesThePreviousLogoWhenTheExtensionChanges() {
        AppConfig config = AppConfig.createDefaults();
        config.updateAppLogo("logo/app-logo.png");
        when(loadAppConfigPort.getOrCreate()).thenReturn(config);

        service.execute(new UploadAppLogoUseCase.Command("admin-1", new byte[]{1}, "image/webp"));

        verify(objectStoragePort).delete("logo/app-logo.png");
    }
}
