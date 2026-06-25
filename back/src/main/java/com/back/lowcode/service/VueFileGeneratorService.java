package com.back.lowcode.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Vue SFC 文件生成器 —— 将 layoutJson 转换为可下载的 .vue 文件
 * 
 * 支持两种 layoutJson 格式：
 * 1. 扁平化（PageViewer 风格）：[{id, type, x, y, width, height, label, props, ...}]
 * 2. 递归树（SchemaRenderer 风格）：[{id, compKey, label, props, style, children, ...}]
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VueFileGeneratorService {

    private final ObjectMapper objectMapper;

    /** 收集脚本中需要的数据变量名（避免重复声明） */
    private final Set<String> scriptDataVars = new LinkedHashSet<>();
    /** 收集用到的 TDesign 组件名 */
    private final Set<String> tdesignComponents = new LinkedHashSet<>();
    /** 收集用到的图标 */
    private final Set<String> usedIcons = new LinkedHashSet<>();

    /**
     * 生成完整的 Vue SFC 字符串
     */
    public String generate(String pageName, String layoutJson) {
        scriptDataVars.clear();
        tdesignComponents.clear();
        usedIcons.clear();

        StringBuilder sb = new StringBuilder();

        // 1. Template
        String template = generateTemplate(layoutJson);
        // 2. Script
        String script = generateScript(pageName);
        // 3. Style
        String style = generateStyle();

        sb.append("<template>\n");
        sb.append(template);
        sb.append("</template>\n\n");
        sb.append(script);
        sb.append("\n");
        sb.append(style);
        sb.append("\n");

        return sb.toString();
    }

    // ==================== Template 生成 ====================

    private String generateTemplate(String layoutJson) {
        if (layoutJson == null || layoutJson.isBlank() || "[]".equals(layoutJson.trim())) {
            return "  <div class=\"page-container\">\n    <t-empty description=\"空白页面\" />\n  </div>\n";
        }

        try {
            List<Map<String, Object>> elements = parseJson(layoutJson);
            if (elements == null || elements.isEmpty()) {
                return "  <div class=\"page-container\">\n    <t-empty description=\"空白页面\" />\n  </div>\n";
            }

            // 判断格式：有 x/y 字段为扁平格式，有 compKey 为树格式
            if (!elements.isEmpty()) {
                Map<String, Object> first = elements.get(0);
                if (first.containsKey("compKey") && first.containsKey("children")) {
                    // 树格式
                    StringBuilder treeTpl = new StringBuilder();
                    treeTpl.append("  <div class=\"page-container\">\n");
                    for (Map<String, Object> node : elements) {
                        treeTpl.append(renderTreeNode(node, 2));
                    }
                    treeTpl.append("  </div>\n");
                    return treeTpl.toString();
                } else {
                    // 扁平格式（PageViewer 绝对定位）
                    return generateFlatTemplate(elements);
                }
            }
        } catch (Exception e) {
            log.warn("解析 layoutJson 失败: {}", e.getMessage());
        }
        return "  <div class=\"page-container\">\n    <t-empty description=\"页面解析失败\" />\n  </div>\n";
    }

    /** 扁平格式：绝对定位布局 */
    private String generateFlatTemplate(List<Map<String, Object>> elements) {
        StringBuilder sb = new StringBuilder();
        sb.append("  <div class=\"page-viewer\">\n");
        for (Map<String, Object> el : elements) {
            String type = str(el.get("type"));
            String id = str(el.get("id"));
            Object x = el.get("x"), y = el.get("y");
            Object w = el.get("width"), h = el.get("height");
            String label = str(el.get("label"));

            String styleStr = String.format("left: %spx; top: %spx; width: %spx; height: %spx",
                    x != null ? x : 0, y != null ? y : 0, w != null ? w : 200, h != null ? h : 40);

            sb.append("    <div class=\"page-element\" style=\"").append(styleStr).append("\">\n");
            sb.append(renderLeafElement(type, id, label, el, "      "));
            renderChildElements(sb, el, "      ");
            sb.append("    </div>\n");
        }
        sb.append("  </div>\n");
        return sb.toString();
    }

    /** 递归渲染树节点 */
    private String renderTreeNode(Map<String, Object> node, int depth) {
        String indent = "  ".repeat(depth);
        String compKey = str(node.get("compKey"));
        String id = str(node.get("id"));
        String label = str(node.get("label"));
        @SuppressWarnings("unchecked")
        Map<String, Object> style = (Map<String, Object>) node.get("style");
        String styleStr = styleToCss(style);

        StringBuilder sb = new StringBuilder();

        // 容器类组件：递归子节点
        if (isContainer(compKey)) {
            String tag = containerTag(compKey, id, label, styleStr);
            sb.append(indent).append(tag);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> children = (List<Map<String, Object>>) node.get("children");
            if (children != null) {
                sb.append("\n");
                for (Map<String, Object> child : children) {
                    sb.append(renderTreeNode(child, depth + 1));
                }
                sb.append(indent);
            }
            sb.append(containerCloseTag(compKey));
            sb.append("\n");
        } else {
            // 叶子组件
            sb.append(indent).append(renderLeafElement(compKey, id, label, node, indent)).append("\n");
        }

        return sb.toString();
    }

    private void renderChildElements(StringBuilder sb, Map<String, Object> el, String indent) {
        // 扁平格式的子元素从 children 字段获取
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> children = (List<Map<String, Object>>) el.get("children");
        if (children != null) {
            for (Map<String, Object> child : children) {
                String childType = str(child.get("type"));
                String childId = str(child.get("id"));
                String childLabel = str(child.get("label"));
                sb.append(indent).append(renderLeafElement(childType, childId, childLabel, child, indent + "  ")).append("\n");
            }
        }
    }

    /** 渲染叶子组件 */
    private String renderLeafElement(String type, String id, String label,
                                      Map<String, Object> props, String indent) {
        String safeId = id != null ? id.replaceAll("[^a-zA-Z0-9_-]", "_") : "el";
        String safeLabel = escapeHtml(label != null ? label : "");
        String propStyle = getPropStyle(props);

        switch (type) {
            case "text":
                tdesignComponents.add("Tag");
                return "<span :style=\"" + propStyle + "\">{{ " + safeId + " }}</span>";

            case "button":
                tdesignComponents.add("Button");
                String btnTheme = str(props.getOrDefault("theme", props.getOrDefault("variant", "default")));
                return "<t-button theme=\"" + btnTheme + "\" :style=\"" + propStyle + "\">" + safeLabel + "</t-button>";

            case "link":
                return "<a href=\"#\" :style=\"" + propStyle + "\">" + safeLabel + "</a>";

            case "image":
                String imgSrc = str(props.getOrDefault("src", "#"));
                return "<img :src=\"" + imgSrc + "\" alt=\"" + safeLabel + "\" :style=\"" + propStyle + "\" />";

            case "input":
                tdesignComponents.add("Input");
                return "<t-input v-model=\"formData." + safeId + "\" placeholder=\"请输入" + safeLabel + "\" :style=\"" + propStyle + "\" />";

            case "textarea":
                tdesignComponents.add("Textarea");
                return "<t-textarea v-model=\"formData." + safeId + "\" placeholder=\"请输入" + safeLabel + "\" :style=\"" + propStyle + "\" />";

            case "inputNumber":
                tdesignComponents.add("InputNumber");
                return "<t-input-number v-model=\"formData." + safeId + "\" :style=\"" + propStyle + "\" />";

            case "select":
                tdesignComponents.add("Select");
                return "<t-select v-model=\"formData." + safeId + "\" :options=\"" + safeId + "Options\" placeholder=\"请选择" + safeLabel + "\" :style=\"" + propStyle + "\" />";

            case "date":
                tdesignComponents.add("DatePicker");
                return "<t-date-picker v-model=\"formData." + safeId + "\" :style=\"" + propStyle + "\" />";

            case "time":
                tdesignComponents.add("TimePicker");
                return "<t-time-picker v-model=\"formData." + safeId + "\" :style=\"" + propStyle + "\" />";

            case "switch":
                tdesignComponents.add("Switch");
                return "<t-switch v-model=\"formData." + safeId + "\" :style=\"" + propStyle + "\" />";

            case "checkbox":
                tdesignComponents.add("Checkbox");
                return "<t-checkbox v-model=\"formData." + safeId + "\" :style=\"" + propStyle + "\">" + safeLabel + "</t-checkbox>";

            case "radio":
                tdesignComponents.add("RadioGroup");
                return "<t-radio-group v-model=\"formData." + safeId + "\" :style=\"" + propStyle + "\" />";

            case "slider":
                tdesignComponents.add("Slider");
                return "<t-slider v-model=\"formData." + safeId + "\" :style=\"" + propStyle + "\" />";

            case "rate":
                tdesignComponents.add("Rate");
                return "<t-rate v-model=\"formData." + safeId + "\" :style=\"" + propStyle + "\" />";

            case "upload":
                tdesignComponents.add("Upload");
                return "<t-upload :style=\"" + propStyle + "\" />";

            case "table":
                tdesignComponents.add("Table");
                scriptDataVars.add("tableData");
                scriptDataVars.add("tableColumns");
                scriptDataVars.add("tableLoading");
                scriptDataVars.add("pagination");
                return "<t-table :data=\"tableData\" :columns=\"tableColumns\" :loading=\"tableLoading\" :pagination=\"pagination\" row-key=\"id\" stripe hover :style=\"" + propStyle + "\" />";

            case "form":
                tdesignComponents.add("Form");
                return "<t-form :data=\"formData\" :style=\"" + propStyle + "\">\n" + indent + "  <!-- 表单字段在此 -->\n" + indent + "</t-form>";

            case "card":
                tdesignComponents.add("Card");
                return "<t-card title=\"" + safeLabel + "\" :style=\"" + propStyle + "\">\n" + indent + "  <!-- 卡片内容 -->\n" + indent + "</t-card>";

            case "tag":
                tdesignComponents.add("Tag");
                return "<t-tag :style=\"" + propStyle + "\">" + safeLabel + "</t-tag>";

            case "progress":
                tdesignComponents.add("Progress");
                return "<t-progress :percentage=\"" + str(props.getOrDefault("percentage", "50")) + "\" :style=\"" + propStyle + "\" />";

            case "steps":
                tdesignComponents.add("Steps");
                return "<t-steps :style=\"" + propStyle + "\" />";

            case "alert":
                tdesignComponents.add("Alert");
                return "<t-alert theme=\"info\" :style=\"" + propStyle + "\">" + safeLabel + "</t-alert>";

            case "divider":
                tdesignComponents.add("Divider");
                return "<t-divider :style=\"" + propStyle + "\" />";

            case "breadcrumb":
                tdesignComponents.add("Breadcrumb");
                return "<t-breadcrumb :style=\"" + propStyle + "\" />";

            case "list":
                return "<div v-for=\"(item, idx) in " + safeId + "List\" :key=\"idx\" :style=\"" + propStyle + "\">\n" + indent + "  {{ item }}\n" + indent + "</div>";

            case "chart":
                return "<div ref=\"" + safeId + "Ref\" :style=\"" + propStyle + "\"></div>";

            default:
                return "<!-- 未识别组件: " + type + " -->\n" + indent + "<div :style=\"" + propStyle + "\">" + safeLabel + "</div>";
        }
    }

    private boolean isContainer(String compKey) {
        return Set.of("container", "grid", "tabs", "collapse", "space", "card", "form").contains(compKey);
    }

    private String containerTag(String compKey, String id, String label, String style) {
        switch (compKey) {
            case "container": return "<div :style=\"" + style + "\">";
            case "grid":
                tdesignComponents.add("Row");
                tdesignComponents.add("Col");
                return "<t-row :style=\"" + style + "\">\n  <t-col :span=\"12\">";
            case "tabs":
                tdesignComponents.add("Tabs");
                tdesignComponents.add("TabPanel");
                return "<t-tabs :style=\"" + style + "\">\n  <t-tab-panel value=\"tab1\" label=\"标签1\">";
            case "collapse":
                tdesignComponents.add("Collapse");
                tdesignComponents.add("CollapsePanel");
                return "<t-collapse :style=\"" + style + "\">\n  <t-collapse-panel header=\"" + escapeHtml(label) + "\">";
            case "space":
                tdesignComponents.add("Space");
                return "<t-space :style=\"" + style + "\">";
            case "card":
                tdesignComponents.add("Card");
                return "<t-card title=\"" + escapeHtml(label) + "\" :style=\"" + style + "\">";
            case "form":
                tdesignComponents.add("Form");
                return "<t-form :data=\"formData\" :style=\"" + style + "\">";
            default: return "<div :style=\"" + style + "\">";
        }
    }

    private String containerCloseTag(String compKey) {
        switch (compKey) {
            case "container": return "</div>";
            case "grid": return "</t-col>\n</t-row>";
            case "tabs": return "</t-tab-panel>\n</t-tabs>";
            case "collapse": return "</t-collapse-panel>\n</t-collapse>";
            case "space": return "</t-space>";
            case "card": return "</t-card>";
            case "form": return "</t-form>";
            default: return "</div>";
        }
    }

    // ==================== Script 生成 ====================

    private String generateScript(String pageName) {
        StringBuilder sb = new StringBuilder();
        sb.append("<script setup lang=\"ts\">\n");
        sb.append("import { ref, reactive, onMounted } from 'vue'\n");

        // TDesign 组件导入
        if (!tdesignComponents.isEmpty()) {
            // 图标
            String icons = usedIcons.stream().sorted().collect(Collectors.joining(", "));
            if (!icons.isEmpty()) {
                sb.append("import { ").append(icons).append(" } from 'tdesign-icons-vue-next'\n");
            }
            String comps = tdesignComponents.stream().sorted().collect(Collectors.joining(", "));
            sb.append("import { ").append(comps).append(" } from 'tdesign-vue-next'\n");
        }

        sb.append("\n");

        // formData（如果模板中有用到表单字段）
        sb.append("const formData = reactive<Record<string, any>>({})\n");

        // 表相关变量
        if (scriptDataVars.contains("tableData")) {
            sb.append("const tableData = ref<any[]>([])\n");
            sb.append("const tableColumns = ref<any[]>([])\n");
            sb.append("const tableLoading = ref(false)\n");
            sb.append("const pagination = ref({ current: 1, pageSize: 10, total: 0 })\n");
        }

        sb.append("\n");
        sb.append("onMounted(() => {\n");
        sb.append("  // 页面初始化逻辑\n");
        if (scriptDataVars.contains("tableData")) {
            sb.append("  fetchData()\n");
        }
        sb.append("})\n\n");

        if (scriptDataVars.contains("tableData")) {
            sb.append("async function fetchData(params?: Record<string, any>) {\n");
            sb.append("  tableLoading.value = true\n");
            sb.append("  try {\n");
            sb.append("    // TODO: 调用接口获取数据\n");
            sb.append("    // const res = await api.list(params)\n");
            sb.append("    // tableData.value = res.data.records || []\n");
            sb.append("    // pagination.value.total = res.data.total || 0\n");
            sb.append("  } finally {\n");
            sb.append("    tableLoading.value = false\n");
            sb.append("  }\n");
            sb.append("}\n");
        }

        sb.append("</script>\n");
        return sb.toString();
    }

    // ==================== Style 生成 ====================

    private String generateStyle() {
        return "<style scoped>\n" +
               ".page-container {\n" +
               "  padding: 24px;\n" +
               "  background: #fff;\n" +
               "  min-height: 400px;\n" +
               "  border-radius: 8px;\n" +
               "}\n" +
               ".page-viewer {\n" +
               "  position: relative;\n" +
               "  min-height: 600px;\n" +
               "  background: #f7f8fa;\n" +
               "  border-radius: 8px;\n" +
               "  overflow: auto;\n" +
               "}\n" +
               ".page-element {\n" +
               "  position: absolute;\n" +
               "  box-sizing: border-box;\n" +
               "}\n" +
               "</style>";
    }

    // ==================== 工具方法 ====================

    private List<Map<String, Object>> parseJson(String json) throws Exception {
        return objectMapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {});
    }

    private String str(Object obj) {
        return obj == null ? "" : obj.toString();
    }

    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }

    private String getPropStyle(Map<String, Object> props) {
        // 从 props 中提取 style
        @SuppressWarnings("unchecked")
        Map<String, Object> ps = (Map<String, Object>) props.get("props");
        if (ps != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> style = (Map<String, Object>) ps.get("style");
            if (style != null) return styleToCss(style);
        }
        // 直接 style
        @SuppressWarnings("unchecked")
        Map<String, Object> style = (Map<String, Object>) props.get("style");
        if (style != null) return styleToCss(style);
        return "{}";
    }

    private String styleToCss(Map<String, Object> style) {
        if (style == null || style.isEmpty()) return "{}";
        StringBuilder sb = new StringBuilder("{");
        for (Map.Entry<String, Object> e : style.entrySet()) {
            // 驼峰转短横线
            String key = e.getKey().replaceAll("([A-Z])", "-$1").toLowerCase();
            sb.append(key).append(": '").append(e.getValue()).append("', ");
        }
        sb.append("}");
        return sb.toString().replace(", }", " }");
    }
}
