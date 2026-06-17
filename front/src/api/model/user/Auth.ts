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

// 后端 AuthService.toView 返回的用户视图（剥离 password）
export interface AuthUserView {
    id: number;
    username: string;
    nickname?: string;
    createdAt?: string;
}
