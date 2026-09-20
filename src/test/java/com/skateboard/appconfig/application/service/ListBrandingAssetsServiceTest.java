package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.out.BrandingAssetRepositoryPort;
import com.skateboard.appconfig.domain.model.BrandingAsset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class ListBrandingAssetsServiceTest {

    @Mock
    private BrandingAssetRepositoryPort brandingAssetRepositoryPort;

    private ListBrandingAssetsService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new ListBrandingAssetsService(brandingAssetRepositoryPort);
    }

    @Test
    void returnsAllAssets() {
        UUID id = UUID.randomUUID();
        BrandingAsset asset = BrandingAsset.create(id, "home-header", "assets/" + id + ".png", "image/png", "admin-1");
        when(brandingAssetRepositoryPort.findAll()).thenReturn(List.of(asset));

        assertThat(service.execute()).containsExactly(asset);
    }
}
