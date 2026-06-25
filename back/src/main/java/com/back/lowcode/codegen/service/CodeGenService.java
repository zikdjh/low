package com.back.lowcode.codegen.service;

import com.back.lowcode.codegen.dto.CodeGenPreviewDTO;
import com.back.lowcode.entity.EntityMeta;
import com.back.lowcode.entity.FieldMeta;
import com.back.lowcode.enums.FieldType;
import com.back.lowcode.repository.EntityMetaRepository;
import com.back.lowcode.repository.FieldMetaRepository;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 低代码代码生成服务
 *
 * <p>读取已发布的 {@link EntityMeta} + 其 {@link FieldMeta}，通过 Freemarker
 * 渲染出后端 JPA / Service / Controller 和前端 API / List.vue / Edit.vue 代码骨架，
 * 类似 JeecgBoot 的代码生成器。</p>
 *
 * <p>仅依赖元数据层，不触碰用户动态数据表。</p>
 */
@Service
@RequiredArgsConstructor
public class CodeGenService {

    /** 生成代码的默认后端包名。 */
    public static final String DEFAULT_PKG_BASE = "com.back.generated";

    private final EntityMetaRepository entityMetaRepository;
    private final FieldMetaRepository fieldMetaRepository;

    private Configuration freemarkerCfg;

