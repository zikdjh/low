# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

A full-stack web application split into two modules:

- **`back/`** — Spring Boot 3.5.14 REST API, Java 21, Maven, MySQL + Redis
- **`front/`** — Vue 3 + TypeScript SPA, Vite 8, TDesign Vue Next component library

## Build & Run

### Backend (requires JDK 21, MySQL, Redis)

```bash
# Development run (Maven wrapper)
cd back && ./mvnw spring-boot:run     # Windows: mvnw.cmd spring-boot:run

# Run all tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=BackApplicationTests

# Package JAR
./mvnw clean package -DskipTests
```

Server starts on port **8080**. Requires MySQL database `low_end` and Redis on localhost:6379.
Configuration: `back/src/main/resources/application.yaml`.

### Frontend (requires Node.js)

```bash
cd front
npm install
npm run dev       # Start dev server on port 5174
npm run build     # Type-check + production build
npm run preview   # Preview production build
```

Vite dev server proxies `/api` → `http://0.0.0.0:8080` (strips `/api` prefix on rewrite).

## Architecture

### Backend (`com.back`)

Flat package-by-feature structure (early stage — no controllers/services/repositories exist yet):

| Package | Purpose |
|---------|---------|
| `common/` | `Result` — unified API response `{code, msg, data}` (1=success, 0=error). `Constant` — thread pool sizes, token type strings (`access`/`refresh`). `RedisConstant` — Redis scan defaults. |
| `config/` | `WebConfig` — registers `PathInterceptor` globally and configures CORS (all origins, credentials). `basic/JacksonConfig` — custom `ObjectMapper` bean (`endObjectMapper`) with JavaTimeModule, no timestamp serialization, lenient unknown properties. `basic/ThreadPoolConfig` — `deleteDataExecutor` bean with `@PreDestroy` graceful shutdown. |
| `interceptors/` | `PathInterceptor` — logs every request path via SLF4J, always returns `true`. |
| `annotation/` | `@RateLimit` — method-level annotation for Redis-backed rate limiting (prefix, time window, count limit, custom message). Not yet wired to an aspect/interceptor. |
| `exception/` | `GlobalExceptionHandler` (@RestControllerAdvice) — catches `Exception`, `NoResourceFoundException` (404), `HandlerMethodValidationException`, `MissingServletRequestParameterException`, `RejectedExecutionException`, `HttpRequestMethodNotSupportedException`. Custom `SqlInsertException`/`SqlUpdateException`/`SqlDeleteException` extend `RuntimeException`. |
| `utils/` | `RedisUtil` — static utility for Redis Hash CRUD with Jackson serialization. Handles: JSON-to-hash, hash-to-object, pipelined batch operations, SCAN-based key listing (avoids `KEYS *`), object-to-map conversion with nested JSON handling. All methods take `StringRedisTemplate` and `ObjectMapper` explicitly. |

**Dependencies**: Spring Boot Web, Spring Data JPA (MySQL via HikariCP pool, max 30 connections), Spring Data Redis (Lettuce, pool max 10), Validation, java-jwt 4.5.1, Lombok.

### Frontend

**Routing** — `router/index.ts` auto-discovers route modules via `import.meta.glob`:
- `./modules/**/home.ts` — main routes (currently `/` → redirect to `/home`, using `Layout`)
- `./modules/**/Login.ts` — login route (`/login`, hidden from nav)

**State management** — Pinia 3 with `pinia-plugin-persistedstate`:
- `useSettingStore` — layout mode (`light`/`dark`/`auto`), side mode, brand theme color. Generates TDesign color palettes via `tvision-color`. Persisted to localStorage.
- `useMarkdownEditorStore` — editor/preview/code themes. Persisted.
- `useCropperStore` — image cropping modal state with Promise-based `open()`/`confirm()`/`cancel()` API. Not persisted.
- `useUserStore` — empty stub (defined in `modules/user.ts`, file is effectively empty).

**API layer** (`api/index.ts`) — Axios instance with base URL `/api`:
- **Auth**: Access token stored in `sessionStorage` under key `access` as `{token, expiresAt}` (45-minute expiry). Sent as `access` header on write-protected endpoints.
- **Write list**: A hardcoded `writeList` array of read-only endpoint paths that skip auth (article GETs, comment GETs, tag GET, login, phone code, token refresh).
- **Token refresh**: On HTTP 499 response, queues concurrent failed requests and refreshes the access token once via `PATCH /user/refresh/access`. Retries all queued requests with the new token.
- **User API** (`api/user/index.ts`): `getPhoneCode`, `login`, `getEmailCode`, `emailBind`, `updateUserName`, `updateUserAvatar`, `updateUserSelfIntroduction`, `getUserInfo`, `getUserInfoPersonal`, `refreshToken`.

**UI Framework**: TDesign Vue Next (v1.20) with `tdesign-icons-vue-next`. Login page uses TDesign form components with custom validation. Theme system uses CSS custom properties (`--td-brand-color`, `--td-bg-color-container`, etc.).

**Key dependencies**: `md-editor-v3` (Markdown editor), `vue-cropper` (image cropping), `echarts` + `tvision-color` (charts & color), `katex` + `highlight.js` + `mermaid` (content rendering), `@speechmatics/browser-audio-input` + `@tdesign-vue-next/chat` (AI assistant).

## Auth Flow

1. User submits phone number → `POST /api/end/user/code/phone` sends SMS code
2. User submits phone + code → `POST /api/end/user/login` returns access token
3. Access token stored in `sessionStorage`, sent as `access` header on write operations
4. Refresh token is HttpOnly cookie (handled automatically by browser)
5. On 499 status from any request → `PATCH /end/user/refresh/access` using the HttpOnly cookie → new access token
6. Concurrent requests during refresh are queued and retried once

## Code Patterns

- **Backend** uses Lombok (`@Data`, `@AllArgsConstructor`, `@NoArgsConstructor`, `@Slf4j`, `@RequiredArgsConstructor`). No `@Service`/`@Repository`/`@Controller` classes exist yet — the project is in scaffolding phase.
- **Result wrapper**: All API responses should use `Result.success(data)` or `Result.error(msg)`. Code `1` = success, `0` = error.
- **Redis access**: Always use `RedisUtil` static methods; pass `StringRedisTemplate` and `ObjectMapper` (bean name `endObjectMapper`) explicitly. Use SCAN over KEYS.
- **Frontend types**: API model types in `api/model/`, shared interfaces in `types/interface.d.ts`. Route metadata uses `RouteMeta` interface from `types/interface.d.ts`.
- **Frontend layout**: Uses `<router-view/>` with side-navigation layout pattern. Layout components (Aside, Header, Content) are currently empty stubs.
