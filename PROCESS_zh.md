# 低代码平台 — 进度跟踪

> 最后更新: 2026-05-31
> 基于: [harmonic-wandering-fountain.md](./harmonic-wandering-fountain.md)
> 当前分支: `develop`

---

## 整体进度

| 阶段 | 分支 | 状态 | 进度 |
|------|------|------|------|
| 阶段 1 — 实体元数据引擎 | `feature/lowcode-phase-1-entity-metadata` | ✅ 已合并 | 100% |
| 阶段 2 — 通用 CRUD + 数据管理 | `feature/lowcode-phase-2-generic-crud` | ⬜ 未开始 | 0% |
| 阶段 3 — 可视化页面设计器 | `feature/lowcode-phase-3-page-designer` | ⬜ 未开始 | 0% |
| 阶段 4 — 页面 Schema + 运行时渲染 | `feature/lowcode-phase-4-page-schema` | ⬜ 未开始 | 0% |
| 阶段 5 — 权限接入 + 细节打磨 | `feature/lowcode-phase-5-auth-polish` | ⬜ 未开始 | 0% |

**总体: 1/5 阶段完成 (20%)**

---

## 阶段 1: 实体元数据引擎 ✅

> 分支: `feature/lowcode-phase-1-entity-metadata` → 已合并至 `develop`
> 提交: `42a1cad` feat: Phase 1 - Entity metadata engine

### 后端 (`com.back.lowcode.*`)

| # | 任务 | 状态 | 文件 |
|---|------|------|------|
| 1 | `FieldType` 枚举 (mysqlType, javaType, needsLength, needsPrecision) | ✅ | `enums/FieldType.java` |
| 2 | `EntityMeta` JPA 实体 | ✅ | `entity/EntityMeta.java` |
| 3 | `FieldMeta` JPA 实体 | ✅ | `entity/FieldMeta.java` |
| 4 | `DdlLog` 审计实体 | ✅ | `entity/DdlLog.java` |
| 5 | `EntityMetaRepository` | ✅ | `repository/EntityMetaRepository.java` |
| 6 | `FieldMetaRepository` | ✅ | `repository/FieldMetaRepository.java` |
| 7 | `DdlLogRepository` | ✅ | `repository/DdlLogRepository.java` |
| 8 | `EntityMetaDTO` | ✅ | `dto/EntityMetaDTO.java` |
| 9 | `FieldMetaDTO` | ✅ | `dto/FieldMetaDTO.java` |
| 10 | `EntityListRequest` 分页查询请求 | ✅ | `dto/EntityListRequest.java` |
| 11 | `DDLService` — CREATE/ALTER/DROP 建表与安全检查 | ✅ | `service/DDLService.java` |
| 12 | `EntityMetaService` — 实体 CRUD + 发布 + Redis 缓存 | ✅ | `service/EntityMetaService.java` |
| 13 | `EntityMetaController` — `/lowcode/entity/**` REST 接口 | ✅ | `controller/EntityMetaController.java` |
| 14 | `LowCodeConstants` 常量定义 | ✅ | `config/LowCodeConstants.java` |
| 15 | Redis 缓存 (`lc:meta:{entityCode}:fields`) | ✅ | 在 `EntityMetaService` 中实现 |

### 前端

| # | 任务 | 状态 | 文件 |
|---|------|------|------|
| 1 | TypeScript 类型定义 (EntityMeta, FieldMeta, FieldType 等) | ✅ | `types/lowcode.d.ts` |
| 2 | 实体元数据 API 模块 | ✅ | `api/lowcode/entityMeta.ts` |
| 3 | EntityList 页面 (表格 + 发布/归档/删除操作) | ✅ | `pages/lowcode/metadata/EntityList.vue` |
| 4 | EntityEdit 页面 (实体表单 + 可拖拽字段表格) | ✅ | `pages/lowcode/metadata/EntityEdit.vue` |
| 5 | 低代码路由树 (`/lowcode/entity`, `/lowcode/entity/:id`) | ✅ | `router/modules/lowcode.ts` |

### 验证清单

- [ ] 创建"客户"实体 (name VARCHAR, phone VARCHAR, age INTEGER, status VARCHAR)
- [ ] 发布实体 → 验证 `lc_customer` 表在 MySQL 中创建且列正确
- [ ] 编辑实体字段 → 验证 ALTER TABLE 正确执行
- [ ] 归档实体 → 验证状态变为 "archived"
- [ ] 删除草稿实体 → 验证字段级联删除

---

## 阶段 2: 通用 CRUD + 数据管理 ⬜

> 分支: `feature/lowcode-phase-2-generic-crud` (尚未创建)

### 后端 — 计划

