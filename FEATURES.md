# 低代码平台 — 功能开发手册

> 文档版本：2026-06-15
> 配套阅读：`CLAUDE.md`（架构约束）、`DEVELOPMENT.md`（路线图与进度）
> 本文档专注于"**做一个功能**要写哪些代码、改哪些文件、踩哪些坑"。每个功能都按"接入位置 → 后端改动 → 前端改动 → 联调要点 → 验收标准"组织，可作为 PR 模板使用。

---

## 0. 功能开发通用流程

读完 `CLAUDE.md` 后，**所有低代码功能**都遵循同一套套路：

```
① 元数据 / Schema 改不改？
        │
        ├─ 改 → JPA 实体 + Repository + Service（元数据平面）
        │       └─ 涉及已发布实体？ → 调 cacheFieldMeta / invalidateCache
        │
        └─ 不改 → 跳过

② 数据库结构动不动？
        │
        ├─ 动用户表 → 必须走 DDLService（唯一出口，写 lc_ddl_log）
        ├─ 动元表   → 改 @Entity 字段，靠 ddl-auto: update 自动迁移
        └─ 不动     → 跳过

③ Controller 暴露端点
        │
        └─ 路径前缀 /lowcode/<resource>，返回 Result（code=1 成功 / 0 失败）

④ 前端类型定义（src/types/lowcode.d.ts）
        │
⑤ 前端 API 包装（src/api/lowcode/<resource>.ts）
        │
⑥ 前端页面 / 组件改动
        │
⑦ 路由（src/router/modules/lowcode.ts）— 如有新页面
```

每个步骤可独立提交，便于 review。

---

## 1. 已建成功能 — 用法速查

下表是**当前已经能用的**功能，列出"用什么场景去做什么"。

| 功能 | 入口 | 后端服务 | 前端页面 |
|------|------|---------|---------|
| 实体定义 + 物理建表 | `/lowcode/entity` | `EntityMetaService` + `DDLService` | `metadata/EntityList.vue` + `EntityEdit.vue` |
| 实体字段管理 | `/lowcode/entity/{id}/fields` | `EntityMetaService.updateFields` | `EntityEdit.vue` 字段表 |
| 实体发布 / 归档 | `/lowcode/entity/{id}/publish` `/{id}/archive` | `EntityMetaService.publishEntity` | `EntityList.vue` 操作列 |
| 通用数据 CRUD | `/lowcode/data/{entityCode}` | `DynamicDataService` | `data/DataList.vue` |
| 数据字典 | `/lowcode/dict` | `DictService` | （无独立页面，由组件消费） |
| 组件元数据 | `/lowcode/component` | `ComponentDefService` | 设计器左栏 |
| 页面 Schema CRUD | `/lowcode/page` | `PageSchemaService` | `page/PageDesigner.vue` |
| 拖拽设计器 | 设计器画布 | — | `designer/Canvas.vue` 等 |
| 设计器撤销重做 | Ctrl+Z / Ctrl+Y | — | `store/modules/designer.ts`（50 步 history）|
| 渲染器（仅预览） | `sessionStorage.previewLayout` | — | `SchemaRenderer.vue`（**仅 3 层嵌套**） |
| Redis 字段元数据缓存 | 自动 | `lc:meta:{code}:fields` 30d TTL | — |

---

## 2. 待开发功能 — 详细规格

按依赖关系排：表头 → 内容 → 高级。每个功能配上"**为什么需要它**"、"**接入位置**"、"**实现要点**"、"**验收标准**"四块。

---

### 2.1 字段类型扩展：REFERENCE（实体引用）

> 别名：外键字段。让一个实体的字段可以"指向"另一个已发布实体的某条记录。

**为什么需要它** — 没有外键，实体之间是孤立的；订单不能关联客户，权限不能关联角色。这是从"单表 demo"走向"业务系统"的最小必要功能。

**接入位置**

- 后端：`FieldType` 枚举 + `DDLService.buildCreateTableSql`、`FieldMeta` 新增列
- 前端：`EntityEdit.vue` 字段配置弹窗 + `DataList.vue` 表单的 Select 控件

**实现要点**

