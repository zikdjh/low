import service from "../index.ts";
import type { AuthLoginForm, RegisterForm } from "../model/user/Auth.ts";

/**
 * 注册：POST /auth/register
 * 后端返回 Result：code=1 成功，data 为不含密码的用户视图
 */
const register = async (data: RegisterForm) => {
    const response = await service.post('/auth/register', data);
    return response.data;
};

/**
 * 登录：POST /auth/login
 * 后端目前不签发 token（无 Security 过滤链），data 为用户视图
 */
const login = async (data: AuthLoginForm) => {
    const response = await service.post('/auth/login', data);
    return response.data;
};

const authApi = {
    register,
    login,
};

export default authApi;
