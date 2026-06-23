package com.back.lowcode.config;

import com.back.lowcode.entity.EntityMeta;
import com.back.lowcode.entity.EntityRelation;
import com.back.lowcode.entity.FieldMeta;
import com.back.lowcode.repository.EntityMetaRepository;
import com.back.lowcode.repository.EntityRelationRepository;
import com.back.lowcode.repository.FieldMetaRepository;
import com.back.lowcode.service.DDLService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 请假管理场景初始化器
 * 自动创建学生、教职工、请假申请三张实体表，建立实体间关系，并插入演示数据
 * 体现低代码平台「元数据驱动 + 实体关系建模」的设计思路
 */
@Component
@Order(1)
@RequiredArgsConstructor
public class LeaveDataInitializer implements CommandLineRunner {

    private final EntityMetaRepository entityMetaRepository;
    private final FieldMetaRepository fieldMetaRepository;
    private final EntityRelationRepository entityRelationRepository;
    private final DDLService ddlService;
    private final JdbcTemplate jdbcTemplate;

    // ==================== 实体编码常量 ====================
    private static final String STUDENT_CODE = "student";
    private static final String APPROVER_CODE = "approver";
    private static final String LEAVE_CODE = "leave_application";

    @Override
    public void run(String... args) {
        // 1. 按依赖顺序创建实体（学生和教职工独立 → 请假申请依赖学生）
        initStudentEntity();
        initApproverEntity();
        initLeaveEntity();

        // 2. 建立实体间关系
        initEntityRelations();

        // 3. 插入演示数据
        seedDemoData();
    }

    // ==================== 表存在性检查 ====================

