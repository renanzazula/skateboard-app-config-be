package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.out.LoadAboutPagePort;
import com.skateboard.appconfig.domain.model.AboutPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class GetAboutPageServiceTest {

    @Mock
    private LoadAboutPagePort loadAboutPagePort;

    private GetAboutPageService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new GetAboutPageService(loadAboutPagePort);
    }

    @Test
    void returnsEmptyWhenNoPageExists() {
        when(loadAboutPagePort.find()).thenReturn(Optional.empty());

        assertThat(service.execute(true)).isEmpty();
        assertThat(service.execute(false)).isEmpty();
    }

    @Test
    void hidesADraftPageFromTheStandardViewer() {
        AboutPage draft = AboutPage.createEmpty();
        when(loadAboutPagePort.find()).thenReturn(Optional.of(draft));

        assertThat(service.execute(false)).isEmpty();
        assertThat(service.execute(true)).contains(draft);
    }

    @Test
    void showsAPublishedPageToEveryone() {
        AboutPage page = AboutPage.createEmpty();
        page.update("About us", "Sub", AboutPage.Status.PUBLISHED, java.util.List.of(), "admin-1");
        when(loadAboutPagePort.find()).thenReturn(Optional.of(page));

        assertThat(service.execute(false)).contains(page);
        assertThat(service.execute(true)).contains(page);
    }
}
