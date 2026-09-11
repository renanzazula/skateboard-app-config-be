# skateboard-app-config-be

## What this service is

Owns **application/tenant-level configuration** for the Skateboard platform — not
per-user settings. It is a singleton config store (one `AppConfig` row for the
whole app, no multi-tenant scoping despite some doc references to "tenant").

Concretely, it currently owns:
- **Branding**: login background image, app logo, login screen title/message,
  and a collection of named "branding assets" (reusable admin-uploaded images).
- **About Us**: an admin-editable content-block page (`AboutPage`) with images.
- **Home dashboard config**: which video categories are eligible for the
  mobile Home feed (`HomeVideoCategoryConfig`), and the featured-player
  selection (`HomeFeaturedPlayerConfig`).

Place in the system: `skateboard-fe` (mobile) → `skateboard-ui-backend` (BFF)
→ **this service**. It never talks to other domain microservices directly and
has no reason to — it's purely a config/branding source of truth that the BFF
proxies to.

### Featured Player: `selectionMode` (MANUAL / AUTO)

`HomeFeaturedPlayerConfig.selectionMode` (`V9__home_featured_player_selection_mode.sql`,
`NOT NULL DEFAULT 'MANUAL'` — existing rows backfill to MANUAL, so no live
configuration changes behavior) is a **policy flag only**:

- **MANUAL** (default, and the only mode that existed before this field): an
  admin explicitly picks `contentSource`/`contentId`, exactly as before.
- **AUTO**: this service stores no concrete selection at all — `update()`
  forces `contentId` to `null` whenever `selectionMode == AUTO`, even if a
  caller sends one. There is nothing here to resolve *to* — this service
  doesn't own posts and, per the note above, never calls the service that
  does. `skateboard-ui-backend` is the one that resolves AUTO's actual
  episode, live, on every Home read (it asks skateboard-podcast-be for the
  latest post matching the official episode title pattern). This service
  only decides whether AUTO is turned on.

`contentSource` is still required in both modes when `enabled` — it's what
tells the BFF which resolver family to dispatch to. Only `contentId` is
mode-dependent.

`createDefaults()` (a brand-new singleton row, before any admin has ever
configured one) now returns `position = TOP`, `preferredPlatform = YOUTUBE`,
`selectionMode = MANUAL` — the ticket's requested defaults for a *new*
configuration. An existing row keeps whatever it already had; this only
affects the very first row ever created.

A `selectionMode` of `null` (a pre-migration row read through code that
predates this field, or an update request that omits it) is always treated
as `MANUAL`, never `AUTO` — see `HomeFeaturedPlayerConfig.reconstitute`/`update`.

## Tech stack

- Java 21, Spring Boot 3.4.4 (Spring Web, Spring Data JPA, Spring Security
  OAuth2 resource server, Bean Validation, Actuator).
- PostgreSQL, schema-managed by **Flyway** (`src/main/resources/db/migration`,
  `V1__...` … `V8__...`). Hibernate is `ddl-auto: validate` only — Flyway owns
  the schema, Hibernate just checks entities match it.
- **API-first / OpenAPI-generated controllers**: `api/openapi.yaml` is the
  source of truth. The `openapi-generator-maven-plugin` (bound to `generate`,
  runs on every build) generates interfaces into
  `com.skateboard.appconfig.infrastructure.web.api` (e.g. `AdminApi`,
  `PublicApi`) and DTOs into `...infrastructure.web.dto`. Hand-written
  `@RestController` classes under `adapter/in/rest` `implements` those
  generated interfaces — don't hand-edit anything under
  `target/generated-sources`, edit `api/openapi.yaml` instead and rebuild.
- AWS SDK v2 `S3Client`/`S3Presigner` for object storage (works against MinIO
  locally and a Railway-managed bucket in production — see
  `S3ObjectStorageAdapter`, `RailwayBucketProperties`).
- Auth: Keycloak via Spring's OAuth2 resource server, same realm as the rest
  of the platform (`skateboard-podcast` realm by default, see
  `application.yml`).
- Tests: JUnit 5 + Mockito + AssertJ, plain unit tests (no `@SpringBootTest`,
  no Testcontainers, no test `application.yml`/H2 test config wired up yet
  even though `h2` and `spring-security-test` are on the test classpath).
- Packaging/deploy: Railway (`railpack.json`) — builds the jar with
  `./mvnw clean package -DskipTests`, then bundles the New Relic Java agent
  (`newrelic-config/newrelic.yml`) and runs
  `java -javaagent:newrelic/newrelic.jar -jar target/app.jar` when
  `NEW_RELIC_ENABLED=true`.

## Build / run / test

```bash
./mvnw clean package -DskipTests   # what Railway's build actually runs
./mvnw test                        # unit tests (JUnit5/Mockito/AssertJ)
./mvnw spring-boot:run             # run locally, needs Postgres + MinIO (see below)
```

On Windows use `mvnw.cmd` instead of `./mvnw`.

**JDK gotcha (confirmed on this machine):** the project targets **Java 21**
(`pom.xml` `<java.version>21</java.version>`), but neither the default `java`
on PATH nor `JAVA_HOME` necessarily point at 21 — on this machine `java
-version` resolves to 25 while `JAVA_HOME` is set to a JDK 17 install. Maven
will use whichever JDK `JAVA_HOME`/PATH resolve to, so if a build fails with
compiler-release or "invalid target release" errors, that's almost certainly
why — point `JAVA_HOME` at a JDK 21 install before building.

