package com.back.lowcode.config;

import com.back.lowcode.entity.EntityMeta;
import com.back.lowcode.entity.FieldMeta;
import com.back.lowcode.entity.PageSchema;
import com.back.lowcode.repository.EntityMetaRepository;
import com.back.lowcode.repository.FieldMetaRepository;
import com.back.lowcode.service.PageSchemaService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 请假管理系统 — 角色页面生成器
 * 为 学生/辅导员/系主任/管理员 四类角色生成专用界面
 * 在 LeavePageInitializer(Order=2) 之后执行
 */
@Component
@Order(2)
@RequiredArgsConstructor
public class LeavePageInitializerV2 implements CommandLineRunner {

    private final PageSchemaService pageSchemaService;
    private final EntityMetaRepository entityMetaRepository;
    private final FieldMetaRepository fieldMetaRepository;

    private static final String STUDENT_CODE = "student";
    private static final String APPROVER_CODE = "approver";
    private static final String LEAVE_CODE = "leave_application";

    @Override
    @Transactional
    public void run(String... args) {
        // ===== 学生端页面 =====
        // 1. 新建请假（表单页，调用 leave API）
        createStudentApplyPage();

        // 2. 我的请假记录（列表页，按学生ID筛选）
        createStudentRecordsPage();

        // 3. 个人信息
        createProfilePage(STUDENT_CODE, "leave_student_profile", "个人信息", "查看我的基本信息");

        // ===== 辅导员端页面 =====
        // 4. 请假审核 — 辅导员
        createCounselorReviewPage();

        // 5. 个人信息
        createProfilePage(APPROVER_CODE, "leave_counselor_profile", "个人信息", "查看我的基本信息");

        // ===== 系主任端页面 =====
        // 6. 请假审核 — 系主任
        createDeanReviewPage();

        // 7. 个人信息
        createProfilePage(APPROVER_CODE, "leave_dean_profile", "个人信息", "查看我的基本信息");

        // ===== 管理员端页面 =====
        // 8. 用户管理（自定义，用自定义API）
        createAdminUsersPage();

        // 9. 假条管理
        createAdminLeavesPage();

        // 10. 个人信息
        createProfilePage(APPROVER_CODE, "leave_admin_profile", "个人信息", "查看我的基本信息");
    }

    // ==================== 学生：新建请假 ====================
    private void createStudentApplyPage() {
        String pageCode = "leave_student_apply";
        if (exists(pageCode)) return;

        List<Map<String, Object>> elements = new ArrayList<>();
        
        // 标题
        elements.add(textElement("el_title", "新建请假", 24, 24, 300, 40, 22));

        // 表单 - 使用 leave API
        Map<String, Object> formEl = new LinkedHashMap<>();
        formEl.put("id", "el_leave_form");
        formEl.put("type", "form");
        formEl.put("name", "请假表单");
        formEl.put("x", 24); formEl.put("y", 80);
        formEl.put("width", 600); formEl.put("height", 550);
        Map<String, Object> fProps = new LinkedHashMap<>();
        fProps.put("submitApi", "/lowcode/leave/apply");
        fProps.put("labelWidth", 100);
        // 定义表单字段
        fProps.put("fields", List.of(
                Map.of("code", "studentId", "name", "学生ID", "fieldType", "LONG", "required", true),
                Map.of("code", "studentName", "name", "学生姓名", "fieldType", "VARCHAR", "required", true),
                Map.of("code", "leaveType", "name", "请假类型", "fieldType", "VARCHAR", "required", true),
                Map.of("code", "reason", "name", "请假原因", "fieldType", "TEXT", "required", true),
                Map.of("code", "startDate", "name", "开始日期", "fieldType", "DATE", "required", true),
                Map.of("code", "endDate", "name", "结束日期", "fieldType", "DATE", "required", true)
        ));
        formEl.put("props", fProps);
        elements.add(formEl);

        // 说明文字
        elements.add(textElement("el_hint", "提示：请假≤3天仅需辅导员审批，>3天需系主任审批", 
                24, 650, 600, 30, 13));

        save(pageCode, "新建请假", "form", LEAVE_CODE, toJson(elements),
                "在线填写学号、姓名、请假时间、请假理由并提交");
    }

    // ==================== 学生：请假记录 ====================
    private void createStudentRecordsPage() {
        String pageCode = "leave_student_records";
        if (exists(pageCode)) return;

        List<Map<String, Object>> elements = new ArrayList<>();
        elements.add(textElement("el_title", "我的请假记录", 24, 24, 300, 40, 22));

        List<Map<String, Object>> columns = List.of(
                col("student_name", "学生姓名", 100, "VARCHAR"),
                col("leave_type", "请假类型", 90, "VARCHAR"),
                col("reason", "请假原因", 180, "TEXT"),
                col("start_date", "开始日期", 120, "DATE"),
                col("end_date", "结束日期", 120, "DATE"),
                col("leave_days", "天数", 70, "INTEGER"),
                col("status", "审核状态", 130, "VARCHAR")
        );

        Map<String, Object> tableEl = tableWithDataSource("el_records",
                "/lowcode/leave/student-records", columns, 24, 80, 1200, 500);
        elements.add(tableEl);

        save(pageCode, "请假记录", "list", LEAVE_CODE, toJson(elements),
                "查看自己的请假信息和审核状态");
    }

