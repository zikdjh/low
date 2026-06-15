# 低代码平台开发文档

> 文档版本：2026-06-15 · 适用分支：`develop` / `feature/lowcode-phase-1-entity-metadata`
> 阅读前请先看 `CLAUDE.md`，本文档是它的延伸：`CLAUDE.md` 讲"现在的代码是怎么组织的"，本文档讲"已完成多少、还要做什么、下一步怎么做"。

---

## 1. 项目定位

一个**自托管的轻量级低代码平台**：

1. 用户在前端可视化定义业务实体（如"客户"、"订单"）和它们的字段。
2. 后端把元数据落到 `lc_*` 元表，发布时**真实地**在 MySQL 中创建物理表。
3. 已发布实体自动获得通用列表/表单 CRUD 页面。
4. 用户可在拖拽式页面设计器中组合组件，输出一份 JSON Schema，由前端运行时渲染器消费。

技术栈：

| 层 | 技术 | 端口 |
|----|------|------|
| 后端 | Spring Boot 3.5.14 + JPA + JdbcTemplate + Redis | 8080 |
| 前端 | Vue 3 + Vite + TypeScript + Pinia + TDesign | 5174 (dev) |
| 存储 | MySQL 8（数据库 `low_end`） + Redis（元数据缓存） |  |

---

## 2. 当前进度（Phase 1：实体元数据 + 基础页面设计）

### 2.1 已完成 ✅

#### 后端

- **元数据模型** — `EntityMeta` / `FieldMeta` / `PageSchema` / `ComponentDef` / `DictType` / `DictItem` / `DdlLog` 七张元表，由 JPA `ddl-auto: update` 自动建表。
- **实体生命周期** — `draft → published → archived` 三态机，由 `EntityMetaService` 强制约束（仅草稿可编辑，已发布不可删，归档只读）。
- **动态 DDL** — `DDLService` 是项目中**唯一**会发出 DDL 的入口，落实了三条不变式：
  1. 表名必须以 `lc_` 前缀开头；
  2. 列名必须匹配 `^[a-zA-Z][a-zA-Z0-9_]*$` 且非 MySQL 保留字（`MySQLReservedWords`）；
  3. 每条执行过的 DDL 都写入 `lc_ddl_log`，含成功/失败、错误信息。
- **字段变更软删除** — 字段被移除时使用 `RENAME COLUMN` 改名为 `__deleted_xxx`，从不 `DROP`，避免误删用户数据。
- **动态数据 CRUD** — `DynamicDataService` 通过 `JdbcTemplate` / `NamedParameterJdbcTemplate` 操作用户表，所有方法都校验 `published` 状态；列表支持按字段 `LIKE` 过滤、分页。
- **Redis 元数据缓存** — 已发布实体的字段元数据写入 `lc:meta:{code}:fields`，TTL 30 天，未命中时回源数据库并回填。
- **页面 Schema** — `PageSchema` 表 + `PageSchemaService` 提供 CRUD、发布/取消发布；`layoutJson` 是组件树的序列化结果。
- **组件定义 + 数据字典种子** — `DataInitializer` 在首次启动时初始化 15 个内置组件（基础/表单/布局/容器/数据共 5 类）和 3 个内置字典（`status`/`gender`/`yes_no`），幂等。
- **统一响应** — `com.back.common.Result`，**注意：`code: 1 = 成功`，`code: 0 = 失败`**，与 HTTP 200/500 完全无关，前端 axios 拦截器依赖此约定。
- **REST 端点** — 全部挂在 `/lowcode/*`：

  | 路径 | 说明 |
  |------|------|
  | `/lowcode/entity` | 实体 + 字段元数据 CRUD、`POST /{id}/publish`、`POST /{id}/archive` |
  | `/lowcode/data/{entityCode}` | 已发布实体的动态数据 CRUD + 批量删除 |
  | `/lowcode/page` | 页面 Schema CRUD + 发布 |
  | `/lowcode/component` | 组件元数据 |
  | `/lowcode/dict` | 字典 |

#### 前端

