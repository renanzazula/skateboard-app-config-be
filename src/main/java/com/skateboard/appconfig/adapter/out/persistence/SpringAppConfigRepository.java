package com.skateboard.appconfig.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface SpringAppConfigRepository extends JpaRepository<AppConfigJpaEntity, UUID> {

    // Singleton table — exactly one row is expected; Pageable(0,1) avoids
    // pulling the whole (one-row) table just to grab "the" row.
    default AppConfigJpaEntity findSingleton() {
        List<AppConfigJpaEntity> rows = findAll(Pageable.ofSize(1)).getContent();
        return rows.isEmpty() ? null : rows.get(0);
    }
}