    @PostConstruct
    void initFreemarker() {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_34);
        cfg.setClassLoaderForTemplateLoading(getClass().getClassLoader(), "templates/codegen");
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setLogTemplateExceptions(false);
        cfg.setWrapUncheckedExceptions(true);
        cfg.setFallbackOnNullLoopVariable(false);
        this.freemarkerCfg = cfg;
    }

    /**
     * 预览所有生成的文件。
     *
     * @param entityId 实体 ID（必须存在，状态不限——草稿也可预览，但只有已发布才建议直接落盘）
     * @return 按相对路径分组的文件内容
     */
    public CodeGenPreviewDTO preview(Long entityId) {
        EntityMeta entity = entityMetaRepository.findById(entityId)
                .orElseThrow(() -> new IllegalArgumentException("实体不存在: " + entityId));
        List<FieldMeta> fields = fieldMetaRepository.findByEntityIdOrderBySortOrderAsc(entity.getId());

        Map<String, Object> ctx = buildContext(entity, fields, DEFAULT_PKG_BASE);
        Map<String, String> files = renderAll(ctx);

        CodeGenPreviewDTO dto = new CodeGenPreviewDTO();
        dto.setEntityCode(entity.getCode());
        dto.setEntityName(entity.getName());
        dto.setClassName((String) ((Map<?, ?>) ctx.get("entity")).get("className"));
        dto.setFiles(files);
        return dto;
    }

    /**
     * 打包成 ZIP 字节流，目录结构对齐 JeecgBoot：
     * <pre>
     *   {className}/
     *     backend/src/main/java/com/back/generated/...
     *     frontend/api/{className}.ts
     *     frontend/views/{className}List.vue
     *     frontend/views/{className}Edit.vue
     * </pre>
     */
    public byte[] downloadZip(Long entityId) {
        CodeGenPreviewDTO preview = preview(entityId);
        String root = preview.getClassName() + "/";

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (Map.Entry<String, String> e : preview.getFiles().entrySet()) {
                ZipEntry entry = new ZipEntry(root + e.getKey());
                zos.putNextEntry(entry);
                zos.write(e.getValue().getBytes(java.nio.charset.StandardCharsets.UTF_8));
                zos.closeEntry();
            }
        } catch (Exception e) {
            throw new IllegalStateException("打包 ZIP 失败: " + e.getMessage(), e);
        }
        return baos.toByteArray();
    }

    /**
     * 安装结果摘要 —— 写入了哪些文件，跳过了哪些（已存在且未强制覆盖）。
     */
    public static class InstallResult {
        public String className;
        public String entityCode;
        public Path projectRoot;
        public List<String> written = new ArrayList<>();
        public List<String> skipped = new ArrayList<>();
        public boolean force;
    }

    /**
     * 把渲染结果直接写入项目源码目录，重启后即可访问 /lowcode/gen/{entityCode}/* 接口。
     *
     * <p>项目根目录通过定位 {@code back/pom.xml} 自动识别：从当前工作目录向上回溯查找，
     * 这样无论 Spring Boot 是从 {@code back/} 还是从仓库根目录启动都能正确解析。</p>
     *
     * @param entityId 实体 ID
     * @param force    {@code false} 时遇到已存在文件会跳过（计入 skipped）；{@code true} 时强制覆盖
     */
    public InstallResult installToProject(Long entityId, boolean force) {
        CodeGenPreviewDTO preview = preview(entityId);
        Path projectRoot = locateProjectRoot();

        InstallResult result = new InstallResult();
        result.className = preview.getClassName();
        result.entityCode = preview.getEntityCode();
        result.projectRoot = projectRoot;
        result.force = force;

        for (Map.Entry<String, String> e : preview.getFiles().entrySet()) {
            Path target = projectRoot.resolve(e.getKey()).normalize();
            // 安全检查：目标路径必须落在项目根目录下，且必须落在 back/ 或 front/ 之内
            if (!target.startsWith(projectRoot)) {
                throw new IllegalStateException("目标路径越界: " + target);
            }
            String rel = projectRoot.relativize(target).toString().replace('\\', '/');
            if (!rel.startsWith("back/") && !rel.startsWith("front/")) {
                throw new IllegalStateException("目标路径不在允许的子目录下: " + rel);
            }

            if (Files.exists(target) && !force) {
                result.skipped.add(rel);
                continue;
            }
            try {
                Files.createDirectories(target.getParent());
                Files.writeString(target, e.getValue(), StandardCharsets.UTF_8);
                result.written.add(rel);
            } catch (Exception ex) {
                throw new IllegalStateException("写入文件失败 " + rel + ": " + ex.getMessage(), ex);
            }
        }
        return result;
    }

    /**
     * 定位项目根目录 —— 包含 {@code back/} 和 {@code front/} 两个子目录的那一层。
     * 从当前工作目录向上回溯，最多查 5 层。
     */
    static Path locateProjectRoot() {
        Path cwd = Paths.get("").toAbsolutePath();
        Path p = cwd;
        for (int i = 0; i < 5 && p != null; i++) {
            if (Files.isDirectory(p.resolve("back")) && Files.isDirectory(p.resolve("front"))) {
                return p;
            }
            p = p.getParent();
        }
        throw new IllegalStateException(
                "无法定位项目根目录（需要同级包含 back/ 和 front/），当前工作目录: " + cwd);
    }

    // -----------------------------------------------------------------
    // Internals
    // -----------------------------------------------------------------

    /**
     * 构建 Freemarker 上下文。所有 8 个模板共享同一个 root map。
     */
    private Map<String, Object> buildContext(EntityMeta entity, List<FieldMeta> fields, String pkgBase) {
        // 过滤掉系统主键 id 之外，按 sortOrder 排序
        List<FieldMeta> userFields = new ArrayList<>(fields);
        userFields.sort(Comparator.comparingInt(f -> f.getSortOrder() == null ? 0 : f.getSortOrder()));

        boolean hasUserPk = userFields.stream().anyMatch(f -> Boolean.TRUE.equals(f.getIsPrimaryKey()));

        // 构建字段视图模型
        List<Map<String, Object>> fieldModels = new ArrayList<>(userFields.size());
        List<Map<String, Object>> listFields = new ArrayList<>();
        List<Map<String, Object>> formFields = new ArrayList<>();
        List<Map<String, Object>> searchFields = new ArrayList<>();
        Set<String> javaImports = new LinkedHashSet<>();

        for (FieldMeta f : userFields) {
            Map<String, Object> fm = toFieldModel(f);
            fieldModels.add(fm);
            String javaImport = importForJavaType((String) fm.get("javaType"));
            if (javaImport != null) javaImports.add(javaImport);

            if (Boolean.TRUE.equals(f.getShowInList())) listFields.add(fm);
            if (Boolean.TRUE.equals(f.getShowInForm())) formFields.add(fm);
            if (Boolean.TRUE.equals(f.getShowInSearch())) searchFields.add(fm);
        }
        // 总是引入 LocalDateTime（createdAt/updatedAt）
        javaImports.add("java.time.LocalDateTime");

        // 实体视图模型
        String className = toPascalCase(entity.getCode());
        Map<String, Object> entityModel = new LinkedHashMap<>();
        entityModel.put("code", entity.getCode());
        entityModel.put("name", entity.getName());
        entityModel.put("description", entity.getDescription());
        entityModel.put("tableName", entity.getTableName());
        entityModel.put("className", className);
        entityModel.put("classNameLower", toCamelCase(entity.getCode()));

        Map<String, Object> ctx = new HashMap<>();
        ctx.put("entity", entityModel);
        ctx.put("fields", fieldModels);
        ctx.put("listFields", listFields);
        ctx.put("formFields", formFields);
        ctx.put("searchFields", searchFields);
        ctx.put("imports", javaImports);
        ctx.put("pkgBase", pkgBase);
        ctx.put("hasUserPk", hasUserPk);
        ctx.put("now", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return ctx;
    }

    /**
     * 单个 FieldMeta -> 视图模型，包含模板需要的所有派生字段（驼峰命名、java 类型、ts 类型、是否必填等）。
     */
    private Map<String, Object> toFieldModel(FieldMeta f) {
        FieldType ft;
        try {
            ft = FieldType.valueOf(f.getFieldType());
        } catch (IllegalArgumentException e) {
            ft = FieldType.VARCHAR;
        }
        String camelName = toCamelCase(f.getCode());
        String pascalName = capitalize(camelName);

        // 长度（仅 VARCHAR 暴露给模板）
        Integer maxLength = ft == FieldType.VARCHAR ? f.getLength() : null;

        // 校验规则简化：required 直接看 nullable
        boolean required = Boolean.FALSE.equals(f.getNullable())
                || (f.getValidationRule() != null && f.getValidationRule().contains("\"required\":true"));

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("code", f.getCode());
        m.put("name", f.getName());
        m.put("columnName", f.getColumnName());
        m.put("fieldType", ft.name());
        m.put("javaType", ft.getJavaType());
        m.put("tsType", toTsType(ft));
        m.put("camelName", camelName);
        m.put("pascalName", pascalName);
        m.put("nullable", Boolean.TRUE.equals(f.getNullable()));
        m.put("required", required);
        m.put("isPrimaryKey", Boolean.TRUE.equals(f.getIsPrimaryKey()));
        m.put("isAutoIncrement", Boolean.TRUE.equals(f.getIsAutoIncrement()));
        if (maxLength != null) m.put("maxLength", maxLength);
        if (f.getScale() != null) m.put("scale", f.getScale());
        if (f.getDictCode() != null && !f.getDictCode().isEmpty()) m.put("dictCode", f.getDictCode());
        return m;
    }

    /** 按上下文渲染 8 个模板，返回 {相对路径 -> 内容}。路径布局与 installToProject 的写入位置一致。 */
    private Map<String, String> renderAll(Map<String, Object> ctx) {
        Map<String, Object> entity = castMap(ctx.get("entity"));
        String className = (String) entity.get("className");
        String classNameLower = (String) entity.get("classNameLower");
        String pkgPath = ((String) ctx.get("pkgBase")).replace('.', '/');
        // 路径布局与 installToProject 落盘位置一致 —— zip 里也是这个结构
        String backendBase = "back/src/main/java/" + pkgPath;
        String frontApiBase = "front/src/api/lowcode/generated";
        String frontPageBase = "front/src/pages/lowcode/generated";

        Map<String, String> out = new LinkedHashMap<>();
        out.put(backendBase + "/entity/" + className + ".java", render("java/Entity.java.ftl", ctx));
        out.put(backendBase + "/dto/" + className + "DTO.java", render("java/DTO.java.ftl", ctx));
        out.put(backendBase + "/repository/" + className + "Repository.java", render("java/Repository.java.ftl", ctx));
        out.put(backendBase + "/service/" + className + "Service.java", render("java/Service.java.ftl", ctx));
        out.put(backendBase + "/controller/" + className + "Controller.java", render("java/Controller.java.ftl", ctx));

        out.put(frontApiBase + "/" + classNameLower + ".ts", render("vue/api.ts.ftl", ctx));
        out.put(frontPageBase + "/" + className + "List.vue", render("vue/List.vue.ftl", ctx));
        out.put(frontPageBase + "/" + className + "Edit.vue", render("vue/Edit.vue.ftl", ctx));
        return out;
    }

    private String render(String templateName, Map<String, Object> ctx) {
        try {
            Template tpl = freemarkerCfg.getTemplate(templateName);
            StringWriter sw = new StringWriter();
            tpl.process(ctx, sw);
            return sw.toString();
        } catch (Exception e) {
            throw new IllegalStateException("渲染模板失败 " + templateName + ": " + e.getMessage(), e);
        }
    }

    /** snake_case -> PascalCase。entity code 是 ascii、^[a-zA-Z][a-zA-Z0-9_]*$ 由 EntityMetaService 保证。 */
    static String toPascalCase(String code) {
        String camel = toCamelCase(code);
        return capitalize(camel);
    }

    static String toCamelCase(String code) {
        if (code == null || code.isEmpty()) return code;
        StringBuilder sb = new StringBuilder(code.length());
        boolean upperNext = false;
        for (int i = 0; i < code.length(); i++) {
            char c = code.charAt(i);
            if (c == '_' || c == '-') {
                upperNext = true;
                continue;
            }
            if (i == 0) {
                sb.append(Character.toLowerCase(c));
            } else if (upperNext) {
                sb.append(Character.toUpperCase(c));
                upperNext = false;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    /** 仅当 java 类型需要 import 时返回 FQN，否则 null。 */
    static String importForJavaType(String javaType) {
        switch (javaType) {
            case "LocalDate":
                return "java.time.LocalDate";
            case "LocalDateTime":
                return "java.time.LocalDateTime";
            case "BigDecimal":
                return "java.math.BigDecimal";
            default:
                return null;
        }
    }

    static String toTsType(FieldType ft) {
        switch (ft) {
            case INTEGER:
            case LONG:
            case DOUBLE:
            case DECIMAL:
                return "number";
            case BOOLEAN:
                return "boolean";
            default:
                return "string";
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> castMap(Object o) {
        return (Map<String, Object>) o;
    }
}
