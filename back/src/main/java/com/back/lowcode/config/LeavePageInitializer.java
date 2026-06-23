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

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 请假管理场景 —— 低代码页面自动生成
 * 
 * 为 student / approver / leave_application 三个实体各生成列表页和表单页，
 * 使用本低代码平台的 PageSchema + PageViewer 渲染，不再手写 Vue 页面。
 */
@Component
@Order(2) // 在 LeaveDataInitializer(Order=1) 之后执行
@RequiredArgsConstructor
public class LeavePageInitializer implements CommandLineRunner {

    private final PageSchemaService pageSchemaService;
    private final EntityMetaRepository entityMetaRepository;
    private final FieldMetaRepository fieldMetaRepository;

    private static final String STUDENT_CODE = "student";
    private static final String APPROVER_CODE = "approver";
    private static final String LEAVE_CODE = "leave_application";

    @Override
    public void run(String... args) {
        createListPage(STUDENT_CODE, "学生管理", "leave_student_list",
                "学生信息列表，支持搜索学号、姓名、班级");
        createFormPage(STUDENT_CODE, "学生表单", "leave_student_form",
                "新增/编辑学生信息");

        createListPage(APPROVER_CODE, "教职工管理", "leave_approver_list",
                "教职工信息列表，支持搜索姓名、类型、院系");
        createFormPage(APPROVER_CODE, "教职工表单", "leave_approver_form",
                "新增/编辑教职工信息");

        createListPage(LEAVE_CODE, "请假记录", "leave_application_list",
                "请假申请记录列表，可按学生、状态筛选");
        createFormPage(LEAVE_CODE, "请假申请", "leave_application_form",
                "提交请假申请");
    }

    // ==================== 列表页 ====================

    private void createListPage(String entityCode, String name, String pageCode, String desc) {
        if (exists(pageCode)) return;

        Optional<EntityMeta> entityOpt = entityMetaRepository.findByCode(entityCode);
        if (entityOpt.isEmpty()) return;
        List<FieldMeta> fields = fieldMetaRepository.findByEntityIdOrderBySortOrderAsc(entityOpt.get().getId());
        // 只取 showInList=true 的字段作为表格列
        List<Map<String, Object>> columns = fields.stream()
                .filter(f -> Boolean.TRUE.equals(f.getShowInList()))
                .map(f -> {
                    Map<String, Object> col = new LinkedHashMap<>();
                    col.put("fieldCode", f.getCode());
                    col.put("title", f.getName());
                    col.put("width", estimateWidth(f));
                    col.put("fieldType", f.getFieldType());
                    return col;
                }).collect(Collectors.toList());

        String layoutJson = toJson(List.of(
                textElement("el_title", name, 24, 24, 400, 48, 24),
                tableElement("el_table", entityCode, columns, 24, 88, 1150, 520)
        ));

        save(pageCode, name, "list", entityCode, layoutJson, desc);
    }

    // ==================== 表单页 ====================

    private void createFormPage(String entityCode, String name, String pageCode, String desc) {
        if (exists(pageCode)) return;

        Optional<EntityMeta> entityOpt = entityMetaRepository.findByCode(entityCode);
        if (entityOpt.isEmpty()) return;
        List<FieldMeta> fields = fieldMetaRepository.findByEntityIdOrderBySortOrderAsc(entityOpt.get().getId());

        List<Map<String, Object>> elements = new ArrayList<>();
        elements.add(textElement("el_title", name, 24, 24, 400, 48, 24));
        elements.add(formElement("el_form", entityCode, fields, 24, 88, 700, 600));

        save(pageCode, name, "form", entityCode, toJson(elements), desc);
    }

    // ==================== 元素构建 ====================

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

    private Map<String, Object> tableElement(String id, String entityCode, List<Map<String, Object>> columns,
                                              int x, int y, int w, int h) {
        Map<String, Object> el = new LinkedHashMap<>();
        el.put("id", id);
        el.put("type", "table");
        el.put("name", "数据表格");
        el.put("x", x); el.put("y", y);
        el.put("width", w); el.put("height", h);
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("entityCode", entityCode);
        props.put("columns", columns);
        props.put("border", true);
        props.put("showIndex", true);
        el.put("props", props);
        return el;
    }

    private Map<String, Object> formElement(String id, String entityCode, List<FieldMeta> fields,
                                             int x, int y, int w, int h) {
        Map<String, Object> el = new LinkedHashMap<>();
        el.put("id", id);
        el.put("type", "form");
        el.put("name", "表单");
        el.put("x", x); el.put("y", y);
        el.put("width", w); el.put("height", h);
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("entityCode", entityCode);
        props.put("labelWidth", 100);
        // 只传表单字段信息（showInForm=true 的字段）
        List<Map<String, Object>> formFields = fields.stream()
                .filter(f -> Boolean.TRUE.equals(f.getShowInForm()))
                .map(f -> {
                    Map<String, Object> ff = new LinkedHashMap<>();
                    ff.put("code", f.getCode());
                    ff.put("name", f.getName());
                    ff.put("fieldType", f.getFieldType());
                    ff.put("required", !Boolean.TRUE.equals(f.getNullable()));
                    ff.put("defaultValue", f.getDefaultValue());
                    ff.put("referenceEntityCode", f.getReferenceEntityCode());
                    return ff;
                }).collect(Collectors.toList());
        props.put("fields", formFields);
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

    private int estimateWidth(FieldMeta f) {
        String type = f.getFieldType();
        if ("DATE".equals(type) || "DATETIME".equals(type)) return 120;
        if ("INTEGER".equals(type) || "LONG".equals(type)) return 100;
        if ("TEXT".equals(type)) return 200;
        Integer len = f.getLength();
        if (len != null && len > 64) return 180;
        return 140;
    }

    @SuppressWarnings("unchecked")
    private String toJson(Object obj) {
        // 只依赖 JDK，避免引入 Jackson 依赖初始化循环
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