1. `FieldMeta` 增加：
   ```java
   /** 引用实体编码（FieldType=REFERENCE 时必填） */
   @Column(length = 64)
   private String refEntityCode;

   /** 引用实体的显示字段编码，用于下拉显示文本（默认取主键） */
   @Column(length = 64)
   private String refDisplayCode;
   ```
2. `FieldType.REFERENCE` 落到 MySQL 是 `BIGINT`（指向被引用表的主键）。
3. `EntityMetaService.updateFields` 校验：`refEntityCode` 必须存在且 `status='published'`，否则发布会失败。
4. **不**做物理外键（FOREIGN KEY），保留软引用 — 否则 `dropTable` / 软删除字段 / 归档实体都会被外键阻塞。
5. 前端 `DataList.vue` 表单遇到 REFERENCE 字段时调 `dynamicDataApi.list(refEntityCode, {pageSize:1000})` 拉候选项。
6. 列表展示时把 `refEntityCode` 的目标值用 `getDataById` 查回展示，**单页用 Map 缓存避免 N+1**。

**验收标准**
- 创建"订单"实体含 `customerId : REFERENCE → customer` 字段，发布后 `lc_order` 表里 `customer_id BIGINT NULL`。
- `DataList` 表单中 customerId 显示为下拉，选项来自 `lc_customer` 真实数据。
- 列表页能显示客户名而非裸 ID。

---

### 2.2 SchemaRenderer 改为递归组件

**为什么需要它** — 当前 `SchemaRenderer.vue` 把渲染硬编码到 3 层，第 4 层及以上的组件直接被吞掉。Form 套 Grid 套 Card 套 Input 这种结构非常普通。

**接入位置** — `pages/lowcode/SchemaRenderer.vue` 重写。

**实现要点**

新建 `RecursiveNode.vue`：

```vue
<template>
  <component
    :is="getRenderComponent(node.compKey)"
    v-bind="getMergedProps(node.props)"
    :style="node.style"
  >
    <RecursiveNode
      v-for="child in node.children"
      :key="child.id"
      :node="child"
    />
  </component>
</template>
<script setup lang="ts">
import RecursiveNode from './RecursiveNode.vue'; // 自引用
defineProps<{ node: ComponentInstance }>();
</script>
```

`SchemaRenderer.vue` 只负责拿 schema、循环根节点、把每个根丢给 `RecursiveNode`。

**验收标准**
- 设计器搭一个 5 层嵌套的页面，预览页面所有层级都正确渲染、属性透传无误。
- `Canvas.vue` / `CanvasComponent.vue` 也应该用同一份递归组件，避免设计器和运行时不一致。

---

### 2.3 TableElement 接通后端数据

**为什么需要它** — 当前 `TableElement.vue` 只展示假数据。低代码平台最常见的需求是"列表页 = Table 绑实体"，这个不通就没法做业务页面。

**接入位置**

- 前端：`pages/lowcode/page/components/TableElement.vue`
- 复用：`api/lowcode/dynamicData.ts`、`api/lowcode/entityMeta.ts`

**实现要点**

```vue
<script setup lang="ts">
import { ref, watch, onMounted } from 'vue';
import dynamicDataApi from '@/api/lowcode/dynamicData';
import entityMetaApi from '@/api/lowcode/entityMeta';

const props = defineProps<{
  entityCode?: string;
  pageSize?: number;
  columns?: string[]; // 可选：限制显示列；空则用 showInList 字段
}>();

const fields = ref([]);
const records = ref([]);
const total = ref(0);
const pagination = ref({ current: 1, pageSize: props.pageSize ?? 10 });

async function loadFields() {
  if (!props.entityCode) return;
  const res = await entityMetaApi.getByCode(props.entityCode);
  fields.value = res.data.fields.filter(f => f.showInList);
}

async function loadData() {
  if (!props.entityCode) return;
  const res = await dynamicDataApi.list(props.entityCode, pagination.value);
  records.value = res.data.content;
  total.value = res.data.totalElements;
}

watch(() => props.entityCode, async () => {
  await loadFields();
  await loadData();
}, { immediate: true });
</script>
```

**易踩坑**
- 设计器中没填 `entityCode` 时不能崩；显示一个"请绑定实体"占位。
- 后端 `Page` 序列化字段是 `content` / `totalElements`（Spring Data 标准），不是 `records` / `total`。

