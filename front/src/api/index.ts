import axios from "axios"
import authApi from "./auth/index.ts"

// 创建axios实例
const service = axios.create({
    baseURL: '/api',
    timeout: 10000,
})
// 跨域携带 Cookie（refresh token 在 HttpOnly Cookie 中）
service.defaults.withCredentials = true;

/**
 * 公开接口白名单：不附加 access 头，也不在 401 时尝试 refresh。
 * 与后端 SecurityConfig.permitAll 列表一致。
 */
const publicPaths = [
    "/auth/register",
    "/auth/login",
    "/auth/refresh",
    "/auth/logout",
];

// 刷新队列及状态，避免并发重复刷新
let isRefreshing = false;
let requestQueue: Array<{ config: any; resolve: (v: any) => void; reject: (e: any) => void }> = [];

function isPublicPath(url?: string): boolean {
    if (!url) return false;
    return publicPaths.some(p => url.includes(p));
}

function getAccessToken(): string | null {
    const raw = sessionStorage.getItem('access');
    if (!raw) return null;
    try {
        const parsed = JSON.parse(raw) as { token: string; expiresAt: number };
        return parsed.token;
    } catch {
        return null;
    }
}

function setAccessToken(token: string) {
    // 与后端 auth.jwt.access-expire-minutes=45 对齐
    const payload = { token, expiresAt: Date.now() + 45 * 60 * 1000 };
    sessionStorage.setItem('access', JSON.stringify(payload));
}

function clearAccessToken() {
    sessionStorage.removeItem('access');
}

// 请求拦截器
service.interceptors.request.use(
    config => {
        if (!isPublicPath(config.url)) {
            const token = getAccessToken();
            if (token) {
                // 后端 JwtAuthenticationFilter 读取 'access' 头
                config.headers['access'] = token;
            }
        }
        return config
    },
    error => Promise.reject(error)
)

// 响应拦截器：401 触发 refresh，失败则清理 + 跳登录
service.interceptors.response.use(
    response => response,
    async error => {
        const { response, config } = error;

        // 仅对受保护接口、且非重试请求做 refresh 尝试
        const shouldTryRefresh =
            response &&
            response.status === 401 &&
            config &&
            !config.__retried &&
            !isPublicPath(config.url);

        if (!shouldTryRefresh) {
            return Promise.reject(error);
        }

        return new Promise((resolve, reject) => {
            requestQueue.push({ config, resolve, reject });

            if (isRefreshing) return;
            isRefreshing = true;

            (async () => {
                try {
                    const refreshResp = await authApi.refresh();
                    if (refreshResp && refreshResp.code === 1 && refreshResp.data?.token) {
                        const newToken = refreshResp.data.token as string;
                        setAccessToken(newToken);

                        const queued = requestQueue.splice(0);
                        for (const { config: cfg, resolve: res, reject: rej } of queued) {
                            try {
                                cfg.headers = cfg.headers || {};
                                cfg.headers['access'] = newToken;
                                cfg.__retried = true;
                                const retryResp = await service.request(cfg);
                                res(retryResp);
                            } catch (retryErr) {
                                rej(retryErr);
                            }
                        }
                    } else {
                        const queued = requestQueue.splice(0);
                        clearAccessToken();
                        queued.forEach(({ reject: rej }) => rej(error));
                        redirectToLogin();
                    }
                } catch (e) {
                    const queued = requestQueue.splice(0);
                    clearAccessToken();
                    queued.forEach(({ reject: rej }) => rej(e));
                    redirectToLogin();
                } finally {
                    isRefreshing = false;
                }
            })();
        });
    }
)

function redirectToLogin() {
    // 已在登录页则不跳，避免循环
    if (typeof window !== 'undefined' && window.location.pathname !== '/login') {
        const redirect = encodeURIComponent(window.location.pathname + window.location.search);
        window.location.href = `/login?redirect=${redirect}`;
    }
}

export default service
