package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.in.UploadAboutImageUseCase;
import com.skateboard.appconfig.application.port.out.ObjectStoragePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UploadAboutImageServiceTest {

    @Mock
    private ObjectStoragePort objectStoragePort;

    private UploadAboutImageService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new UploadAboutImageService(objectStoragePort);
    }

    @Test
    void rejectsAnEmptyFile() {
        assertThatThrownBy(() -> service.execute(new UploadAboutImageUseCase.Command(new byte[0], "image/png")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void storesTheImageUnderAboutUsPrefixAndReturnsASignedUrl() {
        when(objectStoragePort.presignGetUrl(startsWith("about-us/"))).thenReturn("https://signed-url");

        String url = service.execute(new UploadAboutImageUseCase.Command(new byte[]{1, 2}, "image/webp"));

        assertThat(url).isEqualTo("https://signed-url");
        verify(objectStoragePort).put(startsWith("about-us/"), any(), anyString());
    }
}