**验收标准**
- 设计器拖一个 Table，属性面板填 `entityCode=customer`，画布上立刻显示真实客户列表。
- 翻页、按字段排序（点表头）能用。

---

### 2.4 FormElement 自动绑定实体表单

**为什么需要它** — Table 解决了"展示"，Form 解决"录入"。两个加在一起就能撑起 80% 的业务 CRUD 页面。

**接入位置** — `pages/lowcode/page/components/FormElement.vue`

**实现要点**

1. props：`entityCode`、`mode: 'create' | 'edit'`、`recordId?`
2. `mode='edit'` 时调 `dynamicDataApi.get(entityCode, recordId)` 拉初始值
3. 按 `FieldMeta.fieldType` 自动选控件：
   | fieldType | 控件 |
   |-----------|------|
   | VARCHAR / TEXT | `t-input` / `t-textarea` |
   | INTEGER / LONG / DOUBLE / DECIMAL | `t-input-number` |
   | BOOLEAN | `t-switch` |
   | DATE | `t-date-picker` |
   | DATETIME | `t-date-picker` mode='date'+time |
   | JSON | `t-textarea`（带 JSON 校验） |
   | REFERENCE | `t-select` 远程加载 |
   | 有 `dictCode` | `t-select` 用字典项覆盖默认控件 |
4. 提交 → `mode='create'` 调 `create`，`mode='edit'` 调 `update`，成功后 `emit('submitted', data)`
5. 消费 `FieldMeta.validationRule` JSON：解析为 TDesign 的 `rules`

**验收标准**
- 设计器拖 Form 配 `entityCode=customer`，运行时能创建客户、编辑客户。
- 必填字段未填时表单红字提示，提交按钮不发请求。

---

### 2.5 SelectElement 接通字典

**为什么需要它** — 性别、状态、是否这类小集合数据，字典已经在后端建好，前端 Select 必须能直接用。

**接入位置** — `pages/lowcode/page/components/SelectElement.vue` + `api/lowcode/dict.ts`

**实现要点**

```vue
<script setup lang="ts">
const props = defineProps<{ dictCode?: string; options?: any[]; modelValue?: any }>();
const items = ref([]);
watch(() => props.dictCode, async (code) => {
  if (!code) { items.value = props.options ?? []; return; }
  const res = await dictApi.getItems(code);
  items.value = res.data.map(i => ({ label: i.itemValue, value: i.itemKey }));
}, { immediate: true });
</script>
```

注意：`DictItem.itemKey`/`itemValue` 与前端 Select 的 `value`/`label` 命名不同，要做映射。如果有 `color`，可以渲染成带色标签。

**验收标准** — 配 `dictCode=status`，下拉显示"启用 / 禁用 / 草稿 / 归档"四项，选中保存的是 `active`/`inactive`/...

---

### 2.6 事件系统：onClick / onSubmit 联动

**为什么需要它** — 静态页面没意义。"点这个按钮去那个页面" / "提交后刷新这个表" 是低代码的灵魂。

**接入位置**

- 类型：`types/lowcode.d.ts` 的 `ComponentInstance.events`（已有空壳）
- 渲染：递归渲染器要把 events 翻译成 v-on
- 设计器：`PropertyEditor.vue` 增加事件配置面板

**事件 DSL（最小集）**

```ts
type EventAction =
  | { action: 'navigate'; pageCode: string; params?: Record<string, any> }
  | { action: 'callApi'; entityCode: string; op: 'create' | 'update' | 'delete'; data?: any }
  | { action: 'refresh'; targetId: string }            // 让某个 Table 重新拉数据
  | { action: 'setValue'; targetId: string; value: any }; // 改某个组件的值
```

**实现要点**

1. 渲染器在 `getMergedProps` 里把 `events` 翻译为 `onClick={() => execAction(...)}`
2. `execAction` 集中实现，内部发事件总线（mitt）让 `targetId` 的组件感知
3. `refresh` 通过 `eventBus.emit('refresh:' + targetId)`，TableElement 监听该事件
4. 设计器的 PropertyEditor 增加"事件"标签页，下拉选 action + 联动配置项

