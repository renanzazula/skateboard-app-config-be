package com.skateboard.appconfig.application.service;

import com.skateboard.appconfig.application.port.out.LoadPrivacyPolicyPort;
import com.skateboard.appconfig.domain.model.PrivacyPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class GetPrivacyPolicyServiceTest {

    @Mock
    private LoadPrivacyPolicyPort loadPrivacyPolicyPort;

    private GetPrivacyPolicyService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new GetPrivacyPolicyService(loadPrivacyPolicyPort);
    }

    @Test
    void returnsEmptyWhenNoPageExists() {
        when(loadPrivacyPolicyPort.find()).thenReturn(Optional.empty());

        assertThat(service.execute(true)).isEmpty();
        assertThat(service.execute(false)).isEmpty();
    }

    @Test
    void hidesADraftPageFromTheAnonymousViewer() {
        PrivacyPolicy draft = PrivacyPolicy.createEmpty();
        when(loadPrivacyPolicyPort.find()).thenReturn(Optional.of(draft));

        assertThat(service.execute(false)).isEmpty();
        assertThat(service.execute(true)).contains(draft);
    }

    @Test
    void showsAPublishedPageToEveryone() {
        PrivacyPolicy page = PrivacyPolicy.createEmpty();
        page.update("Privacy Policy", "We collect...", PrivacyPolicy.Status.PUBLISHED, "admin-1");
        when(loadPrivacyPolicyPort.find()).thenReturn(Optional.of(page));

        assertThat(service.execute(false)).contains(page);
        assertThat(service.execute(true)).contains(page);
    }
}
