package com.skateboard.appconfig.adapter.out.persistence;

import com.skateboard.appconfig.domain.model.EmailTemplateType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "email_template")
public class EmailTemplateJpaEntity {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private EmailTemplateType type;

    @Column(name = "language", nullable = false, length = 5)
    private String language;

    @Column(name = "subject", nullable = false)
    private String subject;

    @Column(name = "body", nullable = false)
    private String body;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    // Required by JPA/Hibernate to instantiate the entity via reflection.
    public EmailTemplateJpaEntity() {}

    public UUID getId()                { return id; }
    public EmailTemplateType getType() { return type; }
    public String getLanguage()        { return language; }
    public String getSubject()         { return subject; }
    public String getBody()            { return body; }
    public boolean isEnabled()         { return enabled; }
    public String getUpdatedBy()       { return updatedBy; }
    public Instant getCreatedAt()      { return createdAt; }
    public Instant getUpdatedAt()      { return updatedAt; }

    public void setId(UUID id)                           { this.id = id; }
    public void setType(EmailTemplateType type)           { this.type = type; }
    public void setLanguage(String language)              { this.language = language; }
    public void setSubject(String subject)                { this.subject = subject; }
    public void setBody(String body)                      { this.body = body; }
    public void setEnabled(boolean enabled)                { this.enabled = enabled; }
    public void setUpdatedBy(String updatedBy)             { this.updatedBy = updatedBy; }
    public void setCreatedAt(Instant createdAt)            { this.createdAt = createdAt; }
    public void setUpdatedAt(Instant updatedAt)            { this.updatedAt = updatedAt; }
}