**验收标准**
- 一个页面：上方 Form 提交后下方 Table 自动刷新。
- 一个 ButtonElement 点击跳转到另一个已发布页面，路径 `/run/:pageCode`。

---

### 2.7 页面运行时入口 `/run/:pageCode`

**为什么需要它** — 当前预览靠 sessionStorage，关浏览器就丢；终端用户没办法访问已发布的页面。

**接入位置**

- 路由：`router/modules/lowcode.ts` 增加 `/run/:pageCode`
- 视图：`pages/lowcode/RunView.vue`（新建）
- 后端：复用 `/lowcode/page/code/{pageCode}`

**实现要点**

```ts
// RunView.vue
const route = useRoute();
const schema = ref<any>(null);
onMounted(async () => {
  const res = await pageSchemaApi.getByCode(route.params.pageCode);
  if (res.data.status !== 'published') {
    Message.warning('页面尚未发布');
    return;
  }
  schema.value = JSON.parse(res.data.layoutJson);
});
```

模板中循环 `schema` 用递归渲染器渲染。

**验收标准**
- 浏览器直开 `/run/customer-list` 看到客户列表页。
- 未发布的 pageCode 显示提示。
- 不存在的 pageCode 返回 404 而非白屏。

---

### 2.8 字段级权限（行/列脱敏）

**为什么需要它** — 业务系统几乎一定有"普通员工看不到客户手机号"这种需求。低代码平台需要能配出来。

**接入位置**

- 元表新增 `lc_field_acl` (`field_id`, `role_code`, `permission` ENUM('read','hidden','masked'))
- 后端：`DynamicDataService` 在 `convertRowToData` 里按当前用户角色过滤字段
- 前端：表头按权限隐藏列

**实现要点**

1. 依赖 Phase 2 的 JWT 拦截器先落地（无登录态就没法做权限）
2. `convertRowToData` 对 `permission='hidden'` 的字段不放进结果
3. `permission='masked'` 调 `mask(value)` 工具方法（默认中间打 *）
4. 设计器/管理界面增加"权限矩阵"配置 UI（晚做也行，可先用 SQL 直接配）

**验收标准** — 用 admin 登录看到全部字段；用 guest 登录看不到 phone 字段或看到 `138****1234`。

---

### 2.9 实体导入导出

**为什么需要它** — 实体定义在不同环境之间迁移、做 demo 时灌种子数据，都需要这个。

**接入位置**

- 后端：新增 `EntityMetaController` 下的 `/export/{id}` 与 `/import` 端点
- 前端：`EntityList.vue` 操作列加"导出"按钮、列表头加"导入"按钮

**导出格式**

```json
{
  "entity": { "code": "customer", "name": "客户", "description": "..." },
  "fields": [ { "code": "name", "fieldType": "VARCHAR", "length": 64, ... }, ... ],
  "exportedAt": "2026-06-15T...",
  "version": 1
}
```

**实现要点**
- 导出**只导元数据，不导数据**（数据另起一个 endpoint）
- 导入时 `code` 已存在 → 报错而非覆盖（避免误操作）
- 导入后状态强制为 `draft`，让用户检查再发布

**验收标准** — 导出 JSON → 在另一个环境导入 → 发布 → 物理表与原环境结构一致。

---

### 2.10 操作日志 / 审计

**为什么需要它** — 元数据被改坏时需要追溯。`lc_ddl_log` 已经记录了 DDL，但元数据 CRUD 没记录。

**接入位置**

- 元表新增 `lc_audit_log` (`user_id`, `action`, `target_type`, `target_id`, `before_json`, `after_json`, `ts`)
- 切面：新建 `@Audited` 注解 + AOP 切面，作用于所有 `*Service` 的写方法
- 前端：管理面板新增 `/lowcode/audit` 页面查询

**验收标准** — 编辑实体名称后能在 audit 表查到 before/after diff。

---

### 2.11 实体级生命周期钩子

**为什么需要它** — "客户被创建时自动发欢迎邮件"、"订单删除前检查没有未付款"——业务规则总会出现。

**接入位置**

