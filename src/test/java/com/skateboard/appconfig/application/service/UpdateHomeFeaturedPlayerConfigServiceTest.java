package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.in.UpdateHomeFeaturedPlayerConfigUseCase;
import com.skateboard.appconfig.application.port.out.LoadHomeFeaturedPlayerConfigPort;
import com.skateboard.appconfig.application.port.out.SaveHomeFeaturedPlayerConfigPort;
import com.skateboard.appconfig.domain.model.FeaturedContentSource;
import com.skateboard.appconfig.domain.model.HomeFeaturedPlayerConfig;
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

class UpdateHomeFeaturedPlayerConfigServiceTest {

    @Mock
    private LoadHomeFeaturedPlayerConfigPort loadHomeFeaturedPlayerConfigPort;

    @Mock
    private SaveHomeFeaturedPlayerConfigPort saveHomeFeaturedPlayerConfigPort;

    private UpdateHomeFeaturedPlayerConfigService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new UpdateHomeFeaturedPlayerConfigService(loadHomeFeaturedPlayerConfigPort, saveHomeFeaturedPlayerConfigPort);
    }

    @Test
    void enablingWithContentPersistsSelection() {
        HomeFeaturedPlayerConfig config = HomeFeaturedPlayerConfig.createDefaults();
        when(loadHomeFeaturedPlayerConfigPort.getOrCreate()).thenReturn(config);
        when(saveHomeFeaturedPlayerConfigPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        HomeFeaturedPlayerConfig updated = service.execute(new UpdateHomeFeaturedPlayerConfigUseCase.Command(
                "admin-1", true, FeaturedContentSource.PODCAST, "post-1",
                HomeFeaturedPlayerConfig.PlayerType.MINI, HomeFeaturedPlayerConfig.Position.BOTTOM, null,
                HomeFeaturedPlayerConfig.SelectionMode.MANUAL));

        assertThat(updated.isEnabled()).isTrue();
        assertThat(updated.getContentSource()).isEqualTo(FeaturedContentSource.PODCAST);
        assertThat(updated.getContentId()).isEqualTo("post-1");
        assertThat(updated.getSelectionMode()).isEqualTo(HomeFeaturedPlayerConfig.SelectionMode.MANUAL);
        assertThat(updated.getUpdatedBy()).isEqualTo("admin-1");
    }

    @Test
    void enablingWithPreferredPlatformPersistsIt() {
        HomeFeaturedPlayerConfig config = HomeFeaturedPlayerConfig.createDefaults();
        when(loadHomeFeaturedPlayerConfigPort.getOrCreate()).thenReturn(config);
        when(saveHomeFeaturedPlayerConfigPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        HomeFeaturedPlayerConfig updated = service.execute(new UpdateHomeFeaturedPlayerConfigUseCase.Command(
                "admin-1", true, FeaturedContentSource.PODCAST, "post-1",
                HomeFeaturedPlayerConfig.PlayerType.MINI, HomeFeaturedPlayerConfig.Position.BOTTOM,
                HomeFeaturedPlayerConfig.PreferredPlatform.YOUTUBE, HomeFeaturedPlayerConfig.SelectionMode.MANUAL));

        assertThat(updated.getPreferredPlatform()).isEqualTo(HomeFeaturedPlayerConfig.PreferredPlatform.YOUTUBE);
    }

    @Test
    void disablingDoesNotRequireContent() {
        HomeFeaturedPlayerConfig config = HomeFeaturedPlayerConfig.createDefaults();
        when(loadHomeFeaturedPlayerConfigPort.getOrCreate()).thenReturn(config);
        when(saveHomeFeaturedPlayerConfigPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        HomeFeaturedPlayerConfig updated = service.execute(new UpdateHomeFeaturedPlayerConfigUseCase.Command(
                "admin-1", false, null, null,
                HomeFeaturedPlayerConfig.PlayerType.MINI, HomeFeaturedPlayerConfig.Position.BOTTOM, null,
                HomeFeaturedPlayerConfig.SelectionMode.MANUAL));

        assertThat(updated.isEnabled()).isFalse();
    }

    @Test
    void enablingWithoutContentIsRejected() {
        HomeFeaturedPlayerConfig config = HomeFeaturedPlayerConfig.createDefaults();
        when(loadHomeFeaturedPlayerConfigPort.getOrCreate()).thenReturn(config);

        assertThatThrownBy(() -> service.execute(new UpdateHomeFeaturedPlayerConfigUseCase.Command(
                "admin-1", true, null, null,
                HomeFeaturedPlayerConfig.PlayerType.MINI, HomeFeaturedPlayerConfig.Position.BOTTOM, null,
                HomeFeaturedPlayerConfig.SelectionMode.MANUAL)))
                .isInstanceOf(IllegalArgumentException.class);
        verify(saveHomeFeaturedPlayerConfigPort, never()).save(any());
    }

    @Test
    void enablingAutoWithoutContentIdSucceeds() {
        HomeFeaturedPlayerConfig config = HomeFeaturedPlayerConfig.createDefaults();
        when(loadHomeFeaturedPlayerConfigPort.getOrCreate()).thenReturn(config);
        when(saveHomeFeaturedPlayerConfigPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        HomeFeaturedPlayerConfig updated = service.execute(new UpdateHomeFeaturedPlayerConfigUseCase.Command(
                "admin-1", true, FeaturedContentSource.PODCAST, null,
                HomeFeaturedPlayerConfig.PlayerType.MINI, HomeFeaturedPlayerConfig.Position.TOP, null,
                HomeFeaturedPlayerConfig.SelectionMode.AUTO));

        assertThat(updated.isEnabled()).isTrue();
        assertThat(updated.getSelectionMode()).isEqualTo(HomeFeaturedPlayerConfig.SelectionMode.AUTO);
        assertThat(updated.getContentId()).isNull();
    }

    @Test
    void switchingToAutoClearsAPreviouslyPickedContentId() {
        HomeFeaturedPlayerConfig config = HomeFeaturedPlayerConfig.createDefaults();
        when(loadHomeFeaturedPlayerConfigPort.getOrCreate()).thenReturn(config);
        when(saveHomeFeaturedPlayerConfigPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // A caller sending a stale/leftover contentId alongside AUTO must not
        // have it persisted — AUTO is pure policy, resolved elsewhere.
        HomeFeaturedPlayerConfig updated = service.execute(new UpdateHomeFeaturedPlayerConfigUseCase.Command(
                "admin-1", true, FeaturedContentSource.PODCAST, "post-1",
                HomeFeaturedPlayerConfig.PlayerType.MINI, HomeFeaturedPlayerConfig.Position.TOP, null,
                HomeFeaturedPlayerConfig.SelectionMode.AUTO));

        assertThat(updated.getContentId()).isNull();
    }

    @Test
    void enablingAutoWithoutContentSourceIsRejected() {
        HomeFeaturedPlayerConfig config = HomeFeaturedPlayerConfig.createDefaults();
        when(loadHomeFeaturedPlayerConfigPort.getOrCreate()).thenReturn(config);

        assertThatThrownBy(() -> service.execute(new UpdateHomeFeaturedPlayerConfigUseCase.Command(
                "admin-1", true, null, null,
                HomeFeaturedPlayerConfig.PlayerType.MINI, HomeFeaturedPlayerConfig.Position.TOP, null,
                HomeFeaturedPlayerConfig.SelectionMode.AUTO)))
                .isInstanceOf(IllegalArgumentException.class);
        verify(saveHomeFeaturedPlayerConfigPort, never()).save(any());
    }

    @Test
    void omittedSelectionModeDefaultsToManual() {
        HomeFeaturedPlayerConfig config = HomeFeaturedPlayerConfig.createDefaults();
        when(loadHomeFeaturedPlayerConfigPort.getOrCreate()).thenReturn(config);
        when(saveHomeFeaturedPlayerConfigPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        HomeFeaturedPlayerConfig updated = service.execute(new UpdateHomeFeaturedPlayerConfigUseCase.Command(
                "admin-1", true, FeaturedContentSource.PODCAST, "post-1",
                HomeFeaturedPlayerConfig.PlayerType.MINI, HomeFeaturedPlayerConfig.Position.BOTTOM, null, null));

        assertThat(updated.getSelectionMode()).isEqualTo(HomeFeaturedPlayerConfig.SelectionMode.MANUAL);
        assertThat(updated.getContentId()).isEqualTo("post-1");
    }
}
