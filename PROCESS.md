# Low-Code Platform — Progress Tracking

> Last updated: 2026-05-31
> Based on: [harmonic-wandering-fountain.md](./harmonic-wandering-fountain.md)
> Current branch: `develop`

---

## Overall Progress

| Phase | Branch | Status | Progress |
|-------|--------|--------|----------|
| Phase 1 — Entity Metadata Engine | `feature/lowcode-phase-1-entity-metadata` | ✅ Merged | 100% |
| Phase 2 — Generic CRUD + Data Management | `feature/lowcode-phase-2-generic-crud` | ⬜ Not started | 0% |
| Phase 3 — Visual Page Designer | `feature/lowcode-phase-3-page-designer` | ⬜ Not started | 0% |
| Phase 4 — Page Schema + Runtime Rendering | `feature/lowcode-phase-4-page-schema` | ⬜ Not started | 0% |
| Phase 5 — Auth Wiring + Polish | `feature/lowcode-phase-5-auth-polish` | ⬜ Not started | 0% |

**Overall: 1/5 phases complete (20%)**

---

## Phase 1: Entity Metadata Engine ✅

> Branch: `feature/lowcode-phase-1-entity-metadata` → merged to `develop`
> Commits: `42a1cad` feat: Phase 1 - Entity metadata engine

### Backend (`com.back.lowcode.*`)

| # | Task | Status | File |
|---|------|--------|------|
| 1 | `FieldType` enum (mysqlType, javaType, needsLength, needsPrecision) | ✅ | `enums/FieldType.java` |
| 2 | `EntityMeta` JPA entity | ✅ | `entity/EntityMeta.java` |
| 3 | `FieldMeta` JPA entity | ✅ | `entity/FieldMeta.java` |
| 4 | `DdlLog` audit entity | ✅ | `entity/DdlLog.java` |
| 5 | `EntityMetaRepository` | ✅ | `repository/EntityMetaRepository.java` |
| 6 | `FieldMetaRepository` | ✅ | `repository/FieldMetaRepository.java` |
| 7 | `DdlLogRepository` | ✅ | `repository/DdlLogRepository.java` |
| 8 | `EntityMetaDTO` | ✅ | `dto/EntityMetaDTO.java` |
| 9 | `FieldMetaDTO` | ✅ | `dto/FieldMetaDTO.java` |
| 10 | `EntityListRequest` | ✅ | `dto/EntityListRequest.java` |
| 11 | `DDLService` — CREATE/ALTER/DROP with safety checks | ✅ | `service/DDLService.java` |
| 12 | `EntityMetaService` — CRUD + publish + Redis cache | ✅ | `service/EntityMetaService.java` |
| 13 | `EntityMetaController` — `/lowcode/entity/**` | ✅ | `controller/EntityMetaController.java` |
| 14 | `LowCodeConstants` | ✅ | `config/LowCodeConstants.java` |
| 15 | Redis caching (`lc:meta:{entityCode}:fields`) | ✅ | In `EntityMetaService` |

### Frontend

| # | Task | Status | File |
|---|------|--------|------|
| 1 | TypeScript type definitions (EntityMeta, FieldMeta, FieldType, etc.) | ✅ | `types/lowcode.d.ts` |
| 2 | Entity metadata API module | ✅ | `api/lowcode/entityMeta.ts` |
| 3 | EntityList page (table + publish/archive/delete actions) | ✅ | `pages/lowcode/metadata/EntityList.vue` |
| 4 | EntityEdit page (entity form + draggable field table) | ✅ | `pages/lowcode/metadata/EntityEdit.vue` |
| 5 | Low-code route tree (`/lowcode/entity`, `/lowcode/entity/:id`) | ✅ | `router/modules/lowcode.ts` |

### Verification Checklist

- [ ] Create "Customer" entity (name VARCHAR, phone VARCHAR, age INTEGER, status VARCHAR)
- [ ] Publish entity → verify `lc_customer` table exists in MySQL with correct columns
- [ ] Edit entity fields → verify ALTER TABLE executes correctly
- [ ] Archive entity → verify status changes to "archived"
- [ ] Delete draft entity → verify cascade field deletion

---

## Phase 2: Generic CRUD + Data Management ⬜

> Branch: `feature/lowcode-phase-2-generic-crud` (not yet created)

### Backend — Planned