    // ==================== 辅导员：请假审核 ====================
    private void createCounselorReviewPage() {
        String pageCode = "leave_counselor_review";
        if (exists(pageCode)) return;

        List<Map<String, Object>> elements = new ArrayList<>();
        elements.add(textElement("el_title", "请假审核 — 辅导员", 24, 24, 400, 40, 22));
        elements.add(textElement("el_sub", "待辅导员审核的请假申请", 24, 60, 400, 24, 13));

        List<Map<String, Object>> columns = List.of(
                col("student_name", "学生姓名", 100, "VARCHAR"),
                col("leave_type", "请假类型", 90, "VARCHAR"),
                col("reason", "请假原因", 180, "TEXT"),
                col("start_date", "开始日期", 120, "DATE"),
                col("end_date", "结束日期", 120, "DATE"),
                col("leave_days", "天数", 70, "INTEGER"),
                col("status", "审核状态", 130, "VARCHAR")
        );

        Map<String, Object> tableEl = tableWithDataSource("el_review",
                "/lowcode/leave/counselor-pending", columns, 24, 96, 1200, 480);
        elements.add(tableEl);

        save(pageCode, "请假审核", "list", LEAVE_CODE, toJson(elements),
                "辅导员查看待审核的请假申请，可以审批通过或驳回");
    }

    // ==================== 系主任：请假审核 ====================
    private void createDeanReviewPage() {
        String pageCode = "leave_dean_review";
        if (exists(pageCode)) return;

        List<Map<String, Object>> elements = new ArrayList<>();
        elements.add(textElement("el_title", "请假审核 — 系主任", 24, 24, 400, 40, 22));
        elements.add(textElement("el_sub", "待系主任审核的请假申请（仅超过3天的假条）", 24, 60, 500, 24, 13));

        List<Map<String, Object>> columns = List.of(
                col("student_name", "学生姓名", 100, "VARCHAR"),
                col("leave_type", "请假类型", 90, "VARCHAR"),
                col("reason", "请假原因", 180, "TEXT"),
                col("start_date", "开始日期", 120, "DATE"),
                col("end_date", "结束日期", 120, "DATE"),
                col("leave_days", "天数", 70, "INTEGER"),
                col("status", "审核状态", 130, "VARCHAR")
        );

        Map<String, Object> tableEl = tableWithDataSource("el_review",
                "/lowcode/leave/dean-pending", columns, 24, 96, 1200, 480);
        elements.add(tableEl);

        save(pageCode, "请假审核", "list", LEAVE_CODE, toJson(elements),
                "系主任查看待审核的请假申请（仅超过3天），可以审批通过或驳回");
    }

    // ==================== 管理员：用户管理 ====================
    private void createAdminUsersPage() {
        String pageCode = "leave_admin_users";
        if (exists(pageCode)) return;

        List<Map<String, Object>> elements = new ArrayList<>();
        elements.add(textElement("el_title", "用户管理", 24, 24, 300, 40, 22));

        List<Map<String, Object>> columns = List.of(
                col("username", "用户名", 120, "VARCHAR"),
                col("nickname", "昵称", 120, "VARCHAR"),
                colStr("roles", "角色", 200),
                col("status", "状态", 100, "VARCHAR"),
                col("createdAt", "创建时间", 160, "DATETIME")
        );

        Map<String, Object> tableEl = tableWithDataSource("el_users",
                "/lowcode/leave/admin/users", columns, 24, 80, 1200, 480);
        elements.add(tableEl);

        save(pageCode, "用户管理", "list", null, toJson(elements),
                "管理员查看所有用户，冻结或解冻账号");
    }

    // ==================== 管理员：假条管理 ====================
    private void createAdminLeavesPage() {
        String pageCode = "leave_admin_leaves";
        if (exists(pageCode)) return;

        List<Map<String, Object>> elements = new ArrayList<>();
        elements.add(textElement("el_title", "假条管理", 24, 24, 300, 40, 22));

        List<Map<String, Object>> columns = List.of(
                col("student_name", "学生姓名", 100, "VARCHAR"),
                col("leave_type", "请假类型", 90, "VARCHAR"),
                col("reason", "请假原因", 180, "TEXT"),
                col("start_date", "开始日期", 120, "DATE"),
                col("end_date", "结束日期", 120, "DATE"),
                col("leave_days", "天数", 70, "INTEGER"),
                col("status", "审核状态", 130, "VARCHAR")
        );

        Map<String, Object> tableEl = tableWithDataSource("el_leaves",
                "/lowcode/leave/records", columns, 24, 80, 1200, 480);
        elements.add(tableEl);

        save(pageCode, "假条管理", "list", LEAVE_CODE, toJson(elements),
                "管理员查看所有请假记录，可以删除假条");
    }

