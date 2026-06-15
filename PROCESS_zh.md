# 低代码平台 — 进度跟踪

> 最后更新: 2026-06-04
> 基于: [harmonic-wandering-fountain.md](./harmonic-wandering-fountain.md)
> 当前分支: `develop`

---

## 整体进度

| 阶段 | 分支 | 状态 | 进度 |
|------|------|------|------|
| 阶段 1 — 实体元数据引擎 | `feature/lowcode-phase-1-entity-metadata` | ✅ 已合并 | 100% |
| 阶段 2 — 通用 CRUD + 数据管理 | `feature/lowcode-phase-2-generic-crud` | ✅ 已合并 | 100% |
| 阶段 3 — 可视化页面设计器 | `feature/lowcode-phase-3-page-designer` | ✅ 已合并 | 100% |
| 阶段 4 — 页面 Schema + 运行时渲染 | `feature/lowcode-phase-4-page-schema` | ⬜ 未开始 | 0% |
| 阶段 5 — 权限接入 + 细节打磨 | `feature/lowcode-phase-5-auth-polish` | ⬜ 未开始 | 0% |

**总体: 3/5 阶段完成 (60%)**

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

- [x] 创建"客户"实体 (name VARCHAR, phone VARCHAR, age INTEGER, status VARCHAR)
- [x] 发布实体 → 验证 `lc_customer` 表在 MySQL 中创建且列正确
- [x] 编辑实体字段 → 验证 ALTER TABLE 正确执行
- [x] 归档实体 → 验证状态变为 "archived"
- [x] 删除草稿实体 → 验证字段级联删除

---

## 阶段 2: 通用 CRUD + 数据管理 ✅

> 分支: `feature/lowcode-phase-2-generic-crud` → 已合并至 `develop`

### 后端

| # | 任务 | 状态 | 文件 |
|---|------|------|------|
| 1 | `DynamicDataRequest` DTO | ✅ | `dto/DynamicDataRequest.java` |
| 2 | `DynamicDataService` — 基于 JdbcTemplate 的动态 SQL | ✅ | `service/DynamicDataService.java` |
| 3 | `DynamicDataController` — `/lowcode/data/{entityCode}/**` | ✅ | `controller/DynamicDataController.java` |

### 前端

| # | 任务 | 状态 | 文件 |
|---|------|------|------|
| 1 | `api/lowcode/dynamicData.ts` API 模块 | ✅ | `api/lowcode/dynamicData.ts` |
| 2 | `pages/lowcode/data/DataList.vue` — 动态表格 + CRUD | ✅ | `pages/lowcode/data/DataList.vue` |

### 验证清单

- [x] 通过 DynamicDataController 进行 POST/PUT/DELETE 操作
- [x] `curl` 测试所有 CRUD 端点
- [x] DataManager 界面: 增/改/查/删 记录

---

## 阶段 3: 可视化页面设计器 ✅

> 分支: `feature/lowcode-phase-3-page-designer` → 已合并至 `develop`

### 后端

| # | 任务 | 状态 | 文件 |
|---|------|------|------|
| 1 | `ComponentDef` 实体 + Repository + Controller | ✅ | `entity/ComponentDef.java`, `repository/ComponentDefRepository.java`, `controller/ComponentDefController.java` |
| 2 | `DataInitializer` — 初始化 ~15 个系统组件 | ✅ | `config/DataInitializer.java` |
| 3 | 字典实体 + CRUD (`DictType`, `DictItem`) | ✅ | `entity/DictType.java`, `entity/DictItem.java`, `controller/DictController.java` |
| 4 | `PageSchema` 实体 + Repository + Service + Controller | ✅ | `entity/PageSchema.java`, `service/PageSchemaService.java`, `controller/PageSchemaController.java` |

### 前端

| # | 任务 | 状态 | 文件 |
|---|------|------|------|
| 1 | `api/lowcode/componentDef.ts` | ✅ | `api/lowcode/componentDef.ts` |
| 2 | `api/lowcode/pageSchema.ts` | ✅ | `api/lowcode/pageSchema.ts` |
| 3 | `store/modules/designer.ts` — 组件树状态、选中、撤销/重做 | ✅ | `store/modules/designer.ts` |
| 4 | `components/lowcode/DynamicForm.vue` — JSON Schema → TDesign 表单 | ✅ | `components/lowcode/DynamicForm.vue` |
| 5 | `pages/lowcode/designer/ComponentPanel.vue` — 拖拽源面板 | ✅ | `pages/lowcode/designer/ComponentPanel.vue` |
| 6 | `pages/lowcode/designer/Canvas.vue` — 画布拖放 + 渲染 | ✅ | `pages/lowcode/designer/Canvas.vue` |
| 7 | `pages/lowcode/designer/CanvasComponent.vue` — 选中 + 拖拽 | ✅ | `pages/lowcode/designer/CanvasComponent.vue` |
| 8 | `pages/lowcode/designer/PropertyEditor.vue` — 属性编辑 | ✅ | `pages/lowcode/designer/PropertyEditor.vue` |
| 9 | `pages/lowcode/designer/DesignerLayout.vue` — 三面板布局 | ✅ | `pages/lowcode/designer/DesignerLayout.vue` |
| 10 | `pages/lowcode/page/PageDesigner.vue` — 主设计器页面 | ✅ | `pages/lowcode/page/PageDesigner.vue` |
| 11 | `pages/lowcode/page/PageList.vue` — 页面列表管理 | ✅ | `pages/lowcode/page/PageList.vue` |
| 12 | `pages/lowcode/page/components/` — 渲染器组件 | ✅ | `pages/lowcode/page/components/*.vue` |
| 13 | `pages/lowcode/SchemaRenderer.vue` — 运行时渲染器 | ✅ | `pages/lowcode/SchemaRenderer.vue` |

### 验证清单

- [x] 打开设计器 → 拖入组件 → 配置属性 → 画布实时预览
- [x] 属性编辑器根据 propsSchemaJson 正确渲染表单
- [x] 保存页面 → 验证 `lc_page_schema` 记录存在

---

## 阶段 4: 页面 Schema + 运行时渲染 ⬜

> 分支: `feature/lowcode-phase-4-page-schema` (尚未创建)

### 后端 — 计划

| # | 任务 | 状态 |
|---|------|------|
| 1 | 页面发布功能完善 | ⬜ |

### 前端 — 计划

| # | 任务 | 状态 |
|---|------|------|
| 1 | 添加 `/lowcode/view/:pageCode` 路由 | ⬜ |
| 2 | SchemaRenderer 从后端加载页面 Schema | ⬜ |

### 验证清单

- [ ] 设计 → 保存 → 发布 → 查看由真实数据生成的实时页面

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
| 6 | 设计器撤销/重做 | ✅ (已实现) |
| 7 | UX 打磨: 加载/空/错误状态、响应式 | ⬜ |

---

## Git 分支状态

```
main
  └── develop (当前分支)
       ├── feature/lowcode-phase-1-entity-metadata ✅ (已合并)
       ├── feature/lowcode-phase-2-generic-crud     ✅ (已合并)
       ├── feature/lowcode-phase-3-page-designer    ✅ (已合并)
       ├── feature/lowcode-phase-4-page-schema      ⬜ (未创建)
       └── feature/lowcode-phase-5-auth-polish      ⬜ (未创建)
```

---

## 下一步

开始 **阶段 4: 页面 Schema + 运行时渲染**:
```bash
git checkout develop
git checkout -b feature/lowcode-phase-4-page-schema develop
```
