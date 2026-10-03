package com.skateboard.appconfig.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "privacy_policy_page")
public class PrivacyPolicyJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "text")
    private String body;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    // Required by JPA/Hibernate to instantiate the entity via reflection.
    public PrivacyPolicyJpaEntity() {}

    public UUID getId()           { return id; }
    public String getTitle()      { return title; }
    public String getBody()       { return body; }
    public String getStatus()     { return status; }
    public String getUpdatedBy()  { return updatedBy; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setId(UUID id)           { this.id = id; }
    public void setTitle(String v)       { this.title = v; }
    public void setBody(String v)        { this.body = v; }
    public void setStatus(String v)      { this.status = v; }
    public void setUpdatedBy(String v)   { this.updatedBy = v; }
    public void setCreatedAt(Instant v)  { this.createdAt = v; }
    public void setUpdatedAt(Instant v)  { this.updatedAt = v; }
}
