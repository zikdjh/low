package com.back.lowcode.release.pipeline;

import com.back.lowcode.entity.*;
import com.back.lowcode.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Phase 1: validate — 在序列化前确保设计态数据自洽
 * <ul>
 *   <li>应用必须存在</li>
 *   <li>所有 PageSchema.layoutJson 可反序列化为合法 JSON 树</li>
 *   <li>所有 layoutJson 中引用的 componentKey 在 lc_component_def 中存在</li>
 *   <li>所有 FieldMeta.referenceEntityCode 都能找到对应实体</li>
 * </ul>
 */
@Component
public class ValidatePhase extends ReleasePhase {

    private final BusinessAppRepository businessAppRepository;
    private final PageSchemaRepository pageSchemaRepository;
    private final EntityMetaRepository entityMetaRepository;
    private final FieldMetaRepository fieldMetaRepository;
    private final ComponentDefRepository componentDefRepository;
    private final ObjectMapper objectMapper;

    public ValidatePhase(ReleaseLogRepository releaseLogRepository,
                         BusinessAppRepository businessAppRepository,
                         PageSchemaRepository pageSchemaRepository,
                         EntityMetaRepository entityMetaRepository,
                         FieldMetaRepository fieldMetaRepository,
                         ComponentDefRepository componentDefRepository,
                         ObjectMapper objectMapper) {
        super(releaseLogRepository);
        this.businessAppRepository = businessAppRepository;
        this.pageSchemaRepository = pageSchemaRepository;
        this.entityMetaRepository = entityMetaRepository;
        this.fieldMetaRepository = fieldMetaRepository;
        this.componentDefRepository = componentDefRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public String name() {
        return "validate";
    }

    @Override
    protected void doExecute(Release release) throws Exception {
        String appCode = release.getAppCode();

        // 1. 应用必须存在
        BusinessApp app = businessAppRepository.findByCode(appCode)
                .orElseThrow(() -> new IllegalStateException("应用不存在: " + appCode));

        // 2. 收集应用下的页面
        List<PageSchema> pages = pageSchemaRepository.findByAppCode(appCode);
        if (pages.isEmpty()) {
            throw new IllegalStateException("应用下没有任何页面，无法发布: " + appCode);
        }

        // 3. 校验 layoutJson 合法 + 收集所引用的 componentKey
        Set<String> referencedComps = new HashSet<>();
        for (PageSchema page : pages) {
            if (page.getLayoutJson() == null || page.getLayoutJson().isBlank()) {
                continue;
            }
            try {
                var root = objectMapper.readTree(page.getLayoutJson());
                collectComponentKeys(root, referencedComps);
            } catch (Exception ex) {
                throw new IllegalStateException("页面 " + page.getPageCode() + " 的 layoutJson 非法: " + ex.getMessage());
            }
        }

        // 4. 已引用的组件必须在 lc_component_def 注册
        for (String compKey : referencedComps) {
            if (!componentDefRepository.existsByCompKey(compKey)) {
                throw new IllegalStateException("引用的组件未注册: " + compKey);
            }
        }

        // 5. 字段外键闭环：referenceEntityCode 必须能找到实体
        // 现阶段全平台校验（不区分应用），保证元数据自洽
        List<EntityMeta> allEntities = entityMetaRepository.findAll();
        Set<String> entityCodes = new HashSet<>();
        for (EntityMeta em : allEntities) {
            entityCodes.add(em.getCode());
        }
        for (EntityMeta em : allEntities) {
            List<FieldMeta> fields = fieldMetaRepository.findByEntityIdOrderBySortOrderAsc(em.getId());
            for (FieldMeta f : fields) {
                String ref = f.getReferenceEntityCode();
                if (ref != null && !ref.isBlank() && !entityCodes.contains(ref)) {
                    throw new IllegalStateException("字段 " + em.getCode() + "." + f.getCode()
                            + " 引用的实体不存在: " + ref);
                }
            }
        }
    }

    /** 递归从 layoutJson 树中提取所有 componentKey / type 字段值 */
    private void collectComponentKeys(com.fasterxml.jackson.databind.JsonNode node, Set<String> out) {
        if (node == null || node.isNull()) {
            return;
        }
        if (node.isObject()) {
            // 常见键：compKey / componentKey / type / component
            for (String key : new String[]{"compKey", "componentKey", "component"}) {
                if (node.has(key) && node.get(key).isTextual()) {
                    out.add(node.get(key).asText());
                }
            }
            node.fields().forEachRemaining(e -> collectComponentKeys(e.getValue(), out));
        } else if (node.isArray()) {
            node.forEach(child -> collectComponentKeys(child, out));
        }
    }
}
