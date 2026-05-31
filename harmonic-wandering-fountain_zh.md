# 低代码平台实施方案

## 背景

基于现有的 Spring Boot 3.5.14 + Vue 3/TDesign 代码库（目前是一个博客平台脚手架，包含认证、Redis、JPA 和主题基础设施），构建一个面向**管理后台/CRUD 管理系统**的低代码平台。平台使用**动态数据模型**（用户在运行时定义实体结构，平台自动生成 DDL 和通用 CRUD）。MVP 交付**可视化页面设计器** + **数据管理（CRUD）**。

---

## GitFlow 分支策略

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

## 架构概览

```
┌─────────────────────────────────────────────────────────────┐
│  前端 (Vue 3 + TDesign + Vite 8, 端口 5174)                 │
│                                                             │
│  ┌──────────────────┐  ┌────────────┐  ┌────────────────┐  │
│  │ 设计器 (三面板)    │  │ Schema     │  │ 元数据/数据    │  │
│  │ 组件面板           │  │ 渲染器     │  │ 管理页面       │  │
│  │ 画布               │  │ (运行时)   │  │                │  │
│  │ 属性编辑器         │  │            │  │                │  │
│  └──────────────────┘  └────────────┘  └────────────────┘  │
└──────────────────────────┬──────────────────────────────────┘
                           │ /api → 代理 → 端口 8080
┌──────────────────────────┴──────────────────────────────────┐
│  后端 (Spring Boot 3.5.14, Java 21, MySQL + Redis)          │
│                                                             │
│  com.back.lowcode/                                          │
│  ├── entity/     EntityMeta, FieldMeta, PageSchema,         │
│  │               ComponentDef, DictType, DictItem           │
│  ├── enums/      FieldType                                  │
│  ├── dto/        请求/响应 DTO                               │
│  ├── repository/ Spring Data JPA 仓库                       │
│  ├── service/    EntityMetaService, DDLService,             │
│  │               GenericCrudService, PageSchemaService      │
│  ├── controller/ REST 控制器                                 │
│  ├── config/     LowCodeConstants                           │
│  └── util/       DDL 构建工具                               │
│                                                             │
│  复用: Result, RedisUtil, GlobalExceptionHandler,           │
│         Sql*Exception, RateLimit 注解, JacksonConfig         │
└─────────────────────────────────────────────────────────────┘
```

---

## 核心数据模型（数据库表）

| 表名 | 用途 |
|------|------|
| `lc_entity_meta` | 实体定义（编码、名称、表名、状态） |
| `lc_field_meta` | 字段定义（外键关联实体、字段类型、长度、可空、列表/表单/搜索显示、校验规则 JSON） |
| `lc_page_schema` | 页面设计（layoutJson LONGTEXT、页面类型、版本、状态） |
| `lc_component_def` | 组件注册表（compKey、分类、默认属性 JSON、属性 Schema JSON） |
| `lc_dict_type` | 字典类型定义 |
| `lc_dict_item` | 字典项（标签、值、排序、颜色） |
| `lc_ddl_log` | DDL 审计日志 |
| `lc_{entity_code}` | **动态**用户创建的表（如 `lc_customer`、`lc_order`）— 不由 JPA 管理，通过 JdbcTemplate 访问 |

---

## 关键技术决策

### 1. 动态 DDL 安全
- 所有动态表必须以 `lc_` 为前缀；DDLService 拒绝操作不带此前缀的表。
- 列名与元数据缓存交叉验证；绝不直接用原始用户输入。
- DDL 包含在事务中执行；通过 `lc_ddl_log` 进行审计日志记录。
- 绝不自动删除列；重命名为 `__deleted_xxx` 作为宽限期。
- 对比新旧字段元数据，仅生成必要的 ALTER 语句。

### 2. 基于 JdbcTemplate 的通用 CRUD
- `GenericCrudService` 基于缓存的字段元数据构建动态 SQL。
- 列名始终从 `lc_field_meta` 缓存中获取（Redis，键 `lc:meta:{entityCode}`）。
- 所有值通过 `NamedParameterJdbcTemplate` 参数化。
- 支持操作符：`__like`、`__gte`、`__lte`、`__between`、`__in`。
- 自动类型转换：`FieldType` → Java 类型（INTEGER→int, DATETIME→LocalDateTime 等）。