To run locally you need Postgres and an S3-compatible bucket (MinIO), both
provided by `skateboard-infrastructure`'s `.docker/docker-compose.yaml`, plus
a running Keycloak realm for the OAuth2 issuer. Defaults in
`application.yml` assume: Postgres on `localhost:5432/skateboard`, MinIO on
`localhost:9000` (`minioadmin`/`minioadmin`), Keycloak issuer at
`http://localhost:8180/realms/skateboard-podcast`. Server port is `8083`.

## Architecture: hexagonal / ports & adapters (confirmed in code)

```
adapter/in/rest/            REST controllers, implement generated OpenAPI interfaces
adapter/out/persistence/    Spring Data JPA repositories + JPA entities + persistence adapters
adapter/out/storage/        S3ObjectStorageAdapter (implements ObjectStoragePort), S3 config, bucket props
application/port/in/        Use case interfaces (one per use case, e.g. UploadBrandingAssetUseCase),
                             each carries a nested `Command` record for its input
application/port/out/       Outbound port interfaces (LoadXPort/SaveXPort, ObjectStoragePort, *RepositoryPort)
application/service/        Use case implementations (XService implements XUseCase), pure orchestration,
                             constructor-injected with only outbound ports (no Spring/JPA/AWS types)
domain/model/                Plain domain objects — private constructors + static factories
                             (createDefaults()/reconstitute()) instead of public constructors or setters;
                             mutation happens through named domain methods (updateLoginBackground(),
                             removeAppLogo(), etc.), never field setters
domain/exception/            Domain-specific exceptions (BrandingAssetNotFoundException, ...)
infrastructure/security/     SecurityConfig (JWT/OAuth2 resource server), AudienceValidator
infrastructure/web/          CorrelationIdFilter, GlobalExceptionHandler; web/api + web/dto are
                             OpenAPI-generated (do not hand-edit)
```

Naming convention is very consistent and worth following for new features:
`Get/Update/Upload/Remove/Replace/List<Thing>UseCase` (interface, in
`port/in`) → `<Thing>Service` (impl, in `service`) → `Load<Thing>Port` /
`Save<Thing>Port` / `<Thing>RepositoryPort` (in `port/out`) → JPA entity +
`<Thing>PersistenceAdapter` + `Spring<Thing>Repository` (in
`adapter/out/persistence`).

## Key conventions / gotchas

- **Presigned URLs are never persisted.** Only the bare object key (e.g.
  `branding/login/background.webp`, `assets/{uuid}.webp`) is stored in
  Postgres. `ObjectStoragePort.presignGetUrl(key)` is called fresh in the
  REST layer (controllers, not services) every time a config/asset is
  returned to a client — see `BrandingAdminController.toConfigResponse` /
  `toAssetResponse` and `PublicConfigController`. If you add a new
  image-bearing config, follow this pattern: store the key, presign on read,
  never cache/store the signed URL itself.
- **Authorization is functional-permission based, not role based.** Keycloak
  issues a JWT whose `authorities` claim already contains the effective
  `FUNC_*` permissions (mapped via the realm's protocol mapper in
  `skateboard-infrastructure`'s `realm-export.json`); Spring reads that claim
  verbatim with no `ROLE_`/`SCOPE_` prefix (see `SecurityConfig`). Controllers
  guard mutations with `@PreAuthorize("hasAuthority('FUNC_...')")` — e.g. all
  branding management endpoints require `FUNC_TAB_SETTINGS_BRANDING`. Only
  `GET /api/config` and `/actuator/health` are open; everything else requires
  authentication, and most write endpoints additionally require the specific
  `FUNC_*` authority. `AccessDeniedException` from a denied `@PreAuthorize` is
  translated to 403 by `GlobalExceptionHandler` — don't skip that handler when
  touching exception handling.
- **Every JWT must carry this service's audience.** `AudienceValidator`
  rejects tokens whose `aud` claim doesn't include
  `app.security.oauth2.audience` (default `skateboard-app-config-be`) — this
  must match the FE client's "audience" protocol mapper in Keycloak.
- **Flyway migrations are additive/sequential** (`V1`…`V8`, one file per
  schema change, named after the feature: `V2__branding_asset.sql`,
  `V8__about_us_page.sql`). Schema name is `skateboard-app-config`
  (`spring.flyway.schemas` / `hibernate.default_schema`). Add new migrations
  as the next `Vn__description.sql`; never edit an already-applied one.
- **Domain models are immutable-shaped**: private constructor, `createDefaults()`
  / `reconstitute(...)` static factories, mutation only via intention-revealing
  methods. New domain fields should follow the `AppConfig` pattern (see
  `domain/model/AppConfig.java`) rather than adding public setters.
- The `api/openapi.yaml` file is the actual API contract; `skateboard-fe` and
  `skateboard-ui-backend` are expected to keep their own copies/generated
  clients in sync with it manually (there's no automated contract-sharing
  pipeline visible in this repo).

## Other notes

- `.docs/` exists locally (a Home-dashboard spec and a branding migration
  guide) but is **gitignored** — it's local planning material, not checked
  into the repo, so don't assume it's visible to other contributors or in CI.
- No top-level README; this file plus `api/openapi.yaml` and the migration
  history are the best orientation.
- Recent work (per `git log`): New Relic agent wiring, an "about us"
  content-block feature, home featured-player and home-video-category config,
  branding (login background/app logo/assets) and configurable login
  title/message. These map directly to the controllers/use cases listed above.
