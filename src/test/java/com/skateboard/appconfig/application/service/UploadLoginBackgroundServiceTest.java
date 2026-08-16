package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.in.UploadLoginBackgroundUseCase;
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

class UploadLoginBackgroundServiceTest {

    @Mock
    private LoadAppConfigPort loadAppConfigPort;

    @Mock
    private SaveAppConfigPort saveAppConfigPort;

    @Mock
    private ObjectStoragePort objectStoragePort;

    private UploadLoginBackgroundService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new UploadLoginBackgroundService(loadAppConfigPort, saveAppConfigPort, objectStoragePort);
    }

    @Test
    void oversizedFileIsRejected() {
        byte[] tooLarge = new byte[6 * 1024 * 1024];

        assertThatThrownBy(() -> service.execute(new UploadLoginBackgroundUseCase.Command("admin-1", tooLarge, "image/png")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("5 MB");
        verify(objectStoragePort, never()).put(any(), any(), any());
    }

    @Test
    void unsupportedMimeTypeIsRejected() {
        byte[] data = new byte[]{1, 2, 3};

        assertThatThrownBy(() -> service.execute(new UploadLoginBackgroundUseCase.Command("admin-1", data, "image/gif")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported image type");
        verify(objectStoragePort, never()).put(any(), any(), any());
    }

    @Test
    void validUploadStoresFileAndBumpsVersion() {
        byte[] data = new byte[]{1, 2, 3};
        AppConfig config = AppConfig.createDefaults();
        when(loadAppConfigPort.getOrCreate()).thenReturn(config);
        when(saveAppConfigPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AppConfig updated = service.execute(new UploadLoginBackgroundUseCase.Command("admin-1", data, "image/png"));

        assertThat(updated.getLoginBackgroundKey()).isEqualTo("login/background.png");
        assertThat(updated.getLoginBackgroundVersion()).isEqualTo(1);
        verify(objectStoragePort).put("login/background.png", data, "image/png");
    }
}
