package com.skateboard.appconfig.adapter.out.persistence;

import com.skateboard.appconfig.domain.model.EmailTemplate;
import com.skateboard.appconfig.domain.model.EmailTemplateType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class EmailTemplatePersistenceAdapterTest {

    @Mock
    private SpringEmailTemplateRepository jpaRepository;

    private EmailTemplatePersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        adapter = new EmailTemplatePersistenceAdapter(jpaRepository);
    }

    @Test
    void savingANewTemplateStampsCreatedAt() {
        when(jpaRepository.save(any(EmailTemplateJpaEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        EmailTemplate template = EmailTemplate.createDefault(EmailTemplateType.GUEST_APPLICATION_RECEIVED, "en");

        EmailTemplate saved = adapter.save(template);

        assertThat(saved.getId()).isEqualTo(template.getId());
        assertThat(saved.getType()).isEqualTo(EmailTemplateType.GUEST_APPLICATION_RECEIVED);
        assertThat(saved.getLanguage()).isEqualTo("en");
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void savingAnExistingRowReusesTheEntityAndSkipsCreatedAt() {
        EmailTemplate template = EmailTemplate.createDefault(EmailTemplateType.GUEST_APPLICATION_RECEIVED, "en");
        template.update("New subject", "New body", true, "admin-2");

        EmailTemplateJpaEntity existing = new EmailTemplateJpaEntity();
        existing.setId(template.getId());
        existing.setCreatedAt(Instant.parse("2025-01-01T00:00:00Z"));
        when(jpaRepository.findById(template.getId())).thenReturn(Optional.of(existing));
        when(jpaRepository.save(any(EmailTemplateJpaEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        EmailTemplate saved = adapter.save(template);

        assertThat(saved.getSubject()).isEqualTo("New subject");
        assertThat(saved.getUpdatedBy()).isEqualTo("admin-2");
        assertThat(existing.getCreatedAt()).isEqualTo(Instant.parse("2025-01-01T00:00:00Z"));
    }

    @Test
    void findByTypeAndLanguageMapsToTheDomain() {
        UUID id = UUID.randomUUID();
        EmailTemplateJpaEntity entity = new EmailTemplateJpaEntity();
        entity.setId(id);
        entity.setType(EmailTemplateType.GUEST_APPLICATION_ADMIN_NOTIFICATION);
        entity.setLanguage("pt");
        entity.setSubject("Assunto");
        entity.setBody("Corpo");
        entity.setEnabled(true);
        entity.setCreatedAt(Instant.parse("2025-01-01T00:00:00Z"));
        when(jpaRepository.findByTypeAndLanguage(EmailTemplateType.GUEST_APPLICATION_ADMIN_NOTIFICATION, "pt"))
                .thenReturn(Optional.of(entity));

        Optional<EmailTemplate> found = adapter.findByTypeAndLanguage(
                EmailTemplateType.GUEST_APPLICATION_ADMIN_NOTIFICATION, "pt");

        assertThat(found).isPresent();
        assertThat(found.get().getSubject()).isEqualTo("Assunto");
        assertThat(found.get().getLanguage()).isEqualTo("pt");
    }

    @Test
    void findByIdReturnsEmptyWhenMissing() {
        UUID id = UUID.randomUUID();
        when(jpaRepository.findById(id)).thenReturn(Optional.empty());

        assertThat(adapter.findById(id)).isEmpty();
    }
}
