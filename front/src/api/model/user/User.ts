export interface User{

    // 用户id
    userId: String;

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

    // 账户状态
    // 0：禁用
    // 1：正常
    userStatus: Number;

    // 账户创建时间
    userCreateTime: Date;
}