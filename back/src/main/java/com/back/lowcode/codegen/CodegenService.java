package com.back.lowcode.codegen;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 代码生成核心 — 把 {@link CodegenContext} 构建的根上下文驱动若干 Freemarker 模板，
 * 产出 path → content 的 LinkedHashMap，调用方可决定写盘 / 内存预览 / 打包。
 *
 * <p>模板加载策略：classpath 下 {@code codegen/templates/}，模板路径形如：
 * <ul>
 *   <li>{@code java/entity.java.ftl}</li>
 *   <li>{@code vue/list-page.vue.ftl}</li>
 *   <li>{@code sql/ddl.sql.ftl}</li>
 * </ul>
 *
 * <p>生成产物目录形如：
 * <pre>
 * backend/
 *   ├─ entity/{ClassName}.java
 *   ├─ repository/{ClassName}Repository.java
 *   ├─ service/{ClassName}Service.java
 *   ├─ controller/{ClassName}Controller.java
 *   └─ dto/{ClassName}DTO.java   {ClassName}Query.java
 * frontend/
 *   ├─ views/{appCode}/{ComponentName}.vue
 *   ├─ api/{appCode}/{entityCode}.ts
 *   ├─ router/modules/{appCode}.ts
 *   └─ menu/{appCode}.ts
 * sql/
 *   └─ V_{releaseId}__{appCode}.sql
 * </pre>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CodegenService implements InitializingBean {

    private final CodegenContext codegenContext;

    private Configuration freemarker;

    @Override
    public void afterPropertiesSet() {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_34);
        cfg.setClassForTemplateLoading(CodegenService.class, "/codegen/templates");
        cfg.setDefaultEncoding(StandardCharsets.UTF_8.name());
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setLogTemplateExceptions(false);
        cfg.setWrapUncheckedExceptions(true);
        cfg.setFallbackOnNullLoopVariable(false);
        cfg.setNumberFormat("0.######");  // 防止整数被渲染成 "1,234"
        this.freemarker = cfg;
    }

    /**
     * 生成所有产物，返回 {@code path → fileContent}（顺序：实体维度 → 应用维度）
     *
     * @param releaseId Release id
     */
    public Map<String, String> generate(Long releaseId) {
        Map<String, Object> root = codegenContext.build(releaseId);
        Map<String, String> out = new LinkedHashMap<>();
        String appCode = (String) root.get("appCode");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> entities = (List<Map<String, Object>>) root.get("entities");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> pages = (List<Map<String, Object>>) root.get("pages");

        if (entities == null) entities = new ArrayList<>();
        if (pages == null) pages = new ArrayList<>();

        // ===== 后端：per entity =====
        for (Map<String, Object> entity : entities) {
            Map<String, Object> entityRoot = new LinkedHashMap<>(root);
            entityRoot.put("entity", entity);
            entityRoot.put("pkType", resolvePkType(entity));

            String className = (String) entity.get("className");
            out.put("backend/entity/" + className + ".java",
                    render("java/entity.java.ftl", entityRoot));
            out.put("backend/repository/" + className + "Repository.java",
                    render("java/repository.java.ftl", entityRoot));
            out.put("backend/service/" + className + "Service.java",
                    render("java/service.java.ftl", entityRoot));
            out.put("backend/controller/" + className + "Controller.java",
                    render("java/controller.java.ftl", entityRoot));
            out.put("backend/dto/" + className + "DTO.java",
                    render("java/dto.java.ftl", entityRoot));
            out.put("backend/dto/" + className + "Query.java",
                    render("java/query.java.ftl", entityRoot));
        }

        // ===== 后端：整应用 DDL =====
        out.put("sql/V_" + releaseId + "__" + appCode + ".sql",
                render("sql/ddl.sql.ftl", root));

        // ===== 前端：per page =====
        Map<String, Map<String, Object>> entityByCode = indexEntities(entities);
        for (Map<String, Object> page : pages) {
            String pageType = String.valueOf(page.getOrDefault("pageType", "list"));
            String entityCode = (String) page.get("entityCode");
            Map<String, Object> entity = entityByCode.get(entityCode);
            if (entity == null) {
                log.warn("page {} 引用的实体 {} 未在 release 快照中，跳过 vue 生成",
                        page.get("pageCode"), entityCode);
                continue;
            }
            Map<String, Object> pageRoot = new LinkedHashMap<>(root);
            pageRoot.put("page", page);
            pageRoot.put("entity", entity);

            String componentName = (String) page.get("componentName");
            String template = switch (pageType) {
                case "form" -> "vue/form-page.vue.ftl";
                case "detail" -> "vue/detail-page.vue.ftl";
                default -> "vue/list-page.vue.ftl";
            };
            out.put("frontend/views/" + appCode + "/" + componentName + ".vue",
                    render(template, pageRoot));
        }

        // ===== 前端：per entity api =====
        for (Map<String, Object> entity : entities) {
            Map<String, Object> entityRoot = new LinkedHashMap<>(root);
            entityRoot.put("entity", entity);
            out.put("frontend/api/" + appCode + "/" + entity.get("code") + ".ts",
                    render("vue/api.ts.ftl", entityRoot));
        }

        // ===== 前端：路由 + 菜单 =====
        out.put("frontend/router/modules/" + appCode + ".ts",
                render("vue/router.ts.ftl", root));
        out.put("frontend/menu/" + appCode + ".ts",
                render("vue/menu.ts.ftl", root));

        return out;
    }

    private Map<String, Map<String, Object>> indexEntities(List<Map<String, Object>> entities) {
        Map<String, Map<String, Object>> map = new LinkedHashMap<>();
        for (Map<String, Object> e : entities) {
            map.put((String) e.get("code"), e);
        }
        return map;
    }

    private String resolvePkType(Map<String, Object> entity) {
        @SuppressWarnings("unchecked")
        Map<String, Object> pk = (Map<String, Object>) entity.get("primaryKeyField");
        if (pk == null) return "Long";
        return String.valueOf(pk.getOrDefault("javaType", "Long"));
    }

    private String render(String templatePath, Map<String, Object> root) {
        try (Writer w = new StringWriter()) {
            Template t = freemarker.getTemplate(templatePath);
            t.process(root, w);
            return w.toString();
        } catch (Exception ex) {
            log.error("模板渲染失败: {} — {}", templatePath, ex.getMessage(), ex);
            throw new RuntimeException("模板渲染失败: " + templatePath + " — " + ex.getMessage(), ex);
        }
    }
}
