import axios from "axios"
import userApi from "./user/index.ts"

// 创建axios实例
const service = axios.create({
    baseURL: '/api',
    timeout: 10000,
})
// 开启跨域携带 Cookie
service.defaults.withCredentials = true;

const writeList = [
    // 只读/无需鉴权接口
    "/end/article/get",
    "/end/article/get/visible",

    "/end/comment/get/all",
    "/end/comment/get/article",

    "/end/tag/get",

    "/end/user/code/phone",
    "/end/user/login",
    "/user/refresh/access"
]

// 刷新队列及状态，避免并发重复刷新
let isRefreshing = false;
let requestQueue: Array<{ config: any; resolve: (v: any) => void; reject: (e: any) => void }> = [];

function getAccessToken(): string | null {
    const raw = sessionStorage.getItem('access');
    if (!raw) return null;
    try {
        const parsed = JSON.parse(raw) as { token: string; expiresAt: number };
        // 也可做预刷新策略：将近过期时提前刷新，这里先按后端 499 驱动
        return parsed.token;
    } catch {
        return null;
    }
}

function setAccessToken(token: string) {
    const payload = {token, expiresAt: Date.now() + 45 * 60 * 1000};
    sessionStorage.setItem('access', JSON.stringify(payload));
}

// 请求拦截器
service.interceptors.request.use(
    config => {
        const isWritePath = writeList.some(item => config.url?.includes(item));
        if (!isWritePath) {
            const token = getAccessToken();
            if (token) {
                // 后端 TokenFilter 读取头名 ACCESS_TOKEN_TYPE= 'access'
                config.headers['access'] = token;
            }
        }
        return config
    },
    error => Promise.reject(error)
)

// 响应拦截器
service.interceptors.response.use(
    response => response,
    async error => {
        const {response, config} = error;
        if (response && response.status === 499) {
            if (config.url?.includes('/user/refresh/access')) {
                // The refresh endpoint itself failed; do not retry it recursively.
                return Promise.reject(error);
            }
            return new Promise(async (resolve, reject) => {
                requestQueue.push({ config, resolve, reject });
                if (!isRefreshing) {
                    isRefreshing = true;
                    try {
                        const refreshResp = await userApi.refreshToken();
                        if (refreshResp.code === 1) {
                            const newToken = refreshResp.data;
                            setAccessToken(newToken);
                            const queued = requestQueue.splice(0);
                            for (const { config: cfg, resolve: res, reject: rej } of queued) {
                                try {
                                    cfg.headers = cfg.headers || {};
                                    cfg.headers['access'] = newToken;
                                    const retryResp = await service.request(cfg);
                                    res(retryResp);
                                } catch (retryErr) {
                                    rej(retryErr);
                                }
                            }
                        } else {
                            const queued = requestQueue.splice(0);
                            sessionStorage.removeItem('access');
                            queued.forEach(({ reject: rej }) => rej(error));
                        }
                    } catch (e) {
                        const queued = requestQueue.splice(0);
                        sessionStorage.removeItem('access');
                        queued.forEach(({ reject: rej }) => rej(e));
                    } finally {
                        isRefreshing = false;
                    }
                }
            });
        }
        return Promise.reject(error)
    }
)

export default service