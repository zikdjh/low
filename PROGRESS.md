# 低代码平台 · 项目进度跟踪

> 文档版本：2026-06-15 · 维护策略：**每完成一项任务实时更新本文档**
> 这是项目的"实时仪表盘"，所有人看这一份就能知道现在做到哪、谁在做、卡在哪。
> 配套文档：`CLAUDE.md`（代码约束）/ `DEVELOPMENT.md`（路线图）/ `LIGHTWEIGHT_SCOPE.md`（范围）/ `FEATURES.md`（手册）

---

## 0. 总览仪表盘

| 维度 | 数值 |
|------|------|
| **当前 Phase** | Phase 1 已完成 ✅ · 准备进入 Phase 2 |
| **当前里程碑** | M1（稳健化）— 未启动 |
| **总进度** | 28 / 73 任务（38%） |
| **P0 红线进度** | 0 / 5（0%） — 🔴 全部待启动 |
| **P1 可用性进度** | 0 / 12（0%） |
| **P2 体验进度** | 0 / 11（0%） |
| **代码行数（业务相关）** | 后端 ~3500 行 / 前端 ~5000 行 |
| **本周更新人** | zc |
| **最近更新时间** | 2026-06-15 |

```
进度条
Phase 1 (基础元数据 + 设计器) ██████████ 100% ✅
Phase 2 (稳健化 / 安全)         ░░░░░░░░░░   0%
Phase 3 (可用化 / 接通后端)     ░░░░░░░░░░   0%
Phase 4 (体验打磨)              ░░░░░░░░░░   0%
```

---

## 1. 里程碑追踪

| 里程碑 | 范围 | 计划周期 | 状态 | 实际起止 |
|-------|------|---------|------|---------|
| **M0** Phase 1 完成 | 元数据 + 设计器 demo | — | ✅ 已完成 | 至 2026-06-15 |
| **M1** 稳健化 | P0-1 ~ P0-5 | Week 1-4（4 周） | 🟡 待启动 | — |
| **M2** 可用化 | P1-1 ~ P1-12 | Week 5-8（4 周） | ⏸️ 阻塞中 | 依赖 M1 |
| **M3** 体验打磨 | P2-1 ~ P2-11 | Week 9-12（4 周） | ⏸️ 阻塞中 | 依赖 M2 |

> 状态图例：✅ 完成 · 🟢 进行中 · 🟡 待启动 · 🔴 阻塞 · ⏸️ 等待依赖 · ❌ 已放弃

---

## 2. Phase 1：已完成 ✅（基线）

### 2.1 后端

| # | 任务 | 关键文件 | 状态 | 备注 |
|---|------|---------|------|------|
| 1.1 | 元数据 7 张表（EntityMeta/FieldMeta/PageSchema/ComponentDef/DictType/DictItem/DdlLog）| `lowcode/entity/*` | ✅ | JPA `ddl-auto: update` |
| 1.2 | 实体生命周期状态机 draft→published→archived | `EntityMetaService` | ✅ | |
| 1.3 | DDLService 唯一 DDL 出口 | `DDLService` | ✅ | 三条不变式都已落实 |
| 1.4 | 字段软删除（RENAME COLUMN __deleted_xxx）| `DDLService.generateAlterTable` | ✅ | |
| 1.5 | DynamicDataService 动态 CRUD | `DynamicDataService` | ✅ | ⚠️ SQL 注入待修 |
| 1.6 | Redis 元数据缓存（TTL 30 天）| `EntityMetaService.cacheFieldMeta` | ✅ | |
| 1.7 | PageSchema CRUD + 发布 | `PageSchemaService` | ✅ | |
| 1.8 | DataInitializer 种子（15 组件 + 3 字典）| `config/DataInitializer` | ✅ | 幂等 |
| 1.9 | 统一响应 Result（code:1 成功）| `common/Result` | ✅ | |
| 1.10 | REST 端点（/lowcode/entity, data, page, component, dict）| `lowcode/controller/*` | ✅ | |

### 2.2 前端