- 元表 `lc_entity_hook` (`entity_code`, `event` ENUM('beforeCreate','afterCreate','beforeUpdate',...), `script_type` ENUM('webhook','sql'), `payload`)
- 后端：`DynamicDataService` 每个 CRUD 方法前后扫钩子并执行
- `webhook`：HTTP POST 把数据发到配置的 URL
- `sql`：执行预定义 SQL（参数 `:row.xxx` 占位）

**实现要点** — 钩子失败要可配置：是否阻断主流程？建议默认 `afterXxx` 异步、不阻断；`beforeXxx` 同步、阻断。

**验收标准** — 给 customer 配一个 `afterCreate` webhook，新增客户时能在 webhook 服务器收到 POST。

---

### 2.12 Dashboard / 多 Tab 页面布局

**为什么需要它** — 目前 PageType 已经预留 `dashboard`，但渲染器没有"网格容器"。

**接入位置**

- 新建组件 `DashboardElement.vue`，内部用 GridStack 或 vue-grid-layout
- ChartElement 接通后端：`/lowcode/data/{entityCode}/agg?groupBy=xxx&metric=count`（这要后端新增聚合查询服务）

**实现要点** — 聚合查询要单独一个 `DynamicAggService`，**不要**和 `DynamicDataService` 混在一起；聚合 SQL 同样有列名注入风险，列必须白名单。

**验收标准** — 拖 4 张 Chart 拼一个仪表板，每张图分别绑定不同的实体/统计维度。

---

## 3. 跨功能横切 — 必须遵守的工程约定

### 3.1 后端

- **DDL 必须走 `DDLService`**：增加任何"会改用户表结构"的功能，都要在 `DDLService` 里加方法，不许在其它地方写 `ALTER TABLE`。
- **缓存联动**：动 `FieldMeta` 后必须 `cacheFieldMeta` / `invalidateCache`。新写的 Service 引入 `EntityMetaService` 来调，不要自己拼 Redis key。
- **Result 包装**：所有 Controller 返回 `Result.success(data)` / `Result.error(msg)`，不要返回裸对象。
- **状态校验**：操作动态数据前先查 `entity.getStatus()`，不是 `published` 就抛异常。
- **DTO**：跨层数据用 `dto/` 下的 record/类，不要把 JPA `@Entity` 直接吐到前端（虽然现在有些地方这么做，新代码不要再加）。
- **事务边界**：写操作加 `@Transactional`；只读不需要。
- **异常**：业务异常用 `IllegalArgumentException` / `IllegalStateException`，由 `GlobalExceptionHandler` 统一翻译为 `Result.error`。

### 3.2 前端

- **API 文件按资源拆**：新加资源 `xxx` → `src/api/lowcode/xxx.ts`，导出一个对象，方法名用 `list`/`get`/`create`/`update`/`delete`/`publish` 等动词。
- **类型必须先定义**：先在 `types/lowcode.d.ts` 加接口，再在 API 包装里用，最后在组件里 `import type`。
- **设计器与运行时复用渲染器**：递归组件做出来后，`Canvas.vue` 也应该用它，避免两份渲染逻辑漂移。
- **组件元素与 compKey 一一对应**：新组件类型 → 后端 `DataInitializer` 加种子 + 前端 `pages/lowcode/page/components/` 加 Element 文件 + `SchemaRenderer.componentMap` 注册。
- **Pinia store 只放跨组件状态**：单组件状态用 `ref` 即可；不要把所有东西都塞 store。
- **避免 `Math.random().toString(36).substr(...)`**：`substr` 已废弃，用 `slice`。生成 id 推荐用 `crypto.randomUUID()`。

---

## 4. 新组件类型开发模板

以新增一个 "ImageUpload" 组件为例：

### 后端
```java
// DataInitializer.initComponents() 添加一项
createComponent("imageUpload", "图片上传", "表单", "image",
    "图片上传组件",
    "{\"label\":\"图片\",\"accept\":\"image/*\",\"maxSize\":2}",
    "{\"type\":\"object\",\"properties\":{...}}",
    6
);
```

需要 reseed：`TRUNCATE lc_component_def`，重启后端。

### 前端