- **API 封装** — `src/api/lowcode/` 下每个资源一个 axios 包装文件，类型定义在 `src/types/lowcode.d.ts`。
- **实体管理界面** — `pages/lowcode/metadata/EntityList.vue`、`EntityEdit.vue`，支持创建/编辑/发布/归档/删除、字段拖拽排序、按字段类型显示长度/精度配置。
- **通用数据管理界面** — `pages/lowcode/data/DataList.vue`，按 `FieldMeta` 自动生成表头、查询条件、新增/编辑表单。
- **页面设计器** — `pages/lowcode/page/PageDesigner.vue`（约 2000 行），左侧组件面板、中央拖拽画布、右侧属性编辑器；支持 Ctrl+Z/Y 撤销重做（`store/modules/designer.ts` 维护 50 步 history）。
- **运行时渲染器** — `SchemaRenderer.vue`，从 `sessionStorage.previewLayout` 读取组件树并递归渲染，目前支持 3 层嵌套。
- **组件元素** — `pages/lowcode/page/components/` 下 15 个 `*Element.vue`，对应 `DataInitializer` 中的 15 个 `compKey`。
- **路由** — `src/router/modules/lowcode.ts` 在 `/lowcode/*` 下注册了实体、数据、页面设计四个页面。

### 2.2 已知缺陷 / 技术债 ⚠️

按"必须修"→"建议修"排序：

1. **🔴 SQL 注入风险（高优先级）** — `DynamicDataService.listData` 把 `field.getColumnName()` 直接字符串拼接进 `WHERE`/`ORDER BY`。虽然列名经过 `^[a-zA-Z]...` 校验，但 `entity.getTableName()` 同样是直接拼接，并依赖了 `lc_` 前缀校验。**所有动态 SQL 应改为：把表名/列名做白名单校验后再用反引号包起来**，参考 `DDLService.buildCreateTableSql` 的写法。
2. **🔴 鉴权缺失** — `PathInterceptor` 只打印路径不做认证；前端 axios 拦截器读 `access` header 但后端没人验。`pom.xml` 已经引入了 `java-jwt`，需要在 `WebConfig` 中注册一个真正的 JWT 拦截器。当前所有 `/lowcode/*` 接口对外裸奔。
3. **🟡 默认值未做类型校验** — `FieldMeta.defaultValue` 在 `buildCreateTableSql` 里直接用 `' '` 包起来当字符串拼到 DDL，对 `INTEGER`/`BOOLEAN`/`DATETIME` 类字段会产生不合法 SQL；并且默认值本身没做转义，可被注入。
4. **🟡 `EntityMetaService.updateFields` 用 delete-all + insert-all 替换字段** — 每次保存都会让 `FieldMeta.id` 变化；如果未来要做"按字段 id 关联表单组件"，会断链。建议改为按 `code` 做 diff（add/update/remove）。
5. **🟡 `DDLService.generateAlterTable` 不处理"字段类型变更"** — 现在只识别 `add` / `remove`，对相同 `code` 的字段如果 `fieldType`/`length` 改了，**不会**发 `MODIFY COLUMN`。
6. **🟡 `EntityListRequest` 的 `keyword` / `status` / `sortBy` / `sortOrder` 参数前端在传，后端的 `listEntities` 直接忽略**，只做了简单分页。
7. **🟡 `SchemaRenderer.vue` 把递归写死成三层** — 4 层及以上嵌套就渲染不出来，应改为递归组件。
8. **🟢 `application.yaml` 把数据库密码硬编码进了仓库** — 改成 `${DB_PASSWORD}` + 本地 `.env`，提交前 `git rm --cached`。
9. **🟢 `Math.random().toString(36).substr(2, 9)`** — `substr` 已废弃，应换成 `slice`。
10. **🟢 后端没有任何业务测试** — `BackApplicationTests` 是空壳；`DDLService` 这种安全敏感的服务必须补单元测试（合法/非法表名、保留字、字段 diff）。

---

## 3. 整体架构图

