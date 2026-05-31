export interface UserVO{

    // 用户手机号
    userPhone: String;

    // 用户名
    username: String;

    // 用户邮箱
    userEmail?: String;

    // 用户头像
    userAvatar?: String;

    // 用户简介
    userSelfIntroduction?: String;

    // 账户创建时间
    userCreateTime: Date;
}