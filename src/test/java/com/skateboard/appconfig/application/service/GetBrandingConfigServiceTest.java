package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.out.LoadAppConfigPort;
import com.skateboard.appconfig.domain.model.AppConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class GetBrandingConfigServiceTest {

    @Mock
    private LoadAppConfigPort loadAppConfigPort;

    private GetBrandingConfigService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new GetBrandingConfigService(loadAppConfigPort);
    }

    @Test
    void returnsTheConfig() {
        AppConfig config = AppConfig.createDefaults();
        when(loadAppConfigPort.getOrCreate()).thenReturn(config);

        assertThat(service.execute()).isSameAs(config);
    }
}
