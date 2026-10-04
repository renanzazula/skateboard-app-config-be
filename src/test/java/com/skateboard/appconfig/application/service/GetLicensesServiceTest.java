package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.out.LoadLicensesPort;
import com.skateboard.appconfig.domain.model.Licenses;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class GetLicensesServiceTest {

    @Mock
    private LoadLicensesPort loadLicensesPort;

    private GetLicensesService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new GetLicensesService(loadLicensesPort);
    }

    @Test
    void returnsEmptyWhenNoPageExists() {
        when(loadLicensesPort.find()).thenReturn(Optional.empty());

        assertThat(service.execute(true)).isEmpty();
        assertThat(service.execute(false)).isEmpty();
    }

    @Test
    void hidesADraftPageFromTheStandardViewer() {
        Licenses draft = Licenses.createEmpty();
        when(loadLicensesPort.find()).thenReturn(Optional.of(draft));

        assertThat(service.execute(false)).isEmpty();
        assertThat(service.execute(true)).contains(draft);
    }

    @Test
    void showsAPublishedPageToEveryone() {
        Licenses page = Licenses.createEmpty();
        page.update("Open-source Licenses", "This app uses...", Licenses.Status.PUBLISHED, "admin-1");
        when(loadLicensesPort.find()).thenReturn(Optional.of(page));

        assertThat(service.execute(false)).contains(page);
        assertThat(service.execute(true)).contains(page);
    }
}
