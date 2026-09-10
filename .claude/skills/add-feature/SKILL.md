---
name: add-feature
description: >-
  Add a new endpoint, use case, config type, or vertical slice to
  skateboard-app-config-be following its hexagonal / OpenAPI-first
  conventions. Use whenever the task is "add an endpoint", "new use case",
  "new config", "expose X in the API", "add a field to <config>", or a new
  Flyway migration. Gives the exact file set, layer order, naming rules, and
  repo-specific gotchas (presigned URLs, singleton configs, FUNC_* auth,
  Java 21 build).
---

# Adding a feature to skateboard-app-config-be

This service is hexagonal (ports & adapters) and **API-first**: `api/openapi.yaml`
is the contract, and `openapi-generator-maven-plugin` regenerates controller
interfaces (`...infrastructure.web.api`, one per `tags:` entry) and DTOs
(`...infrastructure.web.dto`) on every `generate` phase. Never hand-edit
anything under `target/generated-sources` — edit `api/openapi.yaml` and rebuild.

## Build / test (Java 21)

`java`/`JAVA_HOME` on this machine do **not** point at 21. Always:

```bash
JAVA_HOME="C:/Program Files/Java/jdk-21" ./mvnw.cmd clean test
JAVA_HOME="C:/Program Files/Java/jdk-21" ./mvnw.cmd clean package -DskipTests
```

Regenerating from the spec happens automatically during `generate` (before
`compile`), so a plain `./mvnw.cmd test` picks up `openapi.yaml` edits.

## Do it in this order

### 1. `api/openapi.yaml` — the contract first

- Add the path under an existing `tags:` entry, or add a new tag (a new tag =
  a new generated `<Tag>Api` interface). Match the existing style: `operationId`
  in camelCase, `summary`, `security: [ bearerAuth: [] ]` for authed routes,
  and the custom `x-required-permissions: [ FUNC_... ]` annotation for
  permission-gated routes (documentation only — enforcement is `@PreAuthorize`
  in the controller).
- Declare `'401'` / `'403'` / `'404'` responses pointing at
  `#/components/schemas/ErrorResponse` (that's the shape `GlobalExceptionHandler`
  returns).
- Add request/response schemas under `components/schemas`. Timestamps are
  `type: string, format: date-time` (generated as `OffsetDateTime`).
- If the route must work pre-auth (like `GET /api/config`), it also needs an
  entry in `SecurityConfig.filterChain` — `@PreAuthorize` alone won't open it.

### 2. `domain/model/` — the domain object

Immutable-shaped, following `AppConfig` / `HomeVideoCategoryConfig`:

- `private` constructor; `public static createDefaults()` and
  `public static reconstitute(...)` factories — **no** public constructors or
  setters.
- Mutation only through intention-revealing methods
  (`updateCategories(...)`, `removeAppLogo()`, `touch(adminId)`), which also
  set `updatedAt`. Validate invariants inside these methods and throw
  `IllegalArgumentException` (maps to 400) or a domain exception.
- Getters return unmodifiable copies of collections.
- For image-bearing state, store **only the object key** (`branding/...`,
  `assets/{uuid}.webp`) — never a presigned/signed URL.

New domain exceptions go in `domain/exception/` (extend `RuntimeException`,
message in the constructor, Javadoc stating the HTTP status) and must get an
`@ExceptionHandler` in `infrastructure/web/GlobalExceptionHandler`.

### 3. `application/port/in/` — the use case interface

One interface per use case, named `Get/Update/Upload/Remove/Replace/List<Thing>UseCase`.
Input is a nested `record Command(...)` (include `adminId` when a write needs
audit attribution). Single method, conventionally `execute(Command)` — or
`execute()` with no args for a plain read.

### 4. `application/port/out/` — outbound ports

- `Load<Thing>Port` / `Save<Thing>Port` (singleton configs use a single
  `getOrCreate()` + `save()` pair, often on one adapter), or a richer
  `<Thing>RepositoryPort` for real aggregates (see `CampaignRepositoryPort`).
- `ObjectStoragePort` already exists for S3 — reuse it, don't add a new one.

### 5. `application/service/` — the implementation

`<Thing>Service implements <Thing>UseCase`, `@Service`, `@Transactional` on
writes. Constructor-injected with **outbound ports only** — no Spring MVC, JPA,
or AWS types here. Pure orchestration: load → call a domain method → save.

### 6. `adapter/out/persistence/` — persistence

- `<Thing>JpaEntity` — `@Entity @Table(name = "snake_case")`, field setters/getters
  (entities are the one place mutable JavaBeans are fine), `@Enumerated(EnumType.STRING)`
  for enums.
- `Spring<Thing>Repository extends JpaRepository<Entity, UUID>`. Singleton
  tables: add a `default findSingleton()` using `findAll(Pageable.ofSize(1))`.
- `<Thing>PersistenceAdapter implements Load/Save...Port`, `@Component`, with
  private `toDomain` / `toEntity` mappers. On update, load the existing entity
  by id and mutate it rather than `new`-ing one (preserves `createdAt`).

### 7. `src/main/resources/db/migration/Vn__<feature>.sql`

Next sequential number (currently through `V9`). **Never edit an applied
migration.** Schema is `skateboard-app-config` (set globally, don't prefix
table names). `TIMESTAMPTZ` for instants, `UUID PRIMARY KEY`, child/collection
tables `REFERENCES parent (id) ON DELETE CASCADE`. Hibernate runs
`ddl-auto: validate`, so the entity and the table must match exactly.

### 8. `adapter/in/rest/` — the controller

`@RestController` `implements` the generated `<Tag>Api` interface. It:

- maps generated DTO ↔ use-case `Command` / domain (a `<Thing>WebMapper` if
  the mapping is non-trivial — see `CampaignWebMapper`);
- guards writes with `@PreAuthorize("hasAuthority('FUNC_...')")` matching the
  spec's `x-required-permissions`;
- reads the caller id from `SecurityContextHolder.getContext().getAuthentication().getName()`;
- **presigns image URLs here, on every read**, via `ObjectStoragePort.presignGetUrl(key)`
  — never in the service, never stored. See `BrandingAdminController.toConfigResponse`.

### 9. Tests

Plain JUnit 5 + Mockito + AssertJ, **no** `@SpringBootTest` / Testcontainers.
Mirror `UpdateHomeVideoCategoryConfigServiceTest`: `@Mock` the outbound ports,
`MockitoAnnotations.openMocks(this)` in `@BeforeEach`, `new` the service
directly, assert domain behaviour and `verify(...)` port interactions
(including `never()` on the rejection paths). Add a
`<Thing>PersistenceAdapterTest` if the adapter has real mapping/reconciliation
logic.

## Checklist before done

- [ ] `api/openapi.yaml` updated; `./mvnw.cmd clean test` (with JDK 21) green
- [ ] new domain exception (if any) handled in `GlobalExceptionHandler`
- [ ] pre-auth routes also opened in `SecurityConfig`
- [ ] migration is the next `Vn__`, entity matches it (`ddl-auto: validate`)
- [ ] no presigned URL persisted or produced outside the REST layer
- [ ] service depends on ports only; unit tests added
- [ ] if the contract changed, note it — `skateboard-fe` / `skateboard-ui-backend`
      sync their OpenAPI copies manually
