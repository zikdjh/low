import service from "../index.ts";
import type {LoginForm} from "../model/user/Login.ts";

// 获取手机号验证码
const getPhoneCode = async (phone: string) => {
    const response = await service.post('/user/code/phone', null, {
        params: {
            phone
        }
    });
    return response.data;
}

// 登录/注册
const login = async (data: LoginForm) => {
    const response = await service.post('/user/login', null, {
        params: {
            phone: data.phone,
            code: data.code
        }
    });
    return response.data;
};

// 获取邮箱验证码
const getEmailCode = async (email: string) => {
    const response = await service.post('/user/code/email', null, {
        params: {
            email
        }
    });
    return response.data;
}

// 绑定邮箱
const emailBind = async (email: string, code: string) => {
    const response = await service.patch('/user/email/bind', null, {
        params: {
            email,
            code
        }
    });
    return response.data;
}

// 更新用户名
const updateUserName = async (userName: string) => {
    const response = await service.patch('/user/update/name', null, {
        params: {
            username: userName
        }
    });
    return response.data
}

// 更新用户头像
const updateUserAvatar = async (avatar: FormData) => {
    const response = await service.post('/user/update/avatar', avatar, {
        headers: {
            "Content-Type": "multipart/form-data"
        },
    })
    return response.data
}

// 更新用户自我介绍
const updateUserSelfIntroduction = async (selfIntroduction: string) => {
    const response = await service.patch('/user/update/selfIntroduction', null, {
        params: {
            selfIntroduction
        }
    });
    return response.data
}

// 获取其它用户信息
const getUserInfo = async (userName: string) => {
    const response = await service.get('/user/info', {
        params: {
            userName
        }
    })
    return response.data
}

// 获取用户个人信息
const getUserInfoPersonal = async () => {
    const response = await service.get('/user/info/personal')
    return response.data
}

// 刷新token（依赖 HttpOnly Cookie）
const refreshToken = async () => {
    const response = await service.patch('/user/refresh/access');
    return response.data;
}

// 修改密码
const changePassword = async (currentPassword: string, newPassword: string, confirmPassword: string) => {
    const response = await service.patch('/auth/password', {
        currentPassword,
        newPassword,
        confirmPassword
    });
    return response.data;
}

const userApi = {
    getPhoneCode,
    login,
    getEmailCode,
    emailBind,
    updateUserName,
    updateUserAvatar,
    updateUserSelfIntroduction,
    getUserInfo,
    getUserInfoPersonal,
    refreshToken,
    changePassword
}

export default userApi;











