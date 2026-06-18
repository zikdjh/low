# Low-Code Platform Implementation Plan

## Context

Based on the existing Spring Boot 3.5.14 + Vue 3/TDesign codebase (currently a blog platform scaffold with auth, Redis, JPA, and theming infrastructure), build a low-code platform targeting **admin/CRUD management backends**. The platform uses **dynamic data models** (users define entity structures at runtime; the platform generates DDL and generic CRUD). MVP delivers **visual page designer** + **data management (CRUD)**.

---

## GitFlow Branching Strategy

遵循 [GitFlow](https://nvie.com/posts/a-successful-git-branching-model/) 开发规范，分支模型如下：

```
main (生产)
  └── develop (开发主线)
       ├── feature/lowcode-phase-1-entity-metadata
       ├── feature/lowcode-phase-2-generic-crud
       ├── feature/lowcode-phase-3-page-designer
       ├── feature/lowcode-phase-4-page-schema
       ├── feature/lowcode-phase-5-auth-polish
       └── release/lowcode-v1.0 → main + develop
```

### 分支命名规范

| 分支类型 | 命名格式 | 用途 |
|----------|----------|------|
| `main` | `main` | 生产就绪代码，仅通过 release/hotfix 合并，打 tag |
| `develop` | `develop` | 开发主线，所有 feature 分支的合并目标 |
| `feature` | `feature/lowcode-phase-{N}-{简述}` | 功能开发，从 develop 创建，完成后合并回 develop |
| `release` | `release/lowcode-v{版本号}` | 发布准备，从 develop 创建，合并到 main + develop |
| `hotfix` | `hotfix/lowcode-{简述}` | 紧急修复，从 main 创建，合并到 main + develop |

### 操作流程

```bash
# 1. 初始化 develop 分支（首次）
git checkout -b develop main
git push -u origin develop

# 2. 每个 Phase 创建 feature 分支
git checkout develop
git pull origin develop
git checkout -b feature/lowcode-phase-1-entity-metadata develop

# 3. 开发完成后合并到 develop（通过 PR/MR）
# ... 在 feature 分支上开发 ...
git checkout develop
git merge --no-ff feature/lowcode-phase-1-entity-metadata
git push origin develop

# 4. 所有阶段完成后创建 release 分支
git checkout -b release/lowcode-v1.0 develop
# ... 测试、修 bug ...
git checkout main
git merge --no-ff release/lowcode-v1.0
git tag -a v1.0.0 -m "Low-code platform v1.0"
git checkout develop
git merge --no-ff release/lowcode-v1.0
```

### 约束规则
- **禁止**直接在 `main` 或 `develop` 上提交代码
- Feature 分支必须从 `develop` 最新节点创建
- 合并到 `develop`/`main` 使用 `--no-ff` 保留分支历史
- 每个 Phase 先合入 `develop`，验证通过后再开始下一 Phase
- Release 分支仅做 bug 修复和文档完善，不添加新功能

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│  Frontend (Vue 3 + TDesign + Vite 8, port 5174)            │
│                                                             │
│  ┌──────────────────┐  ┌────────────┐  ┌────────────────┐  │
│  │ Designer (3-panel)│  │ Schema     │  │ Metadata/Data  │  │
│  │ ComponentPanel    │  │ Renderer   │  │ Manager Pages  │  │
│  │ Canvas            │  │ (runtime)  │  │                │  │
│  │ PropertyEditor    │  │            │  │                │  │
│  └──────────────────┘  └────────────┘  └────────────────┘  │
└──────────────────────────┬──────────────────────────────────┘
                           │ /api → proxy → port 8080
┌──────────────────────────┴──────────────────────────────────┐
│  Backend (Spring Boot 3.5.14, Java 21, MySQL + Redis)      │
│                                                             │
│  com.back.lowcode/                                          │
│  ├── entity/     EntityMeta, FieldMeta, PageSchema,         │
│  │               ComponentDef, DictType, DictItem           │
│  ├── enums/      FieldType                                  │
│  ├── dto/        Request/response DTOs                      │
│  ├── repository/ Spring Data JPA repos                      │
│  ├── service/    EntityMetaService, DDLService,             │
│  │               GenericCrudService, PageSchemaService      │
│  ├── controller/ REST controllers                           │
│  ├── config/     LowCodeConstants                           │
│  └── util/       DDL builder helpers                        │
│                                                             │
│  Reuses: Result, RedisUtil, GlobalExceptionHandler,         │
│          Sql*Exception, RateLimit annotation, JacksonConfig │
└─────────────────────────────────────────────────────────────┘
```

---

## Core Data Model (Database Tables)

| Table | Purpose |
|-------|---------|
| `lc_entity_meta` | Entity definitions (code, name, tableName, status) |
| `lc_field_meta` | Field definitions (FK to entity, fieldType, length, nullable, showInList/Form/Search, validationRule JSON) |
| `lc_page_schema` | Page designs (layoutJson LONGTEXT, pageType, version, status) |
| `lc_component_def` | Component registry (compKey, category, defaultPropsJson, propsSchemaJson) |
| `lc_dict_type` | Dictionary type definitions |
| `lc_dict_item` | Dictionary items (label, value, sortOrder, color) |
| `lc_ddl_log` | DDL audit log |
| `lc_{entity_code}` | **Dynamic** user-created tables (e.g., `lc_customer`, `lc_order`) — NOT managed by JPA, accessed via JdbcTemplate |

---

## Key Technical Decisions

### 1. Dynamic DDL Safety
- All dynamic tables prefixed `lc_`; DDLService refuses to operate without this prefix.
- Column names validated against metadata cache; never from raw user input.
- DDL wrapped in transaction; audit-logged to `lc_ddl_log`.
- Columns never auto-dropped; renamed to `__deleted_xxx` for grace period.
- Diffs old vs new field metadata to generate only necessary ALTER statements.

### 2. Generic CRUD via JdbcTemplate
- `GenericCrudService` builds dynamic SQL from cached field metadata.
- Column names always sourced from `lc_field_meta` cache (Redis, key `lc:meta:{entityCode}`).
- All values parameterized via `NamedParameterJdbcTemplate`.
- Supports operators: `__like`, `__gte`, `__lte`, `__between`, `__in`.
- Auto type conversion: `FieldType` → Java type (INTEGER→int, DATETIME→LocalDateTime, etc.).

### 3. Canvas: Inline Rendering (Not Iframe)
- Designer renders real Vue components in the same DOM.
- Components accept `designMode: boolean` (injected via `provide`/`inject`).
- In design mode: mock/sample data, click-to-select instead of business logic.
- Benefits: shared TDesign theme/CSS, no cross-context complexity, natural Vue reactivity.

### 4. Property Editor: JSON Schema-Driven
- Each `ComponentDef.propsSchemaJson` describes editable properties using simplified JSON Schema + `ui:widget` hints.
- `DynamicForm.vue` interprets the schema and renders TDesign form controls.
- Power-user escape hatch: raw JSON editor tab.

### 5. API Convention
- All endpoints under `/lowcode/` namespace.
- All responses use existing `Result` wrapper (`{code, msg, data}`, 1=success, 0=error).
- Fix frontend `ResponseType` to use `msg` (not `message`) to match backend.

---

## Implementation Phases

### Phase 1: Entity Metadata Engine (Foundation)

> **Branch**: `feature/lowcode-phase-1-entity-metadata` → `develop`

**Backend** — `com.back.lowcode.*`:
1. `enums/FieldType.java` — enum with `mysqlType`, `javaType`, `needsLength`, `needsPrecision`
2. `entity/EntityMeta.java` + `entity/FieldMeta.java` — JPA entities
3. `repository/EntityMetaRepository.java` + `repository/FieldMetaRepository.java`
4. `dto/` — EntityMetaDTO, FieldMetaDTO, EntityListRequest
5. `service/DDLService.java` — `generateCreateTable(EntityMeta, List<FieldMeta>)` using JdbcTemplate, prefix check, audit logging
6. `service/EntityMetaService.java` — CRUD + `publishEntity()` that calls DDLService
7. `controller/EntityMetaController.java` — `/lowcode/entity/**`
8. Redis caching: store field metadata with key `lc:meta:{entityCode}` using `RedisUtil.saveJsonToRedisHash`

**Frontend**:
1. `types/lowcode.d.ts` — EntityMeta, FieldMeta, FieldType interfaces
2. `api/lowcode/entityMeta.ts` — API calls
3. `pages/lowcode/metadata/EntityList.vue` — table with publish/delete actions
4. `pages/lowcode/metadata/EntityEdit.vue` — edit entity + draggable field list builder
5. `router/modules/lowcode.ts` — low-code route tree

**Deliverable**: Create entity → define fields → publish → physical MySQL table exists.

### Phase 2: Generic CRUD + Data Management

> **Branch**: `feature/lowcode-phase-2-generic-crud` → `develop`

**Backend**:
1. `dto/GenericQueryRequest.java` + `dto/GenericPageResult.java`
2. `service/GenericCrudService.java` — dynamic SQL: query(paginated+filter+sort), insert, update, delete, getById
3. `controller/GenericCrudController.java` — `/lowcode/generic/{entityCode}/**`

**Frontend**:
1. `api/lowcode/genericCrud.ts`
2. `hooks/useGenericCrud.ts` — composable with fetch/create/update/remove
3. `pages/lowcode/data/DataManager.vue` — dynamic table with search bar, toolbar, add/edit dialog, batch delete

**Deliverable**: Navigate to `/lowcode/data/{entityCode}` → full CRUD table with server-side pagination, search, sort.

### Phase 3: Visual Page Designer

> **Branch**: `feature/lowcode-phase-3-page-designer` → `develop`

**Backend**:
1. `entity/ComponentDef.java` + repository + controller
2. `config/DataInitializer.java` — seed ~15 system components (page-header, search-bar, data-table, form-*, card, row/col, chart-*, etc.)
3. Dictionary entities + CRUD (`DictType`, `DictItem`)

**Frontend**:
1. `api/lowcode/componentDef.ts`
2. `store/modules/designer.ts` — component tree state, selection, undo/redo, drag state
3. `components/lowcode/DynamicForm.vue` — JSON Schema → TDesign form (reusable)
4. `pages/lowcode/designer/ComponentPanel.vue` — categorized drag sources with HTML5 drag/drop
5. `pages/lowcode/designer/Canvas.vue` — drop target, recursive component rendering, zoom controls
6. `pages/lowcode/designer/CanvasComponent.vue` — selection border, drag handle, delete button
7. `pages/lowcode/designer/PropertyEditor.vue` — DynamicForm + component tree outline + raw JSON tab
8. `pages/lowcode/designer/DesignerLayout.vue` — 3-panel assembly
9. `pages/lowcode/renderer/components/` — initial 9 renderer components supporting designMode

**Deliverable**: Drag components → configure properties → see live preview in canvas.

### Phase 4: Page Schema + Runtime Rendering

> **Branch**: `feature/lowcode-phase-4-page-schema` → `develop`

**Backend**:
1. `entity/PageSchema.java` + repository + service + controller

**Frontend**:
1. `api/lowcode/pageSchema.ts`
2. Save/Load in DesignerLayout (persist to backend)
3. `pages/lowcode/pages/PageList.vue` — manage saved pages
4. `pages/lowcode/renderer/SchemaRenderer.vue` — runtime renderer (designMode=false)
5. `pages/lowcode/renderer/SchemaRendererComponent.vue` — recursive runtime component
6. Add `/lowcode/view/:pageCode` route

**Deliverable**: Design → save → publish → view live generated page with real data.

### Phase 5: Auth Wiring + Polish

> **Branch**: `feature/lowcode-phase-5-auth-polish` → `develop` → `release/lowcode-v1.0` → `main`

1. Wire JWT: `JwtUtil`, `TokenFilter`, fill in `useUserStore`
2. Permission controls on entities/pages
3. Chart components (ECharts bar/line/pie via existing `echarts` dep)
4. Form validation driven by `validationRule` JSON
5. Export/import (CSV) for data tables
6. Undo/redo in designer
7. UX polish: loading/empty/error states, responsive

---

## Files to Reuse

| Existing File | Reused For |
|---------------|-----------|
| `com.back.common.Result` | All API responses |
| `com.back.utils.RedisUtil` | Cache entity metadata, DDL audit logging |
| `com.back.exception.GlobalExceptionHandler` | Error handling (already catches Exception, adds Sql*Exception throws) |
| `com.back.exception.SqlInsertException/SqlUpdateException/SqlDeleteException` | Thrown by GenericCrudService on DB failures |
| `com.back.annotation.RateLimit` | Wire to generic CRUD endpoints |
| `com.back.config.basic.JacksonConfig` (`endObjectMapper`) | Serialization in RedisUtil and JSON handling |
| `com.back.config.WebConfig` | Extend CORS and add TokenFilter to interceptor chain |
| `front/src/api/index.ts` (Axios instance) | All low-code API modules reuse this instance |
| `front/src/store/modules/setting.ts` | Theme applies to designer + rendered pages |
| `front/src/layouts/index.vue` | Layout wrapper for all low-code pages |
| `front/src/components/color/ColorPicker.vue` | Used in designer property editor for color-type props |

---

## Verification Plan

1. **Phase 1**: Create "Customer" entity (name VARCHAR, phone VARCHAR, age INTEGER, status VARCHAR) → Publish → Verify `lc_customer` table exists in MySQL with correct columns.
2. **Phase 2**: POST/PUT/DELETE records via GenericCrudController → `curl` test all CRUD endpoints → Verify data in MySQL. Then use DataManager UI to add/edit/search/delete records.
3. **Phase 3**: Open designer → Drag "Data Table" + "Search Bar" → Select entity → Configure columns → Verify canvas shows correct mock layout. Check property editor form renders from propsSchemaJson.
4. **Phase 4**: Save page → Verify `lc_page_schema` row exists → Publish → Navigate to `/lowcode/view/customer-list` → Verify live data table renders with real data from API.
5. **Phase 5**: Login flow → Token in sessionStorage → Access-protected endpoints → Token refresh on 499 → Verify undo/redo in designer works.
