package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.out.LoadHomeFeaturedPlayerConfigPort;
import com.skateboard.appconfig.domain.model.HomeFeaturedPlayerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class GetHomeFeaturedPlayerConfigServiceTest {

    @Mock
    private LoadHomeFeaturedPlayerConfigPort loadHomeFeaturedPlayerConfigPort;

    private GetHomeFeaturedPlayerConfigService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new GetHomeFeaturedPlayerConfigService(loadHomeFeaturedPlayerConfigPort);
    }

    @Test
    void returnsDefaultsWhenNothingConfiguredYet() {
        HomeFeaturedPlayerConfig defaults = HomeFeaturedPlayerConfig.createDefaults();
        when(loadHomeFeaturedPlayerConfigPort.getOrCreate()).thenReturn(defaults);

        HomeFeaturedPlayerConfig result = service.execute();

        assertThat(result.isEnabled()).isFalse();
        assertThat(result.getContentSource()).isNull();
        assertThat(result.getContentId()).isNull();
        assertThat(result.getPlayerType()).isEqualTo(HomeFeaturedPlayerConfig.PlayerType.MINI);
        assertThat(result.getPosition()).isEqualTo(HomeFeaturedPlayerConfig.Position.BOTTOM);
    }
}
