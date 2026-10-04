package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.out.LoadGuestApplicationConfigPort;
import com.skateboard.appconfig.domain.model.GuestApplicationConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class GetGuestApplicationConfigServiceTest {

    @Mock
    private LoadGuestApplicationConfigPort loadGuestApplicationConfigPort;

    private GetGuestApplicationConfigService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new GetGuestApplicationConfigService(loadGuestApplicationConfigPort);
    }

    @Test
    void executeDelegatesToLoadPort() {
        GuestApplicationConfig config = GuestApplicationConfig.createDefaults();
        when(loadGuestApplicationConfigPort.getOrCreate()).thenReturn(config);

        assertThat(service.execute()).isSameAs(config);
    }
}
