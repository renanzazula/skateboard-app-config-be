package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.in.RemoveBrandingAssetUseCase;
import com.skateboard.appconfig.application.port.out.BrandingAssetRepositoryPort;
import com.skateboard.appconfig.application.port.out.ObjectStoragePort;
import com.skateboard.appconfig.domain.exception.BrandingAssetNotFoundException;
import com.skateboard.appconfig.domain.model.BrandingAsset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RemoveBrandingAssetServiceTest {

    @Mock
    private BrandingAssetRepositoryPort brandingAssetRepositoryPort;

    @Mock
    private ObjectStoragePort objectStoragePort;

    private RemoveBrandingAssetService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new RemoveBrandingAssetService(brandingAssetRepositoryPort, objectStoragePort);
    }

    @Test
    void throwsWhenAssetIsUnknown() {
        UUID id = UUID.randomUUID();
        when(brandingAssetRepositoryPort.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(new RemoveBrandingAssetUseCase.Command(id)))
                .isInstanceOf(BrandingAssetNotFoundException.class);
    }

    @Test
    void deletesTheAssetRowAndItsObject() {
        UUID id = UUID.randomUUID();
        BrandingAsset asset = BrandingAsset.create(id, "home-header", "assets/" + id + ".png", "image/png", "admin-1");
        when(brandingAssetRepositoryPort.findById(id)).thenReturn(Optional.of(asset));

        service.execute(new RemoveBrandingAssetUseCase.Command(id));

        verify(brandingAssetRepositoryPort).deleteById(id);
        verify(objectStoragePort).delete("assets/" + id + ".png");
        assertThat(asset.getId()).isEqualTo(id);
    }
}