| # | 任务 | 状态 |
|---|------|------|
| 1 | `GenericQueryRequest` DTO (page, pageSize, filters, sort) | ⬜ |
| 2 | `GenericPageResult` DTO (records, total, page, pageSize) | ⬜ |
| 3 | `GenericCrudService` — 基于 JdbcTemplate 的动态 SQL | ⬜ |
| 4 | `GenericCrudController` — `/lowcode/generic/{entityCode}/**` | ⬜ |

### 前端 — 计划

| # | 任务 | 状态 |
|---|------|------|
| 1 | `api/lowcode/genericCrud.ts` API 模块 | ⬜ |
| 2 | `hooks/useGenericCrud.ts` 组合式函数 | ⬜ |
| 3 | `pages/lowcode/data/DataManager.vue` — 动态表格 + CRUD | ⬜ |

### 验证清单

- [ ] 通过 GenericCrudController 进行 POST/PUT/DELETE 操作
- [ ] `curl` 测试所有 CRUD 端点
- [ ] DataManager 界面: 增/改/查/删 记录

---

## 阶段 3: 可视化页面设计器 ⬜

> 分支: `feature/lowcode-phase-3-page-designer` (尚未创建)

### 后端 — 计划

| # | 任务 | 状态 |
|---|------|------|
| 1 | `ComponentDef` 实体 + Repository + Controller | ⬜ |
| 2 | `DataInitializer` — 初始化 ~15 个系统组件 | ⬜ |
| 3 | 字典实体 + CRUD (`DictType`, `DictItem`) | ⬜ |

### 前端 — 计划

| # | 任务 | 状态 |
|---|------|------|
| 1 | `api/lowcode/componentDef.ts` | ⬜ |
| 2 | `store/modules/designer.ts` — 组件树状态、选中、撤销/重做 | ⬜ |
| 3 | `components/lowcode/DynamicForm.vue` — JSON Schema → TDesign 表单 | ⬜ |
| 4 | `pages/lowcode/designer/ComponentPanel.vue` — 拖拽源面板 | ⬜ |
| 5 | `pages/lowcode/designer/Canvas.vue` — 画布拖放 + 渲染 | ⬜ |
| 6 | `pages/lowcode/designer/CanvasComponent.vue` — 选中 + 拖拽 | ⬜ |
| 7 | `pages/lowcode/designer/PropertyEditor.vue` — 属性编辑 | ⬜ |
| 8 | `pages/lowcode/designer/DesignerLayout.vue` — 三面板布局 | ⬜ |
| 9 | `pages/lowcode/renderer/components/` — 初始 9 个渲染器组件 | ⬜ |

---

## 阶段 4: 页面 Schema + 运行时渲染 ⬜

> 分支: `feature/lowcode-phase-4-page-schema` (尚未创建)

### 后端 — 计划

| # | 任务 | 状态 |
|---|------|------|
| 1 | `PageSchema` 实体 + Repository + Service + Controller | ⬜ |

### 前端 — 计划

| # | 任务 | 状态 |
|---|------|------|
| 1 | `api/lowcode/pageSchema.ts` | ⬜ |
| 2 | DesignerLayout 中保存/加载功能 | ⬜ |
| 3 | `pages/lowcode/pages/PageList.vue` | ⬜ |
| 4 | `pages/lowcode/renderer/SchemaRenderer.vue` — 运行时渲染器 | ⬜ |
| 5 | `pages/lowcode/renderer/SchemaRendererComponent.vue` | ⬜ |
| 6 | 添加 `/lowcode/view/:pageCode` 路由 | ⬜ |

---

## 阶段 5: 权限接入 + 细节打磨 ⬜

> 分支: `feature/lowcode-phase-5-auth-polish` (尚未创建)

| # | 任务 | 状态 |
|---|------|------|
| 1 | 接入 JWT (`JwtUtil`, `TokenFilter`, `useUserStore`) | ⬜ |
| 2 | 实体/页面的权限控制 | ⬜ |
| 3 | 图表组件 (ECharts 柱状图/折线图/饼图) | ⬜ |
| 4 | 基于 `validationRule` JSON 的表单验证 | ⬜ |
| 5 | 数据表导出/导入 (CSV) | ⬜ |
| 6 | 设计器撤销/重做 | ⬜ |
| 7 | UX 打磨: 加载/空/错误状态、响应式 | ⬜ |

---

## Git 分支状态

```
main
  └── develop (当前分支)
       ├── feature/lowcode-phase-1-entity-metadata ✅ (已合并)
       ├── feature/lowcode-phase-2-generic-crud     ⬜ (未创建)
       ├── feature/lowcode-phase-3-page-designer    ⬜ (未创建)
       ├── feature/lowcode-phase-4-page-schema      ⬜ (未创建)
       └── feature/lowcode-phase-5-auth-polish      ⬜ (未创建)
```

---

## 下一步

开始 **阶段 2: 通用 CRUD + 数据管理**:
```bash
git checkout develop
git checkout -b feature/lowcode-phase-2-generic-crud develop
```
