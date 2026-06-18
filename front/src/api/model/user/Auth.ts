// /auth 接口入参类型
export interface RegisterForm {
    username: string;
    password: string;
    nickname?: string;
}

export interface AuthLoginForm {
    username: string;
    password: string;
}

// 后端 AuthService.toUserView 返回的用户视图（剥离 password）
export interface AuthUserView {
    id: number;
    username: string;
    nickname?: string;
    createdAt?: string;
    /** 角色编码列表，例如 ["root"] / ["user"] */
    roles?: string[];
}

/**
 * /auth/register 与 /auth/login 的成功 payload。
 * refresh token 由后端写入 HttpOnly Cookie，前端不感知。
 */
export interface LoginPayload {
    token: string;
    user: AuthUserView;
}

/** /auth/refresh 成功 payload — 仅刷新 access，user 保持不变 */
export interface RefreshPayload {
    token: string;
    user: AuthUserView;
}
