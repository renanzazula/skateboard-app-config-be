package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.in.ReplaceBrandingAssetUseCase;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReplaceBrandingAssetServiceTest {

    @Mock
    private BrandingAssetRepositoryPort brandingAssetRepositoryPort;

    @Mock
    private ObjectStoragePort objectStoragePort;

    private ReplaceBrandingAssetService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new ReplaceBrandingAssetService(brandingAssetRepositoryPort, objectStoragePort);
        when(brandingAssetRepositoryPort.save(org.mockito.ArgumentMatchers.any()))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void throwsWhenAssetIsUnknown() {
        UUID id = UUID.randomUUID();
        when(brandingAssetRepositoryPort.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(
                new ReplaceBrandingAssetUseCase.Command("admin-1", id, new byte[]{1}, "image/png")))
                .isInstanceOf(BrandingAssetNotFoundException.class);
    }

    @Test
    void replacesTheObjectAndDeletesThePreviousOneWhenKeyChanges() {
        UUID id = UUID.randomUUID();
        BrandingAsset asset = BrandingAsset.reconstitute(id, "home-header", "assets/old-key.png", "image/png", 1,
                java.time.Instant.now(), java.time.Instant.now(), "admin-1");
        when(brandingAssetRepositoryPort.findById(id)).thenReturn(Optional.of(asset));

        BrandingAsset saved = service.execute(
                new ReplaceBrandingAssetUseCase.Command("admin-2", id, new byte[]{1, 2}, "image/webp"));

        assertThat(saved.getObjectKey()).isEqualTo("assets/" + id + ".webp");
        assertThat(saved.getVersion()).isEqualTo(2);
        assertThat(saved.getUpdatedBy()).isEqualTo("admin-2");
        verify(objectStoragePort).put("assets/" + id + ".webp", new byte[]{1, 2}, "image/webp");
        verify(objectStoragePort).delete("assets/old-key.png");
    }

    @Test
    void skipsDeletionWhenTheKeyDoesNotChange() {
        UUID id = UUID.randomUUID();
        String key = "assets/" + id + ".png";
        BrandingAsset asset = BrandingAsset.reconstitute(id, "home-header", key, "image/png", 1,
                java.time.Instant.now(), java.time.Instant.now(), "admin-1");
        when(brandingAssetRepositoryPort.findById(id)).thenReturn(Optional.of(asset));

        service.execute(new ReplaceBrandingAssetUseCase.Command("admin-2", id, new byte[]{1}, "image/png"));

        verify(objectStoragePort, never()).delete(org.mockito.ArgumentMatchers.any());
    }
}
