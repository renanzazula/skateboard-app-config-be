package com.skateboard.appconfig.infrastructure.web;

import com.skateboard.appconfig.domain.exception.BrandingAssetNameConflictException;
import com.skateboard.appconfig.domain.exception.BrandingAssetNotFoundException;
import com.skateboard.appconfig.domain.exception.CampaignInvalidStateTransitionException;
import com.skateboard.appconfig.domain.exception.CampaignNotFoundException;
import com.skateboard.appconfig.domain.exception.CampaignScreenLimitExceededException;
import com.skateboard.appconfig.infrastructure.web.dto.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    private static void assertError(ResponseEntity<ErrorResponse> response, HttpStatus status, String message) {
        assertThat(response.getStatusCode()).isEqualTo(status);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(status.value());
        assertThat(response.getBody().getError()).isEqualTo(status.getReasonPhrase());
        assertThat(response.getBody().getMessage()).isEqualTo(message);
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    void accessDeniedMapsToForbidden() {
        assertError(handler.handleAccessDenied(new AccessDeniedException("nope")), HttpStatus.FORBIDDEN, "Access denied");
    }

    @Test
    void brandingAssetNotFoundMapsToNotFound() {
        BrandingAssetNotFoundException ex = new BrandingAssetNotFoundException("id-1");
        assertError(handler.handleBrandingAssetNotFound(ex), HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @Test
    void brandingAssetNameConflictMapsToConflict() {
        BrandingAssetNameConflictException ex = new BrandingAssetNameConflictException("home-header");
        assertError(handler.handleBrandingAssetNameConflict(ex), HttpStatus.CONFLICT, ex.getMessage());
    }

    @Test
    void illegalArgumentMapsToBadRequest() {
        IllegalArgumentException ex = new IllegalArgumentException("bad input");
        assertError(handler.handleBadRequest(ex), HttpStatus.BAD_REQUEST, "bad input");
    }

    @Test
    void campaignNotFoundMapsToNotFound() {
        CampaignNotFoundException ex = new CampaignNotFoundException(UUID.randomUUID());
        assertError(handler.handleCampaignNotFound(ex), HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @Test
    void campaignScreenLimitMapsToBadRequest() {
        CampaignScreenLimitExceededException ex = CampaignScreenLimitExceededException.tooManyScreens(3);
        assertError(handler.handleCampaignScreenLimit(ex), HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @Test
    void campaignInvalidStateMapsToConflict() {
        CampaignInvalidStateTransitionException ex = new CampaignInvalidStateTransitionException("bad transition");
        assertError(handler.handleCampaignInvalidState(ex), HttpStatus.CONFLICT, ex.getMessage());
    }

    @Test
    void typeMismatchMapsToBadRequestWithParamName() {
        MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException(
                "abc", UUID.class, "campaignId", null, new IllegalArgumentException("not a uuid"));

        assertError(handler.handleTypeMismatch(ex), HttpStatus.BAD_REQUEST, "Invalid value for parameter 'campaignId'");
    }

    @Test
    void genericExceptionMapsToInternalServerError() {
        assertError(handler.handleGeneric(new RuntimeException("boom")), HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal server error");
    }
}