```
┌─────────────────────────────────────────────────────────────┐
│                      浏览器 (Vue 3)                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐  │
│  │ 实体管理     │  │ 数据管理     │  │ 页面设计器       │  │
│  │ EntityList   │  │ DataList     │  │ PageDesigner     │  │
│  └──────────────┘  └──────────────┘  └──────────────────┘  │
│                       │                       │              │
│                       ▼                       ▼              │
│              ┌─────────────────┐  ┌──────────────────┐      │
│              │ designer store  │  │ SchemaRenderer   │      │
│              └─────────────────┘  └──────────────────┘      │
└────────────────────────┬────────────────────────────────────┘
                         │  /api/* (vite 代理 → :8080)
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                  Spring Boot (port 8080)                     │
│                                                              │
│  ┌─────────────────────────┐    ┌───────────────────────┐   │
│  │  元数据平面 (JPA)       │    │  动态数据平面 (JDBC)  │   │
│  │  EntityMeta/FieldMeta   │    │  DynamicDataService   │   │
│  │  PageSchema/Dict/...    │    │  (无 @Entity)         │   │
│  └─────────────────────────┘    └───────────────────────┘   │
│            │                              │                  │
│            │   ┌────────────────────┐     │                  │
│            └──►│  DDLService        │◄────┘                  │
│                │  唯一 DDL 出口      │                        │
│                │  → lc_ddl_log      │                        │
│                └────────────────────┘                        │
│                                                              │
│  ┌─────────────────────────────────────────────────────┐    │
│  │  Redis  lc:meta:{code}:fields  (TTL 30d)            │    │
│  └─────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────┘
                         │
                         ▼
                  ┌──────────────┐
                  │   MySQL      │
                  │ ┌──────────┐ │
                  │ │ lc_*     │ │ ◄ 元表（JPA 管）
                  │ │ lc_xxx   │ │ ◄ 用户表（DDLService 建）
                  │ └──────────┘ │
                  └──────────────┘
```

**核心设计纪律**：

- 元数据平面与动态数据平面**永不交叉** — 不要给用户表加 `@Entity`，不要绕过 `FieldMeta` 直接读用户表。
- 任何修改用户表结构的代码**必须**走 `DDLService`，便于 `lc_ddl_log` 审计。
- 任何修改已发布实体字段的代码**必须**调用 `cacheFieldMeta` 或 `invalidateCache`，否则 Redis 里是脏数据。

---

## 4. 后续开发路线

按重要性 + 依赖关系排，分四个 Phase。每个 Phase 的"完成定义"明确写出来，避免口头扯皮。

### Phase 2：稳健性与权限（必做，预计 1-2 周）

> 目标：把当前 demo 级代码变成"敢给同事用"的工程级代码。

| # | 任务 | 关键文件 | 完成定义 |
|---|------|---------|---------|
| 2.1 | 用 PreparedStatement 重构 `DynamicDataService` 全部 SQL | `DynamicDataService.java` | 表名/列名走白名单校验后反引号包裹；值全部走 `?` 占位符；写一个单元测试故意传 `'; DROP TABLE x; --` 验证拒绝 |
| 2.2 | 加 JWT 拦截器 | 新建 `JwtInterceptor`，注册到 `WebConfig` | 未带 token 访问 `/lowcode/*` 返回 401；前端 axios 401 时跳登录 |
| 2.3 | 默认值类型校验 | `EntityMetaService.updateFields` + `FieldType` 增加 `validateDefault(String)` | 给 INTEGER 字段填 "abc" 在保存时被拒 |
| 2.4 | 字段 diff 改为按 code 关联 | `EntityMetaService.updateFields` | 同名字段保存后 `id` 不变，且支持识别 `fieldType` 变更并发出 `MODIFY COLUMN` |
| 2.5 | 给 `DDLService` 补单元测试 | `back/src/test/java/.../DDLServiceTest.java` | 覆盖：保留字拒绝、非法前缀拒绝、create/alter SQL 文本 snapshot |
| 2.6 | 把数据库密码移出仓库 | `application.yaml`, 新建 `application-local.yaml` | `git ls-files` 不再含明文密码；README 加本地配置说明 |
| 2.7 | `EntityListRequest` 的 keyword / status / sortBy 真正生效 | `EntityMetaRepository`, `EntityMetaService.listEntities` | 前端搜索框能用 |

### Phase 3：页面运行时与数据绑定（核心增量，预计 2-3 周）

> 目标：让"设计的页面"能"跑起来"，用户拖一个 Table 配上 entityCode 就能看见真实数据。

