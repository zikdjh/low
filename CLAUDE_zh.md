# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 仓库结构

一个仓库内两个独立项目：

- `back/` — Spring Boot 3.5.14 / Java 17 / Maven 后端（端口 **8080**）
- `front/` — Vue 3 + TypeScript + Vite 前端（开发端口 **5174**）

前端开发服务器将 `/api/*` 代理到 `http://0.0.0.0:8080/*`，并去掉 `/api` 前缀（见 `front/vite.config.ts`）。后端本身**并不**把 controller 挂在 `/api` 下，该前缀只是前端约定。

## 常用命令

### 后端（`back/`）

```bash
# 在 back/ 目录下
./mvnw spring-boot:run          # 启动开发服务器
./mvnw.cmd spring-boot:run      # Windows / cmd
./mvnw test                     # 跑全部测试
./mvnw test -Dtest=ClassName    # 跑单个测试类
./mvnw clean package            # 打 jar
```

需要本机 MySQL 在 `127.0.0.1:3306` 上有 `low_end` 库，以及 Redis 在 `localhost:6379`。账号密码硬编码在 `back/src/main/resources/application.yaml`（`root` / `zc@qq.com0501`）——本地修改即可，不要提交。`spring.jpa.hibernate.ddl-auto: update` 会在启动时自动建/更新元数据表的结构。

### 前端（`front/`）

```bash
# 在 front/ 目录下
npm install
npm run dev                     # vite 开发服务器，端口 5174
npm run build                   # vue-tsc + vite build
npm run preview                 # 预览打包产物
```

任何低代码功能都需要后端已启动；前端会把所有 `/api` 请求代理到后端。

## 架构

这是一个**低代码平台**：终端用户在界面上定义业务实体，后端为它们创建真实的 MySQL 表，再用一个可视化页面设计器在这些实体上拼装页面。

### 两个截然不同的"数据层"

读代码时必须区分这两套数据平面，不要混在一起：

1. **元数据平面** —— `back/src/main/java/com/back/lowcode/entity/` 下的 JPA 实体（`EntityMeta`、`FieldMeta`、`PageSchema`、`ComponentDef`、`DictType`、`DictItem`、`DdlLog`）。存在固定的 `lc_*` 前缀表里，由 Spring Data JPA 仓库管理。
2. **动态数据平面** —— 用户自定义的业务表。**没有任何 JPA 实体类**；统一通过 `DynamicDataService` 里的 `JdbcTemplate` / `NamedParameterJdbcTemplate` 访问，列名由请求时查询到的 `FieldMeta` 行驱动。

修改低代码相关代码时，绝不能把这两层混用：不要给用户数据加 `@Entity`，也不要绕开 `FieldMeta` 直接读用户表。

### 实体生命周期

`EntityMeta.status` 是一个严格的状态机，由 `EntityMetaService` 强制约束：

- `draft` —— 只有元数据，没有物理表。可编辑、可删除。
- `published` —— 物理表已经通过 `DDLService.generateCreateTable` 创建。字段编辑走 `DDLService.generateAlterTable`（仅做加法：新增字段 → `ADD COLUMN`；被删除的字段是**软删除**，重命名为 `__deleted_*`，永远不会 `DROP`）。`DynamicDataController` 仅在状态为 `published` 时接受 CRUD。
- `archived` —— 只读。

`DynamicDataService` 的每个方法都会用 `published` 状态做兜底校验——新增操作时要保留这个守卫。

### DDL 安全契约

`DDLService` 是唯一发出 DDL 的入口，它强制三条不变量，改代码时不要破坏：

1. 操作的表名必须以 `LowCodeConstants.TABLE_PREFIX`（`lc_`）开头，否则直接抛异常。
2. 列名必须匹配 `^[a-zA-Z][a-zA-Z0-9_]*$` 且不能是 MySQL 保留字（`MySQLReservedWords.isReserved`）。同样的校验在 `EntityMetaService` 里对实体编码 / 字段编码也重复做了一遍。
3. 每一条执行过的语句都会写入 `lc_ddl_log`（`DdlLog`），无论成功失败。

`FieldType`（枚举）是元数据和 SQL 之间的桥梁——它知道每种逻辑类型对应什么 MySQL 列类型，以及如何拼出 `VARCHAR(n)` / `DECIMAL(p,s)`。新增字段类型在这里加。

### REST 接口

所有低代码 controller 都挂在 `/lowcode/*` 下：

- `/lowcode/entity` —— 实体和字段元数据 CRUD；`POST /{id}/publish`、`POST /{id}/archive`
- `/lowcode/data/{entityCode}` —— 已发布实体的动态 CRUD
- `/lowcode/page` —— `PageSchema`（可视化设计器输出的 JSON 存在 `layout_json` 字段）
- `/lowcode/component` —— `ComponentDef` 组件面板项（首次启动由 `DataInitializer` 写入）
- `/lowcode/dict` —— 字典（同样由 `DataInitializer` 写入）

所有响应都用 `com.back.common.Result` 包裹，**约定与常见的不同：`code: 1` 表示成功，`code: 0` 表示失败**（不是 HTTP 风格的 200/500）。前端 `front/src/api/index.ts` 里的 axios 拦截器依赖这一约定。

### 缓存

`EntityMetaService` 会把已发布实体的字段元数据写到 Redis，键为 `lc:meta:{code}:fields`，TTL 30 天；缓存未命中时在 `getCachedFields` 中重新填充。任何修改已发布实体 `FieldMeta` 的代码路径之后，都必须调用 `cacheFieldMeta`（或 `invalidateCache`），否则从 Redis 读到的 schema 会变陈旧。

### 鉴权

`back/src/main/java/com/back/interceptors/PathInterceptor.java` 目前只打印一行请求路径——尽管引入了 `java-jwt` 依赖，前端 axios 拦截器也带了 `access` 请求头，**实际上后端并没有真实的鉴权拦截器接进来**。当前可以认为后端接口是不鉴权的；如果要加鉴权，记得在 `WebConfig` 里注册新拦截器。

`WebConfig` 里的 CORS 允许所有来源带凭证——这是为了适配开发期动态域名，刻意放开的。

### 前端结构

- `src/api/lowcode/*` —— 带类型的 axios 封装，每个后端资源一个文件（`entityMeta`、`dynamicData`、`pageSchema`、`componentDef`、`dict`）。
- `src/pages/lowcode/`
  - `metadata/` —— `EntityList.vue`、`EntityEdit.vue`（实体 + 字段设计器）
  - `data/DataList.vue` —— 由 `FieldMeta` 驱动的通用 CRUD 页面
  - `page/PageDesigner.vue` + `designer/` —— 拖拽式页面构建器。设计器状态（组件树、选中项、撤销历史）在 `src/store/modules/designer.ts`（Pinia）。运行时渲染器是 `src/pages/lowcode/SchemaRenderer.vue`。
- `src/router/modules/lowcode.ts` —— 上述页面的路由。
- UI 组件用 TDesign Vue Next（`tdesign-vue-next`）；图标来自 `tdesign-icons-vue-next`。

### 数据初始化

`DataInitializer` 是一个 `CommandLineRunner`，首次启动时写入组件面板和三个默认字典（`status`、`gender`、`yes_no`）——通过 `count() > 0` 判空保证幂等。如果想重新初始化，清空 `lc_component_def` / `lc_dict_type` / `lc_dict_item` 即可。
