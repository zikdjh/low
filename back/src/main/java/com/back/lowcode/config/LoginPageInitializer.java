package com.back.lowcode.config;

import com.back.lowcode.entity.PageSchema;
import com.back.lowcode.service.PageSchemaService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 登录页设计 —— 请假管理系统登录界面
 * 简洁居中布局：角色选择 + 用户名密码 + 登录
 */
@Component
@Order(4)
@RequiredArgsConstructor
public class LoginPageInitializer implements CommandLineRunner {

    private final PageSchemaService pageSchemaService;

    /** 新版登录页 pageCode，以 leave_ 开头会被 BusinessAppInitializer 自动归入请假管理系统 */
    private static final String PAGE_CODE = "leave_login";

    @Override
    public void run(String... args) {
        // 1. 清理旧版 login_page（如果存在）
        PageSchema oldLogin = pageSchemaService.getByPageCode("login_page");
        if (oldLogin != null) {
            pageSchemaService.delete(oldLogin.getId());
        }

        // 2. 构建新版登录页元素
        String layoutJson = toJson(buildElements());

        // 3. 创建或更新
        PageSchema existing = pageSchemaService.getByPageCode(PAGE_CODE);
        if (existing != null) {
            existing.setName("登录页");
            existing.setLayoutJson(layoutJson);
            existing.setDescription("请假管理系统登录界面——选择身份后输入用户名密码登录");
            existing.setPageType("custom");
            existing.setEntityCode(null);
            existing.setAppCode("leave_management");
            existing.setVersion(existing.getVersion() + 1);
            existing.setPublishedAt(LocalDateTime.now());
            existing.setStatus("published");
            pageSchemaService.update(existing.getId(), existing);
        } else {
            PageSchema page = new PageSchema();
            page.setPageCode(PAGE_CODE);
            page.setName("登录页");
            page.setPageType("custom");
            page.setEntityCode(null);
            page.setAppCode("leave_management");
            page.setLayoutJson(layoutJson);
            page.setDescription("请假管理系统登录界面——选择身份后输入用户名密码登录");
            page.setStatus("published");
            page.setVersion(1);
            page.setPublishedAt(LocalDateTime.now());
            pageSchemaService.create(page);
        }
    }

    // ==================== 页面布局 ====================
    // 画布 1200 × 700，居中卡片式登录表单

    private List<Map<String, Object>> buildElements() {
        List<Map<String, Object>> elements = new ArrayList<>();

        // 白色卡片容器（带圆角和阴影）
        elements.add(container("el_card", 370, 60, 460, 540, "#ffffff", 12, true));

        // 标题：登录
        elements.add(textCenter("el_title", "登录", 390, 108, 420, 48, 28, "bold", "#1a1a3e"));

        // 副标题
        elements.add(textCenter("el_subtitle", "欢迎进入请假管理系统", 390, 162, 420, 24, 15, "normal", "#888"));

        // 分割线
        elements.add(divider("el_div", 420, 205, 360, 1, false));

        // 身份选择标签
        elements.add(textCenter("el_role_label", "请选择身份", 390, 230, 420, 22, 13, "normal", "#aaa"));

        // 角色按钮（一行三个，居中）
        elements.add(buttonEl("el_role_student",  "学生",   "default", "medium", 408, 262, 118, 42));
        elements.add(buttonEl("el_role_fdy",       "辅导员", "default", "medium", 541, 262, 118, 42));
        elements.add(buttonEl("el_role_director",  "系主任", "default", "medium", 674, 262, 118, 42));

        // 用户名
        elements.add(inputEl("el_username", "用户名", "请输入用户名", 400, 340, 400, 50));

        // 密码
        elements.add(inputEl("el_password", "密码",   "请输入密码",   400, 415, 400, 50));

        // 登录按钮
        elements.add(buttonEl("el_btn_login", "登 录", "primary", "large", 400, 498, 400, 48));

        return elements;
    }

    // ==================== 元素构建辅助 ====================

    // --- 容器（卡片）---
    private Map<String, Object> container(String id, int x, int y, int w, int h,
                                           String bgColor, int borderRadius, boolean shadow) {
        Map<String, Object> el = base(id, "container", "卡片", x, y, w, h);
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("title", "");
        props.put("padding", 0);
        props.put("borderRadius", borderRadius);
        props.put("bgColor", bgColor);
        props.put("shadow", shadow);
        el.put("props", props);
        return el;
    }

    // --- 分割线 ---
    private Map<String, Object> divider(String id, int x, int y, int w, int h, boolean dashed) {
        Map<String, Object> el = base(id, "divider", "分割线", x, y, w, h);
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("dashed", dashed);
        el.put("props", props);
        return el;
    }

    // --- 文本：居中 ---
    private Map<String, Object> textCenter(String id, String text, int x, int y,
                                            int w, int h, int fontSize, String fontWeight, String color) {
        Map<String, Object> el = base(id, "text", text, x, y, w, h);
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("text", text);
        props.put("fontSize", fontSize);
        props.put("fontWeight", fontWeight);
        props.put("color", color);
        props.put("textAlign", "center");
        el.put("props", props);
        return el;
    }

    // --- 输入框 ---
    private Map<String, Object> inputEl(String id, String label, String placeholder,
                                         int x, int y, int w, int h) {
        Map<String, Object> el = base(id, "input", label, x, y, w, h);
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("label", label);
        props.put("placeholder", placeholder);
        props.put("required", true);
        el.put("props", props);
        return el;
    }

    // --- 按钮 ---
    private Map<String, Object> buttonEl(String id, String text, String theme,
                                          String size, int x, int y, int w, int h) {
        Map<String, Object> el = base(id, "button", text, x, y, w, h);
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("text", text);
        props.put("theme", theme);
        props.put("size", size);
        el.put("props", props);
        return el;
    }

    // --- 基础元素 ---
    private Map<String, Object> base(String id, String type, String name,
                                      int x, int y, int w, int h) {
        Map<String, Object> el = new LinkedHashMap<>();
        el.put("id", id);
        el.put("type", type);
        el.put("name", name);
        el.put("x", x);
        el.put("y", y);
        el.put("width", w);
        el.put("height", h);
        return el;
    }

    // ==================== JSON 序列化（纯 JDK） ====================

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
