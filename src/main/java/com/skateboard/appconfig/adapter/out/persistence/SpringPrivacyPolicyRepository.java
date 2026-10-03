package com.skateboard.appconfig.adapter.out.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringPrivacyPolicyRepository extends JpaRepository<PrivacyPolicyJpaEntity, UUID> {

    // Singleton table — at most one row; Pageable(0,1) avoids scanning it all
    // just to grab "the" row. Mirrors SpringAboutPageRepository.
    default PrivacyPolicyJpaEntity findSingleton() {
        List<PrivacyPolicyJpaEntity> rows = findAll(Pageable.ofSize(1)).getContent();
        return rows.isEmpty() ? null : rows.get(0);
    }
}
