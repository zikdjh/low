# CLAUDE_zh.md

本文件为 Claude Code (claude.ai/code) 在此仓库中工作时提供指导。

## 项目概览

一个前后端分离的全栈 Web 应用：

- **`back/`** — Spring Boot 3.5.14 REST API，Java 21，Maven，MySQL + Redis
- **`front/`** — Vue 3 + TypeScript SPA，Vite 8，TDesign Vue Next 组件库

## 构建与运行

### 后端（需要 JDK 21、MySQL、Redis）

```bash
# 开发运行（Maven wrapper）
cd back && ./mvnw spring-boot:run     # Windows: mvnw.cmd spring-boot:run

# 运行全部测试
./mvnw test

# 运行单个测试类
./mvnw test -Dtest=BackApplicationTests

# 打包 JAR
./mvnw clean package -DskipTests
```

服务启动端口为 **8080**。需要 MySQL 数据库 `low_end` 和 Redis（localhost:6379）。
配置文件：`back/src/main/resources/application.yaml`。

### 前端（需要 Node.js）

```bash
cd front
npm install
npm run dev       # 启动开发服务器，端口 5174
npm run build     # 类型检查 + 生产构建
npm run preview   # 预览生产构建
```

Vite 开发服务器将 `/api` 代理到 `http://0.0.0.0:8080`（重写时去除 `/api` 前缀）。

## 架构

### 后端（`com.back`）

扁平化按功能分包结构（项目早期阶段——尚无 controller/service/repository）：

| 包名 | 用途 |
|------|------|
| `common/` | `Result` — 统一 API 响应 `{code, msg, data}`（1=成功，0=失败）。`Constant` — 线程池参数、令牌类型常量（`access`/`refresh`）。`RedisConstant` — Redis 扫描默认值。 |
| `config/` | `WebConfig` — 全局注册 `PathInterceptor` 并配置 CORS（允许所有来源，支持凭证）。`basic/JacksonConfig` — 自定义 `ObjectMapper` Bean（`endObjectMapper`），注册 JavaTimeModule，禁用时间戳序列化，忽略未知属性。`basic/ThreadPoolConfig` — `deleteDataExecutor` Bean，带 `@PreDestroy` 优雅关闭。 |
| `interceptors/` | `PathInterceptor` — 通过 SLF4J 记录每个请求路径，始终返回 `true`。 |
| `annotation/` | `@RateLimit` — 方法级注解，用于基于 Redis 的限流（支持前缀、时间窗口、次数限制、自定义提示语）。尚未接入 AOP/拦截器。 |
| `exception/` | `GlobalExceptionHandler`（@RestControllerAdvice）— 捕获 `Exception`、`NoResourceFoundException`（404）、`HandlerMethodValidationException`、`MissingServletRequestParameterException`、`RejectedExecutionException`、`HttpRequestMethodNotSupportedException`。自定义异常 `SqlInsertException`/`SqlUpdateException`/`SqlDeleteException` 继承 `RuntimeException`。 |
| `utils/` | `RedisUtil` — Redis Hash 增删改查的静态工具类，基于 Jackson 序列化。支持：JSON 写入 Hash、Hash 读取为对象、Pipeline 批量操作、基于 SCAN 的键扫描（避免 `KEYS *`）、对象与 Map 互转（含嵌套 JSON 处理）。所有方法需显式传入 `StringRedisTemplate` 和 `ObjectMapper`。 |

**依赖**：Spring Boot Web、Spring Data JPA（MySQL，HikariCP 连接池，最大 30 连接）、Spring Data Redis（Lettuce，连接池最大 10）、Validation、java-jwt 4.5.1、Lombok。

### 前端

**路由** — `router/index.ts` 通过 `import.meta.glob` 自动发现路由模块：
- `./modules/**/home.ts` — 主路由（当前：`/` → 重定向到 `/home`，使用 `Layout` 布局）
- `./modules/**/Login.ts` — 登录路由（`/login`，导航菜单中隐藏）

