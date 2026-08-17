package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.out.LoadHomeVideoCategoryConfigPort;
import com.skateboard.appconfig.domain.model.HomeVideoCategoryConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class GetHomeVideoCategoryConfigServiceTest {

    @Mock
    private LoadHomeVideoCategoryConfigPort loadHomeVideoCategoryConfigPort;

    private GetHomeVideoCategoryConfigService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new GetHomeVideoCategoryConfigService(loadHomeVideoCategoryConfigPort);
    }

    @Test
    void returnsDefaultsWhenNothingConfiguredYet() {
        HomeVideoCategoryConfig defaults = HomeVideoCategoryConfig.createDefaults();
        when(loadHomeVideoCategoryConfigPort.getOrCreate()).thenReturn(defaults);

        HomeVideoCategoryConfig result = service.execute();

        assertThat(result.getMode()).isEqualTo(HomeVideoCategoryConfig.Mode.ALL);
        assertThat(result.getEnabledCategoryIds()).isEmpty();
    }
}