| # | Task | Status |
|---|------|--------|
| 1 | `GenericQueryRequest` DTO (page, pageSize, filters, sort) | ⬜ |
| 2 | `GenericPageResult` DTO (records, total, page, pageSize) | ⬜ |
| 3 | `GenericCrudService` — dynamic SQL via JdbcTemplate | ⬜ |
| 4 | `GenericCrudController` — `/lowcode/generic/{entityCode}/**` | ⬜ |

### Frontend — Planned

| # | Task | Status |
|---|------|--------|
| 1 | `api/lowcode/genericCrud.ts` | ⬜ |
| 2 | `hooks/useGenericCrud.ts` composable | ⬜ |
| 3 | `pages/lowcode/data/DataManager.vue` — dynamic table + CRUD | ⬜ |

### Verification Checklist

- [ ] POST/PUT/DELETE records via GenericCrudController
- [ ] `curl` test all CRUD endpoints
- [ ] DataManager UI: add/edit/search/delete records

---

## Phase 3: Visual Page Designer ⬜

> Branch: `feature/lowcode-phase-3-page-designer` (not yet created)

### Backend — Planned

| # | Task | Status |
|---|------|--------|
| 1 | `ComponentDef` entity + repository + controller | ⬜ |
| 2 | `DataInitializer` — seed ~15 system components | ⬜ |
| 3 | Dictionary entities + CRUD (`DictType`, `DictItem`) | ⬜ |

### Frontend — Planned

| # | Task | Status |
|---|------|--------|
| 1 | `api/lowcode/componentDef.ts` | ⬜ |
| 2 | `store/modules/designer.ts` — component tree state, selection, undo/redo | ⬜ |
| 3 | `components/lowcode/DynamicForm.vue` — JSON Schema → TDesign form | ⬜ |
| 4 | `pages/lowcode/designer/ComponentPanel.vue` — drag sources | ⬜ |
| 5 | `pages/lowcode/designer/Canvas.vue` — drop target + rendering | ⬜ |
| 6 | `pages/lowcode/designer/CanvasComponent.vue` — selection + drag | ⬜ |
| 7 | `pages/lowcode/designer/PropertyEditor.vue` — schema-driven form | ⬜ |
| 8 | `pages/lowcode/designer/DesignerLayout.vue` — 3-panel assembly | ⬜ |
| 9 | `pages/lowcode/renderer/components/` — initial 9 renderer components | ⬜ |

---

## Phase 4: Page Schema + Runtime Rendering ⬜

> Branch: `feature/lowcode-phase-4-page-schema` (not yet created)

### Backend — Planned

| # | Task | Status |
|---|------|--------|
| 1 | `PageSchema` entity + repository + service + controller | ⬜ |

### Frontend — Planned

| # | Task | Status |
|---|------|--------|
| 1 | `api/lowcode/pageSchema.ts` | ⬜ |
| 2 | Save/Load in DesignerLayout | ⬜ |
| 3 | `pages/lowcode/pages/PageList.vue` | ⬜ |
| 4 | `pages/lowcode/renderer/SchemaRenderer.vue` — runtime renderer | ⬜ |
| 5 | `pages/lowcode/renderer/SchemaRendererComponent.vue` | ⬜ |
| 6 | Add `/lowcode/view/:pageCode` route | ⬜ |

---

## Phase 5: Auth Wiring + Polish ⬜

> Branch: `feature/lowcode-phase-5-auth-polish` (not yet created)

| # | Task | Status |
|---|------|--------|
| 1 | Wire JWT (`JwtUtil`, `TokenFilter`, `useUserStore`) | ⬜ |
| 2 | Permission controls on entities/pages | ⬜ |
| 3 | Chart components (ECharts bar/line/pie) | ⬜ |
| 4 | Form validation driven by `validationRule` JSON | ⬜ |
| 5 | Export/import (CSV) for data tables | ⬜ |
| 6 | Undo/redo in designer | ⬜ |
| 7 | UX polish: loading/empty/error states, responsive | ⬜ |

---

## Git Branch Status

```
main
  └── develop (current)
       ├── feature/lowcode-phase-1-entity-metadata ✅ (merged)
       ├── feature/lowcode-phase-2-generic-crud     ⬜ (not created)
       ├── feature/lowcode-phase-3-page-designer    ⬜ (not created)
       ├── feature/lowcode-phase-4-page-schema      ⬜ (not created)
       └── feature/lowcode-phase-5-auth-polish      ⬜ (not created)
```

---

## Next Action

Start **Phase 2: Generic CRUD + Data Management**:
```bash
git checkout develop
git checkout -b feature/lowcode-phase-2-generic-crud develop
```
