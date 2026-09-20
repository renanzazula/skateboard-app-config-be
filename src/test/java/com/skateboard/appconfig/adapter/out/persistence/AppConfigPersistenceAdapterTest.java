package com.skateboard.appconfig.adapter.out.persistence;

import com.skateboard.appconfig.domain.model.AppConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit-level coverage of {@link AppConfigPersistenceAdapter}'s mapping to/from
 * {@link AppConfigJpaEntity}. No database: {@link SpringAppConfigRepository}
 * is mocked with {@code CALLS_REAL_METHODS} so its {@code findSingleton()}
 * default method also runs for real.
 */
class AppConfigPersistenceAdapterTest {

    private SpringAppConfigRepository jpaRepository;

    private AppConfigPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        jpaRepository = Mockito.mock(SpringAppConfigRepository.class, Mockito.CALLS_REAL_METHODS);
        adapter = new AppConfigPersistenceAdapter(jpaRepository);
    }

    @Test
    void createsDefaultsWhenNoRowExists() {
        when(jpaRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));
        when(jpaRepository.findById(any())).thenReturn(Optional.empty());
        when(jpaRepository.save(any(AppConfigJpaEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        AppConfig config = adapter.getOrCreate();

        assertThat(config.getId()).isNotNull();
        assertThat(config.getAppLogoKey()).isNull();
    }

    @Test
    void loadsTheExistingSingletonRow() {
        AppConfigJpaEntity entity = new AppConfigJpaEntity();
        entity.setId(java.util.UUID.randomUUID());
        entity.setLoginBackgroundKey("login/bg.png");
        entity.setLoginBackgroundVersion(2);
        entity.setAppLogoKey("logo/app-logo.png");
        entity.setAppLogoVersion(1);
        entity.setLoginTitle("Welcome");
        entity.setLoginMessage("Please log in");
        entity.setUpdatedBy("admin-1");
        when(jpaRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(entity)));

        AppConfig config = adapter.getOrCreate();

        assertThat(config.getId()).isEqualTo(entity.getId());
        assertThat(config.getLoginBackgroundKey()).isEqualTo("login/bg.png");
        assertThat(config.getLoginBackgroundVersion()).isEqualTo(2);
        assertThat(config.getAppLogoKey()).isEqualTo("logo/app-logo.png");
        assertThat(config.getLoginTitle()).isEqualTo("Welcome");
        assertThat(config.getLoginMessage()).isEqualTo("Please log in");
        assertThat(config.getUpdatedBy()).isEqualTo("admin-1");
    }

    @Test
    void savingAnExistingRowReusesTheEntityAndSkipsCreatedAt() {
        AppConfig config = AppConfig.createDefaults();
        config.updateAppLogo("logo/app-logo.png");
        config.touch("admin-2");

        AppConfigJpaEntity existing = new AppConfigJpaEntity();
        existing.setId(config.getId());
        existing.setCreatedAt(java.time.Instant.parse("2025-01-01T00:00:00Z"));
        when(jpaRepository.findById(config.getId())).thenReturn(Optional.of(existing));
        when(jpaRepository.save(any(AppConfigJpaEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        AppConfig saved = adapter.save(config);

        assertThat(saved.getAppLogoKey()).isEqualTo("logo/app-logo.png");
        assertThat(saved.getUpdatedBy()).isEqualTo("admin-2");
        assertThat(existing.getCreatedAt()).isEqualTo(java.time.Instant.parse("2025-01-01T00:00:00Z"));
    }
}