**状态管理** — Pinia 3 + `pinia-plugin-persistedstate`：
- `useSettingStore` — 布局模式（`light`/`dark`/`auto`）、侧边栏模式、品牌主题色。通过 `tvision-color` 生成 TDesign 色阶。持久化到 localStorage。
- `useMarkdownEditorStore` — 编辑器/预览/代码主题。持久化。
- `useCropperStore` — 图片裁剪弹窗状态，提供基于 Promise 的 `open()`/`confirm()`/`cancel()` API。不持久化。
- `useUserStore` — 空桩（定义在 `modules/user.ts`，文件实际为空）。

**API 层**（`api/index.ts`）— Axios 实例，基础路径 `/api`：
- **认证**：Access Token 存储在 `sessionStorage` 中，键为 `access`，格式 `{token, expiresAt}`（45 分钟过期）。在需要鉴权的写操作请求中作为 `access` 请求头发送。
- **白名单**：硬编码的 `writeList` 数组，列出跳过鉴权的只读接口路径（文章 GET、评论 GET、标签 GET、登录、手机验证码、Token 刷新）。
- **Token 刷新**：收到 HTTP 499 响应时，将并发的失败请求排队，通过 `PATCH /user/refresh/access` 仅刷新一次 Access Token。刷新成功后用新 Token 重试所有排队请求。
- **用户 API**（`api/user/index.ts`）：`getPhoneCode`、`login`、`getEmailCode`、`emailBind`、`updateUserName`、`updateUserAvatar`、`updateUserSelfIntroduction`、`getUserInfo`、`getUserInfoPersonal`、`refreshToken`。

**UI 框架**：TDesign Vue Next（v1.20）+ `tdesign-icons-vue-next`。登录页使用 TDesign 表单组件及自定义校验。主题系统使用 CSS 自定义属性（`--td-brand-color`、`--td-bg-color-container` 等）。

**关键依赖**：`md-editor-v3`（Markdown 编辑器）、`vue-cropper`（图片裁剪）、`echarts` + `tvision-color`（图表与配色）、`katex` + `highlight.js` + `mermaid`（内容渲染）、`@speechmatics/browser-audio-input` + `@tdesign-vue-next/chat`（AI 助手）。

## 认证流程

1. 用户提交手机号 → `POST /api/end/user/code/phone` 发送短信验证码
2. 用户提交手机号 + 验证码 → `POST /api/end/user/login` 返回 Access Token
3. Access Token 存入 `sessionStorage`，写操作请求中作为 `access` 请求头发送
4. Refresh Token 为 HttpOnly Cookie（由浏览器自动携带）
5. 任意请求收到 499 状态码时 → 使用 HttpOnly Cookie 调用 `PATCH /end/user/refresh/access` → 获取新的 Access Token
6. 刷新期间的并发请求会被排队，刷新完成后重试一次

## 代码规范

- **后端**使用 Lombok 注解（`@Data`、`@AllArgsConstructor`、`@NoArgsConstructor`、`@Slf4j`、`@RequiredArgsConstructor`）。当前处于脚手架阶段，尚无 `@Service`/`@Repository`/`@Controller` 类。
- **Result 包装器**：所有 API 响应应使用 `Result.success(data)` 或 `Result.error(msg)`。`code=1` 表示成功，`code=0` 表示失败。
- **Redis 访问**：统一使用 `RedisUtil` 静态方法；显式传入 `StringRedisTemplate` 和 `ObjectMapper`（Bean 名称为 `endObjectMapper`）。使用 SCAN 替代 KEYS。
- **前端类型**：API 模型类型定义在 `api/model/`，共享接口定义在 `types/interface.d.ts`。路由元数据使用 `types/interface.d.ts` 中的 `RouteMeta` 接口。
- **前端布局**：使用 `<router-view/>` + 侧边导航布局模式。布局子组件（Aside、Header、Content）当前为空桩。
