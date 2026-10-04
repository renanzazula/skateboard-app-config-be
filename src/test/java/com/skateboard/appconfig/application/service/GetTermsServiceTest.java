package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.out.LoadTermsPort;
import com.skateboard.appconfig.domain.model.Terms;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class GetTermsServiceTest {

    @Mock
    private LoadTermsPort loadTermsPort;

    private GetTermsService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new GetTermsService(loadTermsPort);
    }

    @Test
    void returnsEmptyWhenNoPageExists() {
        when(loadTermsPort.find()).thenReturn(Optional.empty());

        assertThat(service.execute(true)).isEmpty();
        assertThat(service.execute(false)).isEmpty();
    }

    @Test
    void hidesADraftPageFromTheStandardViewer() {
        Terms draft = Terms.createEmpty();
        when(loadTermsPort.find()).thenReturn(Optional.of(draft));

        assertThat(service.execute(false)).isEmpty();
        assertThat(service.execute(true)).contains(draft);
    }

    @Test
    void showsAPublishedPageToEveryone() {
        Terms page = Terms.createEmpty();
        page.update("Terms & Conditions", "By using this app...", Terms.Status.PUBLISHED, "admin-1");
        when(loadTermsPort.find()).thenReturn(Optional.of(page));

        assertThat(service.execute(false)).contains(page);
        assertThat(service.execute(true)).contains(page);
    }
}