| # | 任务 | 说明 |
|---|------|------|
| 3.1 | `SchemaRenderer` 改为递归 | 当前硬编码 3 层；用 `<RecursiveRenderer :node="comp" />` 解决任意深度 |
| 3.2 | TableElement 接通后端 | 当前的 TableElement 只展示 mock；需要在 props 里读 `entityCode` 后调 `dynamicDataApi.list()` |
| 3.3 | FormElement 接通后端 | 表单根据 `entityCode` 自动从 `getByCode` 拉字段，按 `showInForm` 过滤；提交时调 `create`/`update` |
| 3.4 | SelectElement 接通字典 | props 里 `dictCode` 不为空时调 `dictApi.getItems(dictCode)` 拉选项 |
| 3.5 | 事件系统 | `ComponentInstance.events` 当前只有类型定义没实现；至少支持 `onClick → openPage(pageCode)` 与 `onSubmit → callApi(...)` |
| 3.6 | 页面运行入口 | 新增路由 `/run/:pageCode`，把已发布的 `PageSchema.layoutJson` 喂给递归渲染器；与设计器的 `预览` 区分（预览走 sessionStorage，运行走后端） |
| 3.7 | 字段校验 | `FieldMeta.validationRule` JSON 已经在表里，前端表单要消费它（required/min/max/pattern）|

### Phase 4：扩展与体验（增强，预计持续迭代）

- **关联字段**（外键引用其它实体） — 新增 `FieldType.REFERENCE`，前端 SelectElement 支持"按实体取值"。
- **导入导出** — 实体定义 + 数据 → JSON / Excel 互转。
- **多人协作** — `PageSchema` 加乐观锁 `version` 字段（已有 column 但未启用 `@Version`）。
- **审计 / 回滚** — `lc_ddl_log` 已经记录了所有 DDL，做一个"回滚"按钮，根据 `__deleted_xxx` 列表恢复字段。
- **Docker 一键启动** — 写 `docker-compose.yml` 把 MySQL + Redis + 后端 + 前端打包，本地起项目不再需要手装中间件。

### Phase 5：插件化（远期）

- 组件市场：允许第三方注册 `compKey`，前端按需加载组件 JS；后端 `lc_component_def` 已支持 `isSystem=false`，但前端没有"运行时加载远程组件"的机制。
- 自定义字段类型：把 `FieldType` 从枚举改成一张配置表，让用户能注册"邮箱"、"手机号"、"地理坐标"这类业务类型。

---

## 5. 给后续开发者的提示

读代码时，把 `CLAUDE.md` 第 "Architecture / DDL safety contract / Caching" 三节当成硬约束 — 它们都是因为已经踩过坑而写出来的：

1. **不要在动态数据里加 `@Entity`** — 一旦加了，JPA 会试图给用户表生成 schema，与 `DDLService` 的预期产生冲突，会破坏 `ddl-auto: update` 行为。
2. **不要绕过 `DDLService` 写 `ALTER` / `DROP`** — `lc_ddl_log` 的审计能力靠这个唯一入口实现。
3. **改任何 `FieldMeta` 的代码都要想"已发布实体的 Redis 缓存怎么办"** — 漏一个 `cacheFieldMeta` 会让 `getCachedFields` 返回过时 schema，CRUD 就会写错列。
4. **`Result.code` 不是 HTTP 码** — `1` 成功 `0` 失败，给新人搭手时一定先讲清楚，否则前端 `if (res.code === 200)` 永远走错分支。
5. **改前端别忘了 `vite.config.ts` 的 `/api` 代理会把前缀剥掉** — 后端 controller 不需要 `@RequestMapping("/api/...")`。

新增 `FieldType` 的检查清单：
- [ ] `FieldType` 枚举里加一项，正确填 `mysqlType` / `needsLength` / `needsPrecision`
- [ ] `DynamicDataService.convertValue` 加对应的 case
- [ ] 前端 `types/lowcode.d.ts` 的 `FieldType` 联合类型补上
- [ ] `EntityEdit.vue` 字段类型下拉框补上选项
- [ ] `DataList.vue` 表单输入控件按新类型选用合适的 TDesign 组件

---

## 6. 启动 / 调试速查

```bash
# 后端（要求 MySQL@127.0.0.1:3306/low_end + Redis@6379 已起）
cd back
./mvnw spring-boot:run

# 前端
cd front
npm install
npm run dev          # http://localhost:5174
```

调试动态 SQL：把 `application.yaml` 里 `spring.jpa.show-sql` 临时改 `true`，或者直接看 `lc_ddl_log` 表的 `sql_statement` 列。
