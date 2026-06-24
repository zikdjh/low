package com.back.lowcode.codegen;

import com.back.lowcode.entity.Release;
import com.back.lowcode.entity.ReleaseItem;
import com.back.lowcode.repository.BusinessAppRepository;
import com.back.lowcode.repository.ReleaseItemRepository;
import com.back.lowcode.repository.ReleaseRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 代码生成上下文构建器 — 把一个 Release 还原成 Freemarker 渲染所需的 Map root。
 * <p>
 * 关键设计：
 * <ul>
 *   <li>仅依赖 {@link ReleaseItem} 快照 + {@link Release} / {@link com.back.lowcode.entity.BusinessApp}，
 *       绝不读设计态（避免发布后设计态变更污染生成产物）</li>
 *   <li>所有内部数据使用 LinkedHashMap，保证模板渲染顺序与数据库内顺序一致</li>
 *   <li>对外暴露 plain Map / List —— Freemarker 直接消费，无需额外 introspection</li>
 * </ul>
 * 构造后的上下文形如：
 * <pre>
 * {
 *   appCode: "leave_management",
 *   appName: "请假管理",
 *   version: "1.0.0",
 *   releaseId: 123,
 *   basePackage: "com.generated.leave_management",
 *   entities: [
 *     { code, name, tableName, className, varName,
 *       fields: [ { code, name, columnName, fieldType, javaType, tsType, ... } ],
 *       imports: [ "java.math.BigDecimal", ... ],
 *       hasReference: true,
 *       primaryKeyField: { ... } }
 *   ],
 *   pages: [ { pageCode, name, pageType, entityCode, layoutJson, layout: {...} } ],
 *   dicts: [ { dictCode, dictName, items: [ { itemCode, itemName, sortOrder } ] } ],
 *   menu: [ MenuNode tree ],
 *   components: [ ... ]
 * }
 * </pre>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CodegenContext {

    private final ReleaseRepository releaseRepository;
    private final ReleaseItemRepository releaseItemRepository;
    private final BusinessAppRepository businessAppRepository;
    private final ObjectMapper objectMapper;

    private static final TypeReference<Map<String, Object>> MAP_TR = new TypeReference<>() {};
    private static final TypeReference<List<Map<String, Object>>> LIST_TR = new TypeReference<>() {};

    /**
     * 从 release id 构建上下文。Release 不存在抛 {@link IllegalArgumentException}。
     */
    public Map<String, Object> build(Long releaseId) {
        Release release = releaseRepository.findById(releaseId)
                .orElseThrow(() -> new IllegalArgumentException("Release 不存在: id=" + releaseId));

        Map<String, Object> ctx = new LinkedHashMap<>();
        ctx.put("releaseId", release.getId());
        ctx.put("appCode", release.getAppCode());
        ctx.put("version", release.getVersion());
        ctx.put("status", release.getStatus());
        ctx.put("checksum", release.getChecksum());
        ctx.put("notes", release.getNotes());

        // 应用元信息（不强制 — 部分场景 release 比 app 元数据更长寿）
        businessAppRepository.findByCode(release.getAppCode()).ifPresent(app -> {
            ctx.put("appName", app.getName());
            ctx.put("appDescription", app.getDescription());
        });
        ctx.putIfAbsent("appName", release.getAppCode());
        ctx.putIfAbsent("appDescription", "");

        String basePackage = "com.generated." + sanitizePackage(release.getAppCode());
        ctx.put("basePackage", basePackage);

        // 按 itemType 分桶读出 ReleaseItem
        List<ReleaseItem> all = releaseItemRepository.findByReleaseId(releaseId);
        Map<String, List<ReleaseItem>> byType = new LinkedHashMap<>();
        for (ReleaseItem it : all) {
            byType.computeIfAbsent(it.getItemType(), k -> new ArrayList<>()).add(it);
        }

        ctx.put("entities", buildEntities(byType.getOrDefault("entity", List.of())));
        ctx.put("pages", buildPages(byType.getOrDefault("page", List.of())));
        ctx.put("dicts", buildDicts(byType.getOrDefault("dict", List.of())));
        ctx.put("components", buildComponents(byType.getOrDefault("component", List.of())));
        ctx.put("menu", buildMenu(byType.getOrDefault("menu", List.of())));

        return ctx;
    }

    // ===== entity =====
    private List<Map<String, Object>> buildEntities(List<ReleaseItem> items) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (ReleaseItem it : items) {
            Map<String, Object> snapshot = readJson(it, MAP_TR);
            if (snapshot == null) continue;

            @SuppressWarnings("unchecked")
            Map<String, Object> entity = (Map<String, Object>) snapshot.get("entity");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> fields = (List<Map<String, Object>>) snapshot.get("fields");
            if (entity == null) continue;
            if (fields == null) fields = new ArrayList<>();

            String code = (String) entity.get("code");
            Map<String, Object> e = new LinkedHashMap<>();
            e.put("id", entity.get("id"));
            e.put("code", code);
            e.put("name", entity.get("name"));
            e.put("tableName", entity.get("tableName"));
            e.put("description", entity.get("description"));
            e.put("status", entity.get("status"));
            e.put("className", CodegenTypeMapper.toPascalCase(code));
            e.put("varName", CodegenTypeMapper.toCamelCase(code));

            // 字段映射 + import 决议
            List<Map<String, Object>> fOut = new ArrayList<>();
            boolean hasReference = false;
            Map<String, Object> pk = null;
            for (Map<String, Object> f : fields) {
                Map<String, Object> fm = mapField(f);
                fOut.add(fm);
                if (Boolean.TRUE.equals(fm.get("isPrimaryKey"))) {
                    pk = fm;
                }
                if ("REFERENCE".equalsIgnoreCase(String.valueOf(fm.get("fieldType")))) {
                    hasReference = true;
                }
            }
            e.put("fields", fOut);
            e.put("imports", CodegenTypeMapper.collectJavaImports(fOut));
            e.put("hasReference", hasReference);
            e.put("primaryKeyField", pk);
            out.add(e);
        }
        return out;
    }

    private Map<String, Object> mapField(Map<String, Object> raw) {
        Map<String, Object> f = new LinkedHashMap<>(raw);
        String code = (String) raw.get("code");
        String fieldType = (String) raw.get("fieldType");
        CodegenTypeMapper.Mapping m = CodegenTypeMapper.resolve(fieldType);
        f.put("javaType", m.getJavaType());
        f.put("tsType", m.getTsType());
        f.put("tsDefault", m.getTsDefault());
        f.put("temporal", m.isTemporal());
        f.put("numeric", m.isNumeric());
        f.put("camelName", CodegenTypeMapper.toCamelCase(code));
        f.put("pascalName", CodegenTypeMapper.toPascalCase(code));
        Object len = raw.get("length");
        Object prec = raw.get("precision");
        Object scale = raw.get("scale");
        f.put("mysqlType", CodegenTypeMapper.mysqlColumnType(
                fieldType,
                toInt(len), toInt(prec), toInt(scale)));
        f.putIfAbsent("nullable", true);
        f.putIfAbsent("isPrimaryKey", false);
        f.putIfAbsent("isAutoIncrement", false);
        f.putIfAbsent("showInList", true);
        f.putIfAbsent("showInForm", true);
        f.putIfAbsent("showInSearch", false);
        return f;
    }

    // ===== page =====
    private List<Map<String, Object>> buildPages(List<ReleaseItem> items) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (ReleaseItem it : items) {
            Map<String, Object> page = readJson(it, MAP_TR);
            if (page == null) continue;
            String pageCode = (String) page.get("pageCode");
            page.put("componentName", CodegenTypeMapper.toPascalCase(pageCode) + "Page");
            // layoutJson 是字符串嵌字符串；尽力解析方便模板按结构取值
            Object layoutJsonObj = page.get("layoutJson");
            if (layoutJsonObj instanceof String s && !s.isBlank()) {
                try {
                    page.put("layout", objectMapper.readValue(s, MAP_TR));
                } catch (Exception ex) {
                    log.debug("page {} layoutJson 解析失败 — 忽略: {}", pageCode, ex.getMessage());
                    page.put("layout", new LinkedHashMap<>());
                }
            } else {
                page.put("layout", new LinkedHashMap<>());
            }
            out.add(page);
        }
        return out;
    }

    // ===== dict =====
    private List<Map<String, Object>> buildDicts(List<ReleaseItem> items) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (ReleaseItem it : items) {
            Map<String, Object> snapshot = readJson(it, MAP_TR);
            if (snapshot == null) continue;
            @SuppressWarnings("unchecked")
            Map<String, Object> dict = (Map<String, Object>) snapshot.get("dict");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> dictItems = (List<Map<String, Object>>) snapshot.get("items");
            if (dict == null) continue;
            Map<String, Object> d = new LinkedHashMap<>(dict);
            d.put("items", dictItems == null ? new ArrayList<>() : dictItems);
            out.add(d);
        }
        return out;
    }

    // ===== component =====
    private List<Map<String, Object>> buildComponents(List<ReleaseItem> items) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (ReleaseItem it : items) {
            Map<String, Object> c = readJson(it, MAP_TR);
            if (c != null) out.add(c);
        }
        return out;
    }

    // ===== menu =====
    private List<Map<String, Object>> buildMenu(List<ReleaseItem> items) {
        if (items.isEmpty()) return new ArrayList<>();
        // menu 整树作为一条 ReleaseItem（itemCode=appCode）
        List<Map<String, Object>> tree = readJson(items.get(0), LIST_TR);
        return tree == null ? new ArrayList<>() : tree;
    }

    // ===== 工具 =====
    private <T> T readJson(ReleaseItem item, TypeReference<T> tr) {
        if (item.getSnapshotJson() == null) return null;
        try {
            return objectMapper.readValue(item.getSnapshotJson(), tr);
        } catch (Exception ex) {
            log.warn("release item {} 反序列化失败: {}", item.getId(), ex.getMessage());
            return null;
        }
    }

    private Integer toInt(Object o) {
        if (o == null) return null;
        if (o instanceof Number n) return n.intValue();
        try {
            return Integer.parseInt(String.valueOf(o));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String sanitizePackage(String appCode) {
        if (appCode == null || appCode.isBlank()) return "app";
        StringBuilder sb = new StringBuilder();
        for (char c : appCode.toLowerCase().toCharArray()) {
            if (Character.isLetterOrDigit(c)) {
                sb.append(c);
            } else if (c == '_' || c == '-') {
                sb.append('_');
            }
        }
        String s = sb.toString();
        if (s.isEmpty() || Character.isDigit(s.charAt(0))) {
            s = "app_" + s;
        }
        return s;
    }

    public Optional<Release> getRelease(Long releaseId) {
        return releaseRepository.findById(releaseId);
    }
}