1. 新建 `pages/lowcode/page/components/ImageUploadElement.vue`
2. 注册到 `SchemaRenderer.componentMap` 和（如果设计器单独维护映射）`Canvas.vue` 的 map
3. 设计器组件面板会从后端拉 `componentDef` 自动出现，不用手写菜单项
4. 如果有特殊属性面板（如裁剪比例），在 `PropertyEditor.vue` 增加分支

### 验收
- 组件出现在设计器左栏"表单"分组下，可拖到画布
- 可在属性面板配置 label / accept / maxSize
- 运行时能上传图片（需要后端有 `/upload` 端点 — 这是另一个独立功能）

---

## 5. 新字段类型开发模板

以新增 "EMAIL"（带格式校验的字符串）为例：

```java
// FieldType.java
EMAIL("VARCHAR", "String", true, false, false) {
    @Override
    public String defaultLength() { return "255"; }
    @Override
    public void validateValue(Object v) {
        if (v != null && !v.toString().matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$"))
            throw new IllegalArgumentException("非法邮箱: " + v);
    }
}
```

> 注：当前 `FieldType` 是简单枚举，要支持 `validateValue` 需要把它改成"枚举 + 子类方法"或单独抽 `FieldTypeStrategy`。这是 Phase 3 推荐的小重构。

清单：
- [ ] `FieldType` 加枚举值
- [ ] `DynamicDataService.convertValue` 加 case（EMAIL 直接当 String）
- [ ] `types/lowcode.d.ts` 的 `FieldType` 联合类型加 `'EMAIL'`
- [ ] `EntityEdit.vue` 字段类型下拉加选项
- [ ] `DataList.vue` / FormElement 表单按 EMAIL 用专门的 input + 即时校验

---

## 6. 联调常见问题

| 现象 | 原因 | 解决 |
|------|------|------|
| 前端永远拿不到数据，但 Network 显示 200 | `Result.code` 不是 200，是 1。axios 拦截器判断错了 | 看 `front/src/api/index.ts`，确认按 `code === 1` 判断成功 |
| 发布实体报"非法列名" | 字段 code 用了 MySQL 保留字（`order` / `key` / `desc` 等） | 换名 |
| 改了字段但 CRUD 还按老 schema | Redis 缓存未失效 | Service 写完字段后调 `cacheFieldMeta` |
| `ddl-auto: update` 没改元表结构 | JPA 对已存在列的修改保守，不会删/改列类型 | 手动 SQL 或在本地 `drop database low_end; create database low_end;` 重来 |
| 前端代理报 ECONNREFUSED | 后端没起 / 端口不是 8080 | 看 `vite.config.ts` 的 proxy 配置 |
| CORS 错误 | 自定义了 CORS 中间件覆盖了 `WebConfig` | `WebConfig` 已 `allowedOrigins=*, allowCredentials=true`，新代码别另起一份 |
| 设计器拖出来的组件不显示 | `compKey` 没在 `SchemaRenderer.componentMap` 注册 | 检查映射 |

---

## 7. 测试建议

当前测试覆盖几乎为零（只有空 `BackApplicationTests`）。新功能必须配测试，建议覆盖：

- **`DDLService`** — DDL 字符串 snapshot 测试 + 注入用例（保留字、特殊字符、`lc_` 前缀绕过尝试）
- **`EntityMetaService`** — 状态机迁移：draft→published、published→draft（应拒绝）、archived 编辑（应拒绝）
- **`DynamicDataService`** — convertValue 类型转换；filters 注入用例
- **前端** — 至少给 `designer` store 写单测：addComponent / undo / redo

后端测试用 `@DataJpaTest` + 内嵌 H2，不要依赖本地 MySQL。

---

## 8. 提 PR 模板

```
### 功能描述
（一句话讲清"做什么"，不讲"怎么做"）

### 涉及文件
- 后端：xxx
- 前端：xxx
- 元表变更：是 / 否（如果是，列出新增列 / 表）
- 物理表变更：是 / 否（如果是，要走 DDLService 吗？是否补 lc_ddl_log？）

### 测试
- [ ] 后端单元测试
- [ ] 手动验证步骤（列出来）

### 兼容性
- 是否影响已发布实体？（影响 → 必须有缓存失效逻辑）
- 是否破坏旧的 layoutJson？（破坏 → 需要迁移脚本）
```
