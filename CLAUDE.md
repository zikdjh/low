# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Repository layout

Two independent projects in one repo:

- `back/` — Spring Boot 3.5.14 / Java 17 / Maven backend (port **8080**)
- `front/` — Vue 3 + TypeScript + Vite frontend (dev port **5174**)

The frontend dev server proxies `/api/*` → `http://0.0.0.0:8080/*` (path prefix `/api` is stripped — see `front/vite.config.ts`). The backend itself does **not** mount controllers under `/api`; that prefix exists only as a frontend convention.

## Common commands

### Backend (`back/`)

```bash
# from back/
./mvnw spring-boot:run          # run dev server
./mvnw.cmd spring-boot:run      # Windows / cmd
./mvnw test                     # run all tests
./mvnw test -Dtest=ClassName    # run a single test class
./mvnw clean package            # build jar
```

Requires MySQL on `127.0.0.1:3306` with database `low_end` and Redis on `localhost:6379`. Credentials are hard-coded in `back/src/main/resources/application.yaml` (`root` / `zc@qq.com0501`) — change locally rather than committing. `spring.jpa.hibernate.ddl-auto: update` auto-creates schema for the metadata tables on startup.

### Frontend (`front/`)

```bash
# from front/
npm install
npm run dev                     # vite dev server on :5174
npm run build                   # vue-tsc + vite build
npm run preview                 # preview built bundle
```

Backend must be running for any low-code feature; the frontend proxies all `/api` calls to it.

## Architecture

This is a **low-code platform**: end users define business entities in the UI, the backend creates real MySQL tables for them, and a visual page designer composes pages over those entities.

### Two layers of "data"

There are two completely separate data planes — keep them straight when reading code:

1. **Metadata plane** — JPA entities under `back/src/main/java/com/back/lowcode/entity/` (`EntityMeta`, `FieldMeta`, `PageSchema`, `ComponentDef`, `DictType`, `DictItem`, `DdlLog`). Stored in fixed tables prefixed `lc_*`, managed by Spring Data JPA repositories.
2. **Dynamic data plane** — user-defined business tables. These have **no JPA entity classes**; they are accessed through `JdbcTemplate` / `NamedParameterJdbcTemplate` in `DynamicDataService`, with column lists driven by `FieldMeta` rows looked up at request time.

When changing low-code code, never mix the two: don't add `@Entity` for user data, don't bypass `FieldMeta` to read user tables.

### Entity lifecycle

`EntityMeta.status` is a strict state machine, enforced in `EntityMetaService`:

- `draft` — metadata exists, no physical table. Editable. Deletable.
- `published` — physical table created via `DDLService.generateCreateTable`. Field edits go through `DDLService.generateAlterTable` (additive: new fields → `ADD COLUMN`; removed fields are **soft-deleted** by renaming to `__deleted_*`, never `DROP`). `DynamicDataController` only accepts CRUD when status is `published`.
- `archived` — read-only.

CRUD on user data is gated by `published` status in every `DynamicDataService` method — preserve this guard when adding operations.

### DDL safety contract

`DDLService` is the only path that emits DDL. It enforces three invariants — preserve them when editing:

1. Tables it touches must start with `LowCodeConstants.TABLE_PREFIX` (`lc_`). Anything else throws.
2. Column names must match `^[a-zA-Z][a-zA-Z0-9_]*$` and not be a MySQL reserved word (`MySQLReservedWords.isReserved`). Same check is duplicated for entity codes / field codes in `EntityMetaService`.
3. Every executed statement is logged to `lc_ddl_log` (`DdlLog`) with success/failure, regardless of outcome.

`FieldType` (enum) is the bridge between metadata and SQL — it knows the MySQL column type per logical type and how to render `VARCHAR(n)` / `DECIMAL(p,s)`. New field types go here.

### REST surface

All low-code controllers live under `/lowcode/*`:

- `/lowcode/entity` — entity & field metadata CRUD; `POST /{id}/publish`, `POST /{id}/archive`
- `/lowcode/data/{entityCode}` — dynamic CRUD over a published entity
- `/lowcode/page` — `PageSchema` (visual designer output, stored as JSON in `layout_json`)
- `/lowcode/component` — `ComponentDef` palette items (seeded by `DataInitializer` on first run)
- `/lowcode/dict` — dictionaries (seeded by `DataInitializer`)

All responses are wrapped in `com.back.common.Result` with non-standard codes: **`code: 1` means success, `code: 0` means error** (not the HTTP-style 200/500). The frontend's axios interceptor in `front/src/api/index.ts` relies on this.

### Caching

`EntityMetaService` writes published field metadata to Redis under `lc:meta:{code}:fields` with a 30-day TTL, and re-populates on cache miss in `getCachedFields`. Any code path that mutates `FieldMeta` for a published entity must call `cacheFieldMeta` (or `invalidateCache`) afterwards — or queries that read from Redis will see stale schema.

### Auth

`back/src/main/java/com/back/interceptors/PathInterceptor.java` currently only logs the request path — there is no real auth interceptor wired up despite the `java-jwt` dependency and the `access`-header logic in the frontend's axios interceptor. Treat backend endpoints as effectively unauthenticated for now; if you add auth, register the new interceptor in `WebConfig`.

CORS in `WebConfig` allows all origins with credentials — broad by design for the dynamic-host dev setup.

### Frontend structure

- `src/api/lowcode/*` — typed axios wrappers, one file per backend resource (`entityMeta`, `dynamicData`, `pageSchema`, `componentDef`, `dict`).
- `src/pages/lowcode/`
  - `metadata/` — `EntityList.vue`, `EntityEdit.vue` (entity + field designer)
  - `data/DataList.vue` — generic CRUD UI driven by `FieldMeta`
  - `page/PageDesigner.vue` + `designer/` — drag-and-drop page builder. Designer state (component tree, selection, undo history) lives in `src/store/modules/designer.ts` (Pinia). The runtime renderer is `src/pages/lowcode/SchemaRenderer.vue`.
- `src/router/modules/lowcode.ts` — routes for the above.
- UI components are TDesign Vue Next (`tdesign-vue-next`); icons from `tdesign-icons-vue-next`.

### Seeding

`DataInitializer` (a `CommandLineRunner`) seeds the component palette and three default dictionaries (`status`, `gender`, `yes_no`) on first start — guarded by `count() > 0` checks so it's idempotent. To re-seed, truncate `lc_component_def` / `lc_dict_type` / `lc_dict_item`.
