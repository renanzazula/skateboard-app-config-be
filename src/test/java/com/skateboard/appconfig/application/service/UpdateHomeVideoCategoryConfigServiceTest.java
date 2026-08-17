package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.in.UpdateHomeVideoCategoryConfigUseCase;
import com.skateboard.appconfig.application.port.out.LoadHomeVideoCategoryConfigPort;
import com.skateboard.appconfig.application.port.out.SaveHomeVideoCategoryConfigPort;
import com.skateboard.appconfig.domain.model.HomeVideoCategoryConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UpdateHomeVideoCategoryConfigServiceTest {

    @Mock
    private LoadHomeVideoCategoryConfigPort loadHomeVideoCategoryConfigPort;

    @Mock
    private SaveHomeVideoCategoryConfigPort saveHomeVideoCategoryConfigPort;

    private UpdateHomeVideoCategoryConfigService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new UpdateHomeVideoCategoryConfigService(loadHomeVideoCategoryConfigPort, saveHomeVideoCategoryConfigPort);
    }

    @Test
    void switchingToAllClearsEnabledCategoryIds() {
        HomeVideoCategoryConfig config = HomeVideoCategoryConfig.createDefaults();
        when(loadHomeVideoCategoryConfigPort.getOrCreate()).thenReturn(config);
        when(saveHomeVideoCategoryConfigPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        HomeVideoCategoryConfig updated = service.execute(
                new UpdateHomeVideoCategoryConfigUseCase.Command("admin-1", HomeVideoCategoryConfig.Mode.ALL, Set.of("podcasts")));

        assertThat(updated.getMode()).isEqualTo(HomeVideoCategoryConfig.Mode.ALL);
        assertThat(updated.getEnabledCategoryIds()).isEmpty();
        assertThat(updated.getUpdatedBy()).isEqualTo("admin-1");
    }

    @Test
    void switchingToSelectedPersistsChosenCategories() {
        HomeVideoCategoryConfig config = HomeVideoCategoryConfig.createDefaults();
        when(loadHomeVideoCategoryConfigPort.getOrCreate()).thenReturn(config);
        when(saveHomeVideoCategoryConfigPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        HomeVideoCategoryConfig updated = service.execute(
                new UpdateHomeVideoCategoryConfigUseCase.Command("admin-1", HomeVideoCategoryConfig.Mode.SELECTED,
                        Set.of("podcasts", "skate-clips")));

        assertThat(updated.getMode()).isEqualTo(HomeVideoCategoryConfig.Mode.SELECTED);
        assertThat(updated.getEnabledCategoryIds()).containsExactlyInAnyOrder("podcasts", "skate-clips");
    }

    @Test
    void selectedModeWithNoCategoriesIsRejected() {
        HomeVideoCategoryConfig config = HomeVideoCategoryConfig.createDefaults();
        when(loadHomeVideoCategoryConfigPort.getOrCreate()).thenReturn(config);

        assertThatThrownBy(() -> service.execute(
                new UpdateHomeVideoCategoryConfigUseCase.Command("admin-1", HomeVideoCategoryConfig.Mode.SELECTED, Set.of())))
                .isInstanceOf(IllegalArgumentException.class);
        verify(saveHomeVideoCategoryConfigPort, never()).save(any());
    }
}