    private boolean tableExists(String tableName) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = ?",
                    Integer.class, tableName);
            return count != null && count > 0;
        } catch (DataAccessException e) {
            try {
                jdbcTemplate.queryForObject("SELECT 1 FROM `" + tableName + "` LIMIT 1", Integer.class);
                return true;
            } catch (DataAccessException ex) {
                return false;
            }
        }
    }

    // ==================== 实体创建/恢复通用逻辑 ====================

    /**
     * 处理实体存在时的状态恢复逻辑
     * @return true 表示已处理（跳过后续创建），false 表示需要新建
     */
    private boolean handleExistingEntity(String code, String tableName) {
        Optional<EntityMeta> existing = entityMetaRepository.findByCode(code);
        if (existing.isEmpty()) return false;

        EntityMeta entity = existing.get();
        if ("published".equals(entity.getStatus()) && !tableExists(tableName)) {
            tryCreateTable(entity);
        } else if ("draft".equals(entity.getStatus())) {
            entity.setStatus("published");
            entityMetaRepository.save(entity);
            tryCreateTable(entity);
        }
        return true;
    }

    // ==================== 1. 学生实体 ====================

    private void initStudentEntity() {
        String tableName = "lc_student";
        if (handleExistingEntity(STUDENT_CODE, tableName)) return;

        EntityMeta entity = EntityMeta.builder()
                .code(STUDENT_CODE)
                .name("学生信息")
                .tableName(tableName)
                .description("学生基本信息，包含学号、班级、院系、联系方式等")
                .status("draft")
                .build();
        entity = entityMetaRepository.save(entity);
        Long eid = entity.getId();

        List<FieldMeta> fields = List.of(
                field(eid, "student_no", "学号", "student_no", "VARCHAR", 20, 1,
                        false, null, true, true, true),
                field(eid, "name", "姓名", "name", "VARCHAR", 64, 2,
                        false, null, true, true, true),
                field(eid, "gender", "性别", "gender", "VARCHAR", 8, 3,
                        true, null, true, true, false),
                field(eid, "class_name", "班级", "class_name", "VARCHAR", 64, 4,
                        true, null, true, true, true),
                field(eid, "grade", "年级", "grade", "VARCHAR", 16, 5,
                        true, null, true, true, false),
                field(eid, "department", "院系", "department", "VARCHAR", 64, 6,
                        true, null, true, true, false),
                field(eid, "phone", "联系电话", "phone", "VARCHAR", 20, 7,
                        true, null, true, true, false),
                field(eid, "email", "邮箱", "email", "VARCHAR", 128, 8,
                        true, null, true, true, false),
                field(eid, "user_id", "关联用户", "user_id", "LONG", null, 9,
                        true, null, true, false, false),
                field(eid, "status", "学籍状态", "status", "VARCHAR", 16, 10,
                        false, "active", true, true, true)
        );
        fieldMetaRepository.saveAll(fields);
        tryCreateTable(entity);
    }

    // ==================== 2. 教职工实体（辅导员/系主任合一） ====================

    private void initApproverEntity() {
        String tableName = "lc_approver";
        if (handleExistingEntity(APPROVER_CODE, tableName)) return;

        EntityMeta entity = EntityMeta.builder()
                .code(APPROVER_CODE)
                .name("教职工信息")
                .tableName(tableName)
                .description("教职工基本信息，通过 approver_type 区分辅导员(counselor)和系主任(dept_head)")
                .status("draft")
                .build();
        entity = entityMetaRepository.save(entity);
        Long eid = entity.getId();

        List<FieldMeta> fields = List.of(
                field(eid, "name", "姓名", "name", "VARCHAR", 64, 1,
                        false, null, true, true, true),
                field(eid, "approver_type", "人员类型", "approver_type", "VARCHAR", 32, 2,
                        false, null, true, true, true),
                field(eid, "department", "所属院系", "department", "VARCHAR", 64, 3,
                        true, null, true, true, true),
                field(eid, "title", "职称", "title", "VARCHAR", 32, 4,
                        true, null, true, true, false),
                field(eid, "phone", "联系电话", "phone", "VARCHAR", 20, 5,
                        true, null, true, true, false),
                field(eid, "email", "邮箱", "email", "VARCHAR", 128, 6,
                        true, null, true, true, false),
                field(eid, "user_id", "关联用户", "user_id", "LONG", null, 7,
                        true, null, true, false, false),
                field(eid, "status", "在职状态", "status", "VARCHAR", 16, 8,
                        false, "active", true, true, true)
        );
        fieldMetaRepository.saveAll(fields);
        tryCreateTable(entity);
    }

    // ==================== 3. 请假申请实体 ====================

    private void initLeaveEntity() {
        String tableName = "lc_leave_application";
        if (handleExistingEntity(LEAVE_CODE, tableName)) return;

        EntityMeta entity = EntityMeta.builder()
                .code(LEAVE_CODE)
                .name("请假申请")
                .tableName(tableName)
                .description("学生请假申请记录，关联学生实体，通过工作流完成审批流程")
                .status("draft")
                .build();
        entity = entityMetaRepository.save(entity);
        Long eid = entity.getId();

        // student_id 添加引用关系: referenceEntityCode="student", referenceDisplayFieldCode="name"
        FieldMeta f1 = FieldMeta.builder().entityId(eid).code("student_id").name("学生")
                .columnName("student_id").fieldType("LONG").nullable(false).sortOrder(1)
                .referenceEntityCode("student").referenceDisplayFieldCode("name")
                .showInList(true).showInForm(true).showInSearch(true).build();
        FieldMeta f2 = FieldMeta.builder().entityId(eid).code("student_name").name("学生姓名")
                .columnName("student_name").fieldType("VARCHAR").length(64).nullable(false).sortOrder(2)
                .showInList(true).showInForm(false).showInSearch(true).build();
        FieldMeta f3 = FieldMeta.builder().entityId(eid).code("leave_type").name("请假类型")
                .columnName("leave_type").fieldType("VARCHAR").length(32).nullable(false).sortOrder(3)
                .showInList(true).showInForm(true).showInSearch(true).build();
        FieldMeta f4 = FieldMeta.builder().entityId(eid).code("reason").name("请假原因")
                .columnName("reason").fieldType("TEXT").nullable(false).sortOrder(4)
                .showInList(true).showInForm(true).showInSearch(false).build();
        FieldMeta f5 = FieldMeta.builder().entityId(eid).code("start_date").name("开始日期")
                .columnName("start_date").fieldType("DATE").nullable(false).sortOrder(5)
                .showInList(true).showInForm(true).showInSearch(false).build();
        FieldMeta f6 = FieldMeta.builder().entityId(eid).code("end_date").name("结束日期")
                .columnName("end_date").fieldType("DATE").nullable(false).sortOrder(6)
                .showInList(true).showInForm(true).showInSearch(false).build();
        FieldMeta f7 = FieldMeta.builder().entityId(eid).code("leave_days").name("请假天数")
                .columnName("leave_days").fieldType("INTEGER").nullable(false).sortOrder(7)
                .showInList(true).showInForm(false).showInSearch(false).build();
        FieldMeta f8 = FieldMeta.builder().entityId(eid).code("status").name("审批状态")
                .columnName("status").fieldType("VARCHAR").length(32).nullable(false).sortOrder(8)
                .defaultValue("pending").showInList(true).showInForm(false).showInSearch(true).build();

        fieldMetaRepository.saveAll(List.of(f1, f2, f3, f4, f5, f6, f7, f8));
        tryCreateTable(entity);
    }

    // ==================== 4. 实体关系 ====================

    @Transactional
    private void initEntityRelations() {
        if (entityRelationRepository.count() > 0) {
            return;
        }

        List<EntityRelation> relations = List.of(
                // 请假申请.student_id → 学生实体 (多对一)
                EntityRelation.builder()
                        .sourceEntityCode(LEAVE_CODE)
                        .sourceFieldCode("student_id")
                        .targetEntityCode(STUDENT_CODE)
                        .targetDisplayFieldCode("name")
                        .relationType("MANY_TO_ONE")
                        .description("请假申请关联学生，一个学生可有多条请假记录")
                        .cascadeDelete(false)
                        .enabled(true)
                        .build(),
                // 学生.user_id → 系统用户 (一对一)
                EntityRelation.builder()
                        .sourceEntityCode(STUDENT_CODE)
                        .sourceFieldCode("user_id")
                        .targetEntityCode("user")
                        .targetDisplayFieldCode("username")
                        .relationType("ONE_TO_ONE")
                        .description("学生关联系统登录用户")
                        .cascadeDelete(false)
                        .enabled(true)
                        .build(),
                // 教职工.user_id → 系统用户 (一对一)
                EntityRelation.builder()
                        .sourceEntityCode(APPROVER_CODE)
                        .sourceFieldCode("user_id")
                        .targetEntityCode("user")
                        .targetDisplayFieldCode("username")
                        .relationType("ONE_TO_ONE")
                        .description("教职工关联系统登录用户")
                        .cascadeDelete(false)
                        .enabled(true)
                        .build()
        );
        entityRelationRepository.saveAll(relations);
    }

    // ==================== 5. 演示数据 ====================

    private void seedDemoData() {
        seedStudentData();
        seedApproverData();
    }

    private void seedStudentData() {
        if (!tableExists("lc_student")) return;
        try {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM lc_student", Integer.class);
            if (count != null && count > 0) {
                return;
            }
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
        } catch (DataAccessException e) {
            // 演示数据插入失败不影响主流程
        }
    }

    private void seedApproverData() {
        if (!tableExists("lc_approver")) return;
        try {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM lc_approver", Integer.class);
            if (count != null && count > 0) {
                return;
            }
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
        } catch (DataAccessException e) {
            // 演示数据插入失败不影响主流程
        }
    }

    // ==================== 建表工具 ====================

    private void tryCreateTable(EntityMeta entity) {
        try {
            List<FieldMeta> fields = fieldMetaRepository.findByEntityIdOrderBySortOrderAsc(entity.getId());
            ddlService.generateCreateTable(entity, fields);
            if (!"published".equals(entity.getStatus())) {
                entity.setStatus("published");
                entityMetaRepository.save(entity);
            }
        } catch (Exception e) {
            if ("published".equals(entity.getStatus())) {
                entity.setStatus("draft");
                entityMetaRepository.save(entity);
            }
            throw new RuntimeException("建表失败: " + entity.getCode(), e);
        }
    }

    // ==================== FieldMeta 快速构建器 ====================

    private FieldMeta field(Long entityId, String code, String name, String columnName,
                            String fieldType, Integer length, int sortOrder,
                            boolean nullable, String defaultValue,
                            boolean showInList, boolean showInForm, boolean showInSearch) {
        return FieldMeta.builder()
                .entityId(entityId)
                .code(code)
                .name(name)
                .columnName(columnName)
                .fieldType(fieldType)
                .length(length)
                .nullable(nullable)
                .defaultValue(defaultValue)
                .sortOrder(sortOrder)
                .showInList(showInList)
                .showInForm(showInForm)
                .showInSearch(showInSearch)
                .build();
    }
}