### 3. 画布：内联渲染（非 Iframe）
- 设计器在同一 DOM 中渲染真实 Vue 组件。
- 组件接受 `designMode: boolean`（通过 `provide`/`inject` 注入）。
- 设计模式下：使用模拟/示例数据，点击选中而非触达真实业务逻辑。
- 优点：共享 TDesign 主题/CSS，无跨上下文复杂性，自然 Vue 响应性。

### 4. 属性编辑器：JSON Schema 驱动
- 每个 `ComponentDef.propsSchemaJson` 使用简化 JSON Schema + `ui:widget` 提示描述可编辑属性。
- `DynamicForm.vue` 解析 Schema 并渲染 TDesign 表单控件。
- 高级用户逃生舱：原始 JSON 编辑器标签页。

### 5. API 约定
- 所有端点位于 `/lowcode/` 命名空间下。
- 所有响应使用现有 `Result` 包装器（`{code, msg, data}`，1=成功，0=错误）。
- 修复前端 `ResponseType` 使用 `msg`（而非 `message`）以匹配后端。

---

## 实施阶段

### 阶段 1: 实体元数据引擎（基础）

> **分支**: `feature/lowcode-phase-1-entity-metadata` → `develop`

**后端** — `com.back.lowcode.*`:
1. `enums/FieldType.java` — 枚举，包含 `mysqlType`、`javaType`、`needsLength`、`needsPrecision`
2. `entity/EntityMeta.java` + `entity/FieldMeta.java` — JPA 实体
3. `repository/EntityMetaRepository.java` + `repository/FieldMetaRepository.java`
4. `dto/` — EntityMetaDTO、FieldMetaDTO、EntityListRequest
5. `service/DDLService.java` — 使用 JdbcTemplate 的 `generateCreateTable(EntityMeta, List<FieldMeta>)`，前缀检查，审计日志
6. `service/EntityMetaService.java` — CRUD + 调用 DDLService 的 `publishEntity()`
7. `controller/EntityMetaController.java` — `/lowcode/entity/**`
8. Redis 缓存：使用 `RedisUtil.saveJsonToRedisHash` 存储字段元数据，键 `lc:meta:{entityCode}`

**前端**:
1. `types/lowcode.d.ts` — EntityMeta、FieldMeta、FieldType 接口
2. `api/lowcode/entityMeta.ts` — API 调用
3. `pages/lowcode/metadata/EntityList.vue` — 含发布/删除操作的表格
4. `pages/lowcode/metadata/EntityEdit.vue` — 编辑实体 + 可拖拽字段列表构建器
5. `router/modules/lowcode.ts` — 低代码路由树

**交付标准**: 创建实体 → 定义字段 → 发布 → 物理 MySQL 表存在。

### 阶段 2: 通用 CRUD + 数据管理

> **分支**: `feature/lowcode-phase-2-generic-crud` → `develop`

**后端**:
1. `dto/GenericQueryRequest.java` + `dto/GenericPageResult.java`
2. `service/GenericCrudService.java` — 动态 SQL：查询（分页+过滤+排序）、新增、更新、删除、按ID获取
3. `controller/GenericCrudController.java` — `/lowcode/generic/{entityCode}/**`

**前端**:
1. `api/lowcode/genericCrud.ts`
2. `hooks/useGenericCrud.ts` — 含 fetch/create/update/remove 的组合式函数
3. `pages/lowcode/data/DataManager.vue` — 含搜索栏、工具栏、增/改对话框、批量删除的动态表格

**交付标准**: 导航至 `/lowcode/data/{entityCode}` → 全功能 CRUD 表格，支持服务端分页、搜索、排序。

### 阶段 3: 可视化页面设计器

> **分支**: `feature/lowcode-phase-3-page-designer` → `develop`

**后端**:
1. `entity/ComponentDef.java` + Repository + Controller
2. `config/DataInitializer.java` — 初始化约 15 个系统组件（page-header、search-bar、data-table、form-*、card、row/col、chart-* 等）
3. 字典实体 + CRUD（`DictType`、`DictItem`）

