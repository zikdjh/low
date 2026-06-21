package com.back.service;

import com.back.common.Result;

import java.util.Map;

public interface AdminService {

    /** 获取所有用户列表（含状态） */
    Result listUsers();

    /** 冻结用户 */
    Result freezeUser(Long id);

    /** 解冻用户 */
    Result unfreezeUser(Long id);

    /** 获取所有页面 */
    Result listAllPages();

    /** 获取所有实体 */
    Result listAllEntities();

    /** 获取管理员个人信息 */
    Result getProfile(Long adminId);

    /** 更新管理员个人信息 */
    Result updateProfile(Long adminId, Map<String, String> body);
}
