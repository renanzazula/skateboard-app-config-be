package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.in.UpdateLoginTextUseCase;
import com.skateboard.appconfig.application.port.out.LoadAppConfigPort;
import com.skateboard.appconfig.application.port.out.SaveAppConfigPort;
import com.skateboard.appconfig.domain.model.AppConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class UpdateLoginTextServiceTest {

    @Mock
    private LoadAppConfigPort loadAppConfigPort;

    @Mock
    private SaveAppConfigPort saveAppConfigPort;

    private UpdateLoginTextService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new UpdateLoginTextService(loadAppConfigPort, saveAppConfigPort);
        when(saveAppConfigPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void updatesTheLoginTextAndTouchesTheConfig() {
        AppConfig config = AppConfig.createDefaults();
        when(loadAppConfigPort.getOrCreate()).thenReturn(config);

        AppConfig result = service.execute(new UpdateLoginTextUseCase.Command("admin-1", "Welcome", "Please log in"));

        assertThat(result.getLoginTitle()).isEqualTo("Welcome");
        assertThat(result.getLoginMessage()).isEqualTo("Please log in");
        assertThat(result.getUpdatedBy()).isEqualTo("admin-1");
    }
}
