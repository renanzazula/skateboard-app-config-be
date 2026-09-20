package com.skateboard.appconfig.infrastructure.security;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class AudienceValidatorTest {

    private final AudienceValidator validator = new AudienceValidator("skateboard-app-config");

    @Test
    void succeedsWhenTheRequiredAudienceIsPresent() {
        Jwt jwt = Mockito.mock(Jwt.class);
        when(jwt.getAudience()).thenReturn(List.of("skateboard-app-config", "other-client"));

        OAuth2TokenValidatorResult result = validator.validate(jwt);

        assertThat(result.hasErrors()).isFalse();
    }

    @Test
    void failsWhenTheRequiredAudienceIsMissing() {
        Jwt jwt = Mockito.mock(Jwt.class);
        when(jwt.getAudience()).thenReturn(List.of("other-client"));

        OAuth2TokenValidatorResult result = validator.validate(jwt);

        assertThat(result.hasErrors()).isTrue();
        assertThat(result.getErrors()).extracting(e -> e.getErrorCode()).contains("invalid_token");
    }
}
