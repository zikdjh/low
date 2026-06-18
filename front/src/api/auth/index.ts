import service from "../index.ts";
import type { AuthLoginForm, RegisterForm } from "../model/user/Auth.ts";

/**
 * 注册：POST /auth/register
 * 后端 Result<{ token, user }>，注册即登录；refresh token 写入 HttpOnly Cookie
 */
const register = async (data: RegisterForm) => {
    const response = await service.post('/auth/register', data);
    return response.data;
};

/**
 * 登录：POST /auth/login
 * 后端 Result<{ token, user }>；refresh token 写入 HttpOnly Cookie
 */
const login = async (data: AuthLoginForm) => {
    const response = await service.post('/auth/login', data);
    return response.data;
};

/**
 * 刷新：POST /auth/refresh
 * 依赖 HttpOnly Cookie 中的 refresh token；后端仅返回新 access，user 不变
 */
const refresh = async () => {
    const response = await service.post('/auth/refresh');
    return response.data;
};

/**
 * 登出：POST /auth/logout
 * 仅清空 refresh Cookie，前端自行清理 sessionStorage
 */
const logout = async () => {
    const response = await service.post('/auth/logout');
    return response.data;
};

const authApi = {
    register,
    login,
    refresh,
    logout,
};

export default authApi;
