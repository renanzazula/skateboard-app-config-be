package com.skateboard.appconfig.adapter.out.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringLicensesRepository extends JpaRepository<LicensesJpaEntity, UUID> {

    // Singleton table — at most one row; Pageable(0,1) avoids scanning it all
    // just to grab "the" row. Mirrors SpringPrivacyPolicyRepository.
    default LicensesJpaEntity findSingleton() {
        List<LicensesJpaEntity> rows = findAll(Pageable.ofSize(1)).getContent();
        return rows.isEmpty() ? null : rows.get(0);
    }
}
