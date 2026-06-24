package com.back.lowcode.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 请假场景业务种子数据 —— 仅插入 lc_student / lc_approver 演示行。
 * <p>
 * 元数据（EntityMeta + FieldMeta + 表结构 + Page + Menu + BusinessApp）已迁移到
 * {@code classpath:seed/leave_management-v1.0.2.json} + {@link com.back.lowcode.release.seed.SeedReleaseLoader}
 * （{@code @Order(50)}）。本 runner 仅在 {@code @Order(60)} 处补业务示例数据，方便首启即可登录体验。
 * <p>
 * 物理表已通过 SeedReleaseLoader / DDLService 建好；若仍缺失则跳过插入（不阻塞启动）。
 */
@Slf4j
@Component
@Order(60)
@RequiredArgsConstructor
public class LeaveDataInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(org.springframework.boot.ApplicationArguments args) {
        seedStudentData();
        seedApproverData();
    }

    private boolean tableExists(String tableName) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = ?",
                    Integer.class, tableName);
            return count != null && count > 0;
        } catch (DataAccessException e) {
            return false;
        }
    }

    private void seedStudentData() {
        if (!tableExists("lc_student")) return;
        try {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM lc_student", Integer.class);
            if (count != null && count > 0) return;
        } catch (DataAccessException e) {
            return;
        }

        String sql = "INSERT INTO lc_student (student_no, name, gender, class_name, grade, department, phone, email, user_id, status, created_at, updated_at) VALUES " +
                "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())";
        try {
            jdbcTemplate.update(sql, "2021001", "张三", "male", "计科2101", "2021级", "计算机学院",
                    "13800001111", "zhangsan@example.com", 3L, "active");
            jdbcTemplate.update(sql, "2021002", "李四", "female", "计科2102", "2021级", "计算机学院",
                    "13800002222", "lisi@example.com", null, "active");
            jdbcTemplate.update(sql, "2021003", "王五", "male", "软件2101", "2021级", "软件学院",
                    "13800003333", "wangwu@example.com", null, "active");
            log.info("[seed] lc_student 演示数据已写入 (3 行)");
        } catch (DataAccessException e) {
            log.warn("[seed] lc_student 演示数据写入失败：{}", e.getMessage());
        }
    }

    private void seedApproverData() {
        if (!tableExists("lc_approver")) return;
        try {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM lc_approver", Integer.class);
            if (count != null && count > 0) return;
        } catch (DataAccessException e) {
            return;
        }

        String sql = "INSERT INTO lc_approver (name, approver_type, department, title, phone, email, user_id, status, created_at, updated_at) VALUES " +
                "(?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())";
        try {
            jdbcTemplate.update(sql, "李辅导员", "counselor", "计算机学院", "辅导员",
                    "13900001111", "fdy@example.com", 4L, "active");
            jdbcTemplate.update(sql, "王主任", "dept_head", "计算机学院", "系主任/教授",
                    "13900002222", "xizhuren@example.com", 5L, "active");
            log.info("[seed] lc_approver 演示数据已写入 (2 行)");
        } catch (DataAccessException e) {
            log.warn("[seed] lc_approver 演示数据写入失败：{}", e.getMessage());
        }
    }
}
