package com.skateboard.appconfig.adapter.out.persistence;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "guest_application_config")
public class GuestApplicationConfigJpaEntity {

    @Id
    private UUID id;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "guest_application_config_recipient", joinColumns = @JoinColumn(name = "config_id"))
    @Column(name = "recipient_id")
    private Set<UUID> recipientIds = new LinkedHashSet<>();

    @Column(name = "confirmation_subject", nullable = false)
    private String confirmationSubject;

    @Column(name = "confirmation_body", nullable = false)
    private String confirmationBody;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    // Nullable — reflects the domain's "last explicit admin change" (null
    // until update() runs), not a generic row-write audit; mirrors
    // HomeFeaturedPlayerConfigJpaEntity/HomeVideoCategoryConfigJpaEntity.
    @Column(name = "updated_at")
    private Instant updatedAt;

    // Required by JPA/Hibernate to instantiate the entity via reflection.
    public GuestApplicationConfigJpaEntity() {}

    public UUID getId()                     { return id; }
    public boolean isEnabled()               { return enabled; }
    public Set<UUID> getRecipientIds()       { return recipientIds; }
    public String getConfirmationSubject()   { return confirmationSubject; }
    public String getConfirmationBody()      { return confirmationBody; }
    public String getUpdatedBy()             { return updatedBy; }
    public Instant getCreatedAt()            { return createdAt; }
    public Instant getUpdatedAt()            { return updatedAt; }

    public void setId(UUID id)                           { this.id = id; }
    public void setEnabled(boolean v)                     { this.enabled = v; }
    public void setRecipientIds(Set<UUID> v)              { this.recipientIds = v; }
    public void setConfirmationSubject(String v)          { this.confirmationSubject = v; }
    public void setConfirmationBody(String v)             { this.confirmationBody = v; }
    public void setUpdatedBy(String v)                    { this.updatedBy = v; }
    public void setCreatedAt(Instant v)                   { this.createdAt = v; }
    public void setUpdatedAt(Instant v)                   { this.updatedAt = v; }
}
