package com.skateboard.appconfig.adapter.in.rest;

import com.skateboard.appconfig.application.port.in.GetEmailTemplateUseCase;
import com.skateboard.appconfig.application.port.in.GetEmailTemplatesUseCase;
import com.skateboard.appconfig.application.port.in.UpdateEmailTemplateUseCase;
import com.skateboard.appconfig.domain.model.EmailTemplate;
import com.skateboard.appconfig.infrastructure.web.api.EmailTemplatesApi;
import com.skateboard.appconfig.infrastructure.web.dto.EmailTemplateResponse;
import com.skateboard.appconfig.infrastructure.web.dto.UpdateEmailTemplateRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * The {@code type} path variable is a plain {@code String} in the generated
 * interface, not the generated DTO enum — see api/openapi.yaml's
 * getEmailTemplate parameter description for why (Spring's default
 * path-variable enum conversion doesn't survive openapi-generator's
 * common-prefix stripping). It's turned into the domain
 * {@code EmailTemplateType} via {@code valueOf} here instead, which does
 * carry the full "GUEST_APPLICATION_..." names. The response body's
 * {@code type} field stays enum-typed — {@link #toDtoType} maps the domain
 * enum to it by constant name after going through {@code fromValue}, since
 * the generated enum's own constant names are the same shortened strings.
 */
@RestController
public class EmailTemplateController implements EmailTemplatesApi {

    private final GetEmailTemplatesUseCase getEmailTemplatesUseCase;
    private final GetEmailTemplateUseCase getEmailTemplateUseCase;
    private final UpdateEmailTemplateUseCase updateEmailTemplateUseCase;

    public EmailTemplateController(GetEmailTemplatesUseCase getEmailTemplatesUseCase,
                                    GetEmailTemplateUseCase getEmailTemplateUseCase,
                                    UpdateEmailTemplateUseCase updateEmailTemplateUseCase) {
        this.getEmailTemplatesUseCase = getEmailTemplatesUseCase;
        this.getEmailTemplateUseCase = getEmailTemplateUseCase;
        this.updateEmailTemplateUseCase = updateEmailTemplateUseCase;
    }

    @Override
    @PreAuthorize("hasAuthority('FUNC_EMAIL_TEMPLATE_MANAGE')")
    public ResponseEntity<List<EmailTemplateResponse>> listEmailTemplates() {
        List<EmailTemplateResponse> response = getEmailTemplatesUseCase.execute().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @Override
    @PreAuthorize("hasAuthority('FUNC_EMAIL_TEMPLATE_MANAGE')")
    public ResponseEntity<EmailTemplateResponse> getEmailTemplate(String type, String language) {
        EmailTemplate template = getEmailTemplateUseCase.execute(
                new GetEmailTemplateUseCase.Query(parseType(type), language));
        return ResponseEntity.ok(toResponse(template));
    }

    @Override
    @PreAuthorize("hasAuthority('FUNC_EMAIL_TEMPLATE_MANAGE')")
    public ResponseEntity<EmailTemplateResponse> updateEmailTemplate(UUID id, UpdateEmailTemplateRequest request) {
        EmailTemplate updated = updateEmailTemplateUseCase.execute(new UpdateEmailTemplateUseCase.Command(
                currentAdminId(), id, request.getSubject(), request.getBody(), Boolean.TRUE.equals(request.getEnabled())));
        return ResponseEntity.ok(toResponse(updated));
    }

    private EmailTemplateResponse toResponse(EmailTemplate template) {
        return new EmailTemplateResponse()
                .id(template.getId())
                .type(toDtoType(template.getType()))
                .language(template.getLanguage())
                .subject(template.getSubject())
                .body(template.getBody())
                .enabled(template.isEnabled())
                .updatedAt(toOffsetDateTime(template.getUpdatedAt()))
                .updatedBy(template.getUpdatedBy());
    }

    private com.skateboard.appconfig.domain.model.EmailTemplateType parseType(String type) {
        try {
            return com.skateboard.appconfig.domain.model.EmailTemplateType.valueOf(type);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown email template type: " + type);
        }
    }

    private com.skateboard.appconfig.infrastructure.web.dto.EmailTemplateType toDtoType(
            com.skateboard.appconfig.domain.model.EmailTemplateType domainType) {
        return com.skateboard.appconfig.infrastructure.web.dto.EmailTemplateType.fromValue(domainType.name());
    }

    private OffsetDateTime toOffsetDateTime(Instant instant) {
        return instant != null ? OffsetDateTime.ofInstant(instant, ZoneOffset.UTC) : null;
    }

    private String currentAdminId() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            return null;
        }
    }
}
