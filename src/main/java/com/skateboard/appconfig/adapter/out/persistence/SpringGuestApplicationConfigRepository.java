package com.skateboard.appconfig.adapter.out.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringGuestApplicationConfigRepository extends JpaRepository<GuestApplicationConfigJpaEntity, UUID> {

    // Singleton table — exactly one row is expected; Pageable(0,1) avoids
    // pulling the whole (one-row) table just to grab "the" row.
    default GuestApplicationConfigJpaEntity findSingleton() {
        List<GuestApplicationConfigJpaEntity> rows = findAll(Pageable.ofSize(1)).getContent();
        return rows.isEmpty() ? null : rows.get(0);
    }
}
