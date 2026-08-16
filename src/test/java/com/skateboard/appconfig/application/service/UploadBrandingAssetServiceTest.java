package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.in.UploadBrandingAssetUseCase;
import com.skateboard.appconfig.application.port.out.BrandingAssetRepositoryPort;
import com.skateboard.appconfig.application.port.out.ObjectStoragePort;
import com.skateboard.appconfig.domain.exception.BrandingAssetNameConflictException;
import com.skateboard.appconfig.domain.model.BrandingAsset;
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

class UploadBrandingAssetServiceTest {

    @Mock
    private BrandingAssetRepositoryPort brandingAssetRepositoryPort;

    @Mock
    private ObjectStoragePort objectStoragePort;

    private UploadBrandingAssetService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new UploadBrandingAssetService(brandingAssetRepositoryPort, objectStoragePort);
    }

    @Test
    void duplicateNameIsRejected() {
        when(brandingAssetRepositoryPort.existsByName("home-header")).thenReturn(true);

        assertThatThrownBy(() -> service.execute(
                new UploadBrandingAssetUseCase.Command("admin-1", "home-header", new byte[]{1}, "image/png")))
                .isInstanceOf(BrandingAssetNameConflictException.class);
        verify(objectStoragePort, never()).put(any(), any(), any());
    }

    @Test
    void validUploadCreatesAssetWithGeneratedKey() {
        when(brandingAssetRepositoryPort.existsByName("home-header")).thenReturn(false);
        when(brandingAssetRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        BrandingAsset asset = service.execute(
                new UploadBrandingAssetUseCase.Command("admin-1", "home-header", new byte[]{1, 2}, "image/webp"));

        assertThat(asset.getName()).isEqualTo("home-header");
        assertThat(asset.getObjectKey()).isEqualTo("assets/" + asset.getId() + ".webp");
        assertThat(asset.getVersion()).isEqualTo(1);
    }
}