    // ==================== 个人信息页（通用） ====================
    private void createProfilePage(String entityCode, String pageCode, String name, String desc) {
        if (exists(pageCode)) return;

        Optional<EntityMeta> entityOpt = entityMetaRepository.findByCode(entityCode);
        if (entityOpt.isEmpty()) return;
        List<FieldMeta> fields = fieldMetaRepository.findByEntityIdOrderBySortOrderAsc(entityOpt.get().getId());

        List<Map<String, Object>> elements = new ArrayList<>();
        elements.add(textElement("el_title", name, 24, 24, 200, 40, 22));

        // 简单信息展示表格
        List<Map<String, Object>> infoFields = fields.stream()
                .filter(f -> Boolean.TRUE.equals(f.getShowInList()))
                .map(f -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("label", f.getName());
                    row.put("code", f.getCode());
                    row.put("fieldType", f.getFieldType());
                    return row;
                }).collect(Collectors.toList());

        Map<String, Object> infoBox = new LinkedHashMap<>();
        infoBox.put("id", "el_info");
        infoBox.put("type", "text");
        infoBox.put("name", "基本信息");
        infoBox.put("x", 24); infoBox.put("y", 80);
        infoBox.put("width", 600); infoBox.put("height", 400);
        Map<String, Object> infoProps = new LinkedHashMap<>();
        infoProps.put("text", "个人信息将通过关联账户自动展示");
        infoProps.put("fontSize", 14);
        infoBox.put("props", infoProps);
        elements.add(infoBox);

        save(pageCode, name, "detail", entityCode, toJson(elements), desc);
    }

    // ==================== 元素构建器 ====================

    private Map<String, Object> textElement(String id, String text, int x, int y, int w, int h, int fontSize) {
        Map<String, Object> el = new LinkedHashMap<>();
        el.put("id", id);
        el.put("type", "text");
        el.put("name", text);
        el.put("x", x); el.put("y", y);
        el.put("width", w); el.put("height", h);
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("text", text);
        props.put("fontSize", fontSize);
        props.put("fontWeight", "bold");
        el.put("props", props);
        return el;
    }

    private Map<String, Object> col(String code, String title, int width, String fieldType) {
        Map<String, Object> c = new LinkedHashMap<>();
        c.put("fieldCode", code);
        c.put("title", title);
        c.put("width", width);
        c.put("fieldType", fieldType);
        return c;
    }

    private Map<String, Object> colStr(String code, String title, int width) {
        Map<String, Object> c = new LinkedHashMap<>();
        c.put("fieldCode", code);
        c.put("title", title);
        c.put("width", width);
        c.put("fieldType", "VARCHAR");
        return c;
    }

    private Map<String, Object> tableWithDataSource(String id, String dataSource, 
                                                      List<Map<String, Object>> columns,
                                                      int x, int y, int w, int h) {
        Map<String, Object> el = new LinkedHashMap<>();
        el.put("id", id);
        el.put("type", "table");
        el.put("name", "数据表格");
        el.put("x", x); el.put("y", y);
        el.put("width", w); el.put("height", h);
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("dataSource", dataSource);
        props.put("columns", columns);
        props.put("border", true);
        props.put("showIndex", true);
        props.put("pageSize", 15);
        el.put("props", props);
        return el;
    }

    // ==================== 工具 ====================

    private boolean exists(String pageCode) {
        try {
            return pageSchemaService.getByPageCode(pageCode) != null;
        } catch (Exception e) {
            return false;
        }
    }

    private void save(String pageCode, String name, String pageType,
                      String entityCode, String layoutJson, String desc) {
        PageSchema page = new PageSchema();
        page.setPageCode(pageCode);
        page.setName(name);
        page.setPageType(pageType);
        page.setEntityCode(entityCode);
        page.setLayoutJson(layoutJson);
        page.setDescription(desc);
        page.setStatus("published");
        page.setVersion(1);
        page.setPublishedAt(LocalDateTime.now());
        pageSchemaService.create(page);
    }

    // ==================== 简单JSON序列化 ====================

    @SuppressWarnings("unchecked")
    private String toJson(Object obj) {
        StringBuilder sb = new StringBuilder();
        toJsonString(obj, sb);
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private void toJsonString(Object obj, StringBuilder sb) {
        if (obj == null) {
            sb.append("null");
        } else if (obj instanceof String) {
            sb.append('"').append(escape((String) obj)).append('"');
        } else if (obj instanceof Number) {
            sb.append(obj);
        } else if (obj instanceof Boolean) {
            sb.append(obj);
        } else if (obj instanceof Map) {
            sb.append("{");
            boolean first = true;
            for (Map.Entry<?, ?> e : ((Map<?, ?>) obj).entrySet()) {
                if (!first) sb.append(",");
                first = false;
                toJsonString(e.getKey(), sb);
                sb.append(":");
                toJsonString(e.getValue(), sb);
            }
            sb.append("}");
        } else if (obj instanceof Collection) {
            sb.append("[");
            boolean first = true;
            for (Object item : (Collection<?>) obj) {
                if (!first) sb.append(",");
                first = false;
                toJsonString(item, sb);
            }
            sb.append("]");
        } else {
            sb.append('"').append(escape(String.valueOf(obj))).append('"');
        }
    }

    private String escape(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