| # | 任务 | 关键文件 | 状态 |
|---|------|---------|------|
| 1.11 | API 封装（5 个资源）| `src/api/lowcode/*` | ✅ |
| 1.12 | 实体管理界面 | `pages/lowcode/metadata/EntityList.vue`, `EntityEdit.vue` | ✅ |
| 1.13 | 通用数据管理界面 | `pages/lowcode/data/DataList.vue` | ✅ |
| 1.14 | 拖拽页面设计器（~2000 行）| `pages/lowcode/page/PageDesigner.vue` | ✅ |
| 1.15 | 设计器状态管理（50 步撤销）| `store/modules/designer.ts` | ✅ |
| 1.16 | 运行时渲染器（3 层硬编码）| `pages/lowcode/SchemaRenderer.vue` | ✅ ⚠️ 需改递归 |
| 1.17 | 15 个 Element 组件 | `pages/lowcode/page/components/*Element.vue` | ✅ |
| 1.18 | 路由（/lowcode/*） | `router/modules/lowcode.ts` | ✅ |

**Phase 1 总结**：能跑通"建实体 → 发布 → 增删改查 → 设计页面 → 预览"全流程，但安全裸奔、组件未接真实数据。

---

## 3. Phase 2：M1 稳健化（**当前阶段**）

### P0 红线任务清单（必须全部完成才进入 M2）

| # | 任务 | 优先级 | 状态 | 负责人 | 预估 | 阻塞 | 关键文件 |
|---|------|------|------|-------|------|------|---------|
| **P0-1** | JWT 拦截器 + 用户/角色/AccessControl 三表 | P0 | 🟡 待启动 | — | 5d | — | 新建 `JwtInterceptor`, `User`, `Role`, `EntityAcl`, `WebConfig` 注册 |
| **P0-2** | DynamicDataService SQL 注入修复 + 测试 | P0 | 🟡 待启动 | — | 3d | — | `DynamicDataService` 重构所有动态 SQL |
| **P0-3** | 实体级 RBAC 拦截（view/edit/delete） | P0 | 🟡 待启动 | — | 3d | P0-1 | `RbacInterceptor` + AOP |
| **P0-4** | DB 密码移出仓库 | P0 | 🟡 待启动 | — | 0.5d | — | `application-local.yaml` + `.gitignore` |
| **P0-5** | DDLService 单元测试 | P0 | 🟡 待启动 | — | 2d | — | `DDLServiceTest` |
| **P1-9** | 默认值类型校验（顺手做） | P1 | 🟡 待启动 | — | 1d | — | `FieldType.validateDefault` |

**M1 完成定义**：

- [ ] 未登录访问 `/lowcode/*` 返回 401
- [ ] 故意传 `'; DROP TABLE x; --` 给 list 接口被拒绝（有测试）
- [ ] 角色 A 没有"客户实体"权限时，调 `/lowcode/data/customer` 返回 403
- [ ] `git ls-files` 不再含明文 DB 密码
- [ ] `mvn test` 至少 10 个测试用例覆盖 DDLService

### 周计划

```
Week 1  [ ] P0-1 JWT 拦截器 + 三表（5d）+ P0-4 配置外置（0.5d）
Week 2  [ ] P0-3 RBAC 拦截（3d）+ P0-5 DDL 测试启动（2d）
Week 3  [ ] P0-2 SQL 注入修复（3d）+ P1-9 默认值校验（1d）+ P0-5 收尾
Week 4  [ ] 集成测试 + 文档更新 + M1 验收
```

---

## 4. Phase 3：M2 可用化（待启动）

| # | 任务 | 优先级 | 状态 | 预估 | 关键文件 |
|---|------|------|------|------|---------|
| P1-1 | REFERENCE 字段（外键） | P1 | ⏸️ | 4d | `FieldType`, `SelectElement`, 前端字段配置 |
| P1-2 | SchemaRenderer 改递归 | P1 | ⏸️ | 1d | `SchemaRenderer.vue` |
| P1-3 | 运行时入口 `/run/:pageCode` | P1 | ⏸️ | 2d | 新建路由 + 页面 |
| P1-4 | TableElement 接通后端 | P1 | ⏸️ | 2d | `TableElement.vue` |
| P1-5 | FormElement 接通后端 | P1 | ⏸️ | 2d | `FormElement.vue` |
| P1-6 | SelectElement 接通字典 | P1 | ⏸️ | 1d | `SelectElement.vue` |
| P1-7 | 事件系统（5 动作 DSL） | P1 | ⏸️ | 3d | `designer.ts` 事件解释器 |
| P1-8 | 页面参数（?id=123 注入） | P1 | ⏸️ | 1d | `SchemaRenderer.vue` context |
| P1-10 | 唯一约束（单字段 unique） | P1 | ⏸️ | 1d | `FieldMeta` + `DDLService` |
| P1-11 | 字段类型变更 → MODIFY COLUMN | P1 | ⏸️ | 2d | `DDLService.generateAlterTable` |
| P1-12 | EntityListRequest 真正生效 | P1 | ⏸️ | 1d | `EntityMetaRepository` 加 spec |

**M2 完成定义**：拖 Table + Button + Form 三个组件，配上 entityCode，不写代码就有一个能用的客户管理页面。

---

## 5. Phase 4：M3 体验打磨（待启动）

| # | 任务 | 优先级 | 状态 | 预估 |
|---|------|------|------|------|
| P2-1 | Excel 导入 / 导出 | P2 | ⏸️ | 3d |
| P2-2 | 文件上传组件 + 后端存储 | P2 | ⏸️ | 3d |
| P2-3 | Tabs / 折叠面板组件 | P2 | ⏸️ | 1d |
| P2-4 | 多条件高级搜索 | P2 | ⏸️ | 2d |
| P2-5 | 批量编辑 | P2 | ⏸️ | 2d |
| P2-6 | 复制 / 粘贴组件 | P2 | ⏸️ | 1d |
| P2-7 | 操作日志（业务数据 CRUD 审计） | P2 | ⏸️ | 2d |
| P2-8 | DDL 回滚 UI | P2 | ⏸️ | 2d |
| P2-9 | PageSchema 乐观锁启用 | P2 | ⏸️ | 0.5d |
| P2-10 | Docker Compose 一键启动 | P2 | ⏸️ | 1d |
| P2-11 | 应用导出 zip | P2 | ⏸️ | 3d |

---

## 6. 风险登记册

| # | 风险 | 等级 | 影响 | 缓解措施 | 状态 |
|---|------|------|------|---------|------|
| R1 | SQL 注入未修，生产暴露后果严重 | 🔴 高 | 数据泄漏/篡改 | P0-2 优先做，做完前不部署对外 | 🟡 已识别 |
| R2 | 无认证机制，接口对外裸奔 | 🔴 高 | 任意越权访问 | P0-1 优先做 | 🟡 已识别 |
| R3 | DB 密码进 git 历史 | 🟡 中 | 凭据泄漏 | P0-4 + 修改密码 + git filter-branch | 🟡 已识别 |
| R4 | `EntityMetaService.updateFields` delete-all+insert-all 让 FieldMeta.id 不稳定 | 🟡 中 | 未来字段级特性断链 | M2 期间改成按 code diff | 🟡 已识别 |
| R5 | SchemaRenderer 3 层硬编码限制真实页面表达 | 🟡 中 | 复杂页面做不出 | P1-2 做完解除 | 🟡 已识别 |
| R6 | 没有任何业务测试 | 🟡 中 | 重构风险高 | P0-5 起步，逐步补 | 🟡 已识别 |
| R7 | Redis 宕机时 `getCachedFields` 是否回源数据库 | 🟢 低 | 短时性能下降 | 已实现回源，监控 Redis 健康 | ✅ 已缓解 |

---

## 7. 已知技术债（不阻塞但要记账）

| # | 描述 | 优先级 | 何时还 |
|---|------|------|--------|
| T1 | `front/src/store/modules/designer.ts` 用了废弃的 `Math.random().toString(36).substr(2, 9)` | 🟢 低 | 顺手改 |
| T2 | `BackApplicationTests` 是空壳 | 🟢 低 | 随 P0-5 一起补 |
| T3 | CORS 允许所有 origin（开发期 OK，生产需收紧） | 🟢 低 | 上线前收紧 |
| T4 | `PathInterceptor` 只打印路径，无实际功能 | 🟢 低 | P0-1 时一起删 |
| T5 | 默认值在 buildCreateTableSql 里 `' '` 直接拼接（注入风险）| 🟡 中 | 与 P0-2 一起 |
| T6 | `application.yaml.show-sql` 上线前要关 | 🟢 低 | 上线前 |

---

## 8. 决策记录（ADR 简版）

| 编号 | 日期 | 决策 | 理由 | 影响 |
|------|------|------|------|------|
| ADR-001 | 2026-06-15 | 项目定位**轻量级**，对标 Retool/Appsmith | 避免范围蔓延 | 详见 `LIGHTWEIGHT_SCOPE.md` 不做清单 |
| ADR-002 | Phase 1 | 元数据平面（JPA）与动态数据平面（JdbcTemplate）严格分离 | 防止 JPA 试图托管用户表 | 不要在用户表加 `@Entity` |
| ADR-003 | Phase 1 | DDLService 是唯一 DDL 出口，所有 DDL 写 lc_ddl_log | 审计 + 回滚能力 | 改用户表必须走 DDLService |
| ADR-004 | Phase 1 | 字段移除使用 RENAME 而非 DROP | 防误删用户数据 | 列名变 `__deleted_xxx` |
| ADR-005 | Phase 1 | Result.code = 1 表示成功（非 HTTP 200） | 与历史代码兼容 | 前端 axios 拦截器依赖此约定 |
| ADR-006 | 2026-06-15 | 不做工作流 / 多租户 / 表达式引擎 | 见 LIGHTWEIGHT_SCOPE.md 第 1 节 | 这些能力将永远不做 |

---

## 9. 变更日志

> **每次更新本文档都要在这里加一行**。格式：`日期 | 变更人 | 摘要`

| 日期 | 变更人 | 摘要 |
|------|-------|------|
| 2026-06-15 | zc | 初始化 PROGRESS.md，登记 Phase 1 完成基线 + Phase 2-4 任务清单 + 风险登记册 |

---

## 10. 如何使用本文档

### 每日

- 开工前看一眼第 0 节"总览仪表盘"和第 3 节"当前阶段"
- 选一个 🟡 待启动的任务，把状态改为 🟢 进行中，写上自己的名字
- 收工时把进度写进任务行的"备注"，必要时更新预估

### 每完成一个任务

1. 把状态改为 ✅
2. 第 0 节进度数字 +1
3. 第 9 节"变更日志"加一行
4. 如果触发了里程碑完成，把对应里程碑改 ✅，并在 Slack/钉钉群通知

### 每周五

- 复盘本周完成的任务（看变更日志）
- 把延期的任务标 🔴 阻塞，写明原因
- 评估下周计划是否需要调整

### 收到新需求时

- 先对照 `LIGHTWEIGHT_SCOPE.md` 第 1 节"不做清单"和第 5 节"范围管控原则"
- 通过的需求加到 P3（可选）行列，不要直接插到 P0/P1/P2

### 识别到新风险时

- 加到第 6 节"风险登记册"，先标 🟡 已识别
- 想清楚缓解措施再标其它状态

---

## 11. 快速链接

- 代码硬约束 → [`CLAUDE.md`](./CLAUDE.md)
- 完整路线图 → [`DEVELOPMENT.md`](./DEVELOPMENT.md)
- 范围裁剪 → [`LIGHTWEIGHT_SCOPE.md`](./LIGHTWEIGHT_SCOPE.md)
- 功能开发手册 → [`FEATURES.md`](./FEATURES.md)
- 后端入口 → `back/src/main/java/com/back/lowcode/`
- 前端入口 → `front/src/pages/lowcode/`
- DDL 审计表 → MySQL `lc_ddl_log`