**前端**:
1. `api/lowcode/componentDef.ts`
2. `store/modules/designer.ts` — 组件树状态、选中、撤销/重做、拖拽状态
3. `components/lowcode/DynamicForm.vue` — JSON Schema → TDesign 表单（可复用）
4. `pages/lowcode/designer/ComponentPanel.vue` — 基于 HTML5 拖放的分组拖拽面板
5. `pages/lowcode/designer/Canvas.vue` — 拖放目标、递归组件渲染、缩放控制
6. `pages/lowcode/designer/CanvasComponent.vue` — 选中边框、拖拽手柄、删除按钮
7. `pages/lowcode/designer/PropertyEditor.vue` — DynamicForm + 组件树大纲 + 原始 JSON 标签页
8. `pages/lowcode/designer/DesignerLayout.vue` — 三面板组合布局
9. `pages/lowcode/renderer/components/` — 初始 9 个支持 designMode 的渲染器组件

**交付标准**: 拖拽组件 → 配置属性 → 在画布中实时预览。

### 阶段 4: 页面 Schema + 运行时渲染

> **分支**: `feature/lowcode-phase-4-page-schema` → `develop`

**后端**:
1. `entity/PageSchema.java` + Repository + Service + Controller

**前端**:
1. `api/lowcode/pageSchema.ts`
2. DesignerLayout 中的保存/加载（持久化到后端）
3. `pages/lowcode/pages/PageList.vue` — 管理已保存页面
4. `pages/lowcode/renderer/SchemaRenderer.vue` — 运行时渲染器（designMode=false）
5. `pages/lowcode/renderer/SchemaRendererComponent.vue` — 递归运行时组件
6. 添加 `/lowcode/view/:pageCode` 路由

**交付标准**: 设计 → 保存 → 发布 → 查看由真实数据生成的实时页面。

### 阶段 5: 权限接入 + 细节打磨

> **分支**: `feature/lowcode-phase-5-auth-polish` → `develop` → `release/lowcode-v1.0` → `main`

1. 接入 JWT：`JwtUtil`、`TokenFilter`，完善 `useUserStore`
2. 实体/页面的权限控制
3. 图表组件（基于现有 `echarts` 依赖的 ECharts 柱状图/折线图/饼图）
4. 基于 `validationRule` JSON 的表单校验
5. 数据表导出/导入（CSV）
6. 设计器撤销/重做
7. UX 打磨：加载/空/错误状态、响应式适配

---

## 可复用文件

| 现有文件 | 复用用途 |
|----------|----------|
| `com.back.common.Result` | 所有 API 响应 |
| `com.back.utils.RedisUtil` | 缓存实体元数据、DDL 审计日志 |
| `com.back.exception.GlobalExceptionHandler` | 错误处理（已捕获 Exception，可添加 Sql*Exception 抛出） |
| `com.back.exception.SqlInsertException/SqlUpdateException/SqlDeleteException` | GenericCrudService 在数据库失败时抛出 |
| `com.back.annotation.RateLimit` | 接入通用 CRUD 端点 |
| `com.back.config.basic.JacksonConfig`（`endObjectMapper`） | RedisUtil 中的序列化和 JSON 处理 |
| `com.back.config.WebConfig` | 扩展 CORS 并添加 TokenFilter 到拦截器链 |
| `front/src/api/index.ts`（Axios 实例） | 所有低代码 API 模块复用此实例 |
| `front/src/store/modules/setting.ts` | 主题应用于设计器和渲染页面 |
| `front/src/layouts/index.vue` | 所有低代码页面的布局包装器 |
| `front/src/components/color/ColorPicker.vue` | 在设计器属性编辑器中用于颜色类型属性 |

---

## 验证方案

1. **阶段 1**: 创建"客户"实体（name VARCHAR, phone VARCHAR, age INTEGER, status VARCHAR）→ 发布 → 验证 `lc_customer` 表在 MySQL 中存在且列正确。
2. **阶段 2**: 通过 GenericCrudController 进行 POST/PUT/DELETE 操作 → `curl` 测试所有 CRUD 端点 → 验证 MySQL 中数据正确。然后使用 DataManager 界面增/改/查/删记录。
3. **阶段 3**: 打开设计器 → 拖入"数据表格" + "搜索栏" → 选择实体 → 配置列 → 验证画布显示正确的模拟布局。检查属性编辑器表单根据 propsSchemaJson 正确渲染。
4. **阶段 4**: 保存页面 → 验证 `lc_page_schema` 记录存在 → 发布 → 导航至 `/lowcode/view/customer-list` → 验证实时数据表格通过 API 渲染真实数据。
5. **阶段 5**: 登录流程 → Token 在 sessionStorage 中 → 访问受保护端点 → 499 时刷新 Token → 验证设计器撤销/重做功能正常。
