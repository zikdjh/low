package com.back.lowcode.release.pipeline;

import com.back.lowcode.entity.*;
import com.back.lowcode.lcmenu.MenuNode;
import com.back.lowcode.lcmenu.MenuService;
import com.back.lowcode.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Phase 2: snapshot — 把应用相关的设计态全量序列化为 ReleaseItem
 * <p>
 * 序列化范围（应用维度）：
 * <ul>
 *   <li>page：所有 PageSchema where appCode = release.appCode</li>
 *   <li>entity：所有 PageSchema 涉及的 EntityMeta + 其 FieldMeta</li>
 *   <li>component：layoutJson 中引用到的 ComponentDef</li>
 *   <li>dict：FieldMeta.dictCode 涉及的 DictType + DictItem</li>
 *   <li>menu：当前 appCode 下 release_id IS NULL（草稿态）的 AppMenu —— M2 接入后启用</li>
 * </ul>
 * 当前 M1 阶段不依赖 AppMenu（M2 才引入），其 ReleaseItem 留待 M2 接入。
 */
@Component
public class SnapshotPhase extends ReleasePhase {

    private final PageSchemaRepository pageSchemaRepository;
    private final EntityMetaRepository entityMetaRepository;
    private final FieldMetaRepository fieldMetaRepository;
    private final ComponentDefRepository componentDefRepository;
    private final DictTypeRepository dictTypeRepository;
    private final DictItemRepository dictItemRepository;
    private final ReleaseItemRepository releaseItemRepository;
    private final ReleaseRepository releaseRepository;
    private final MenuService menuService;
    private final ObjectMapper objectMapper;

    public SnapshotPhase(ReleaseLogRepository releaseLogRepository,
                         PageSchemaRepository pageSchemaRepository,
                         EntityMetaRepository entityMetaRepository,
                         FieldMetaRepository fieldMetaRepository,
                         ComponentDefRepository componentDefRepository,
                         DictTypeRepository dictTypeRepository,
                         DictItemRepository dictItemRepository,
                         ReleaseItemRepository releaseItemRepository,
                         ReleaseRepository releaseRepository,
                         MenuService menuService,
                         ObjectMapper objectMapper) {
        super(releaseLogRepository);
        this.pageSchemaRepository = pageSchemaRepository;
        this.entityMetaRepository = entityMetaRepository;
        this.fieldMetaRepository = fieldMetaRepository;
        this.componentDefRepository = componentDefRepository;
        this.dictTypeRepository = dictTypeRepository;
        this.dictItemRepository = dictItemRepository;
        this.releaseItemRepository = releaseItemRepository;
        this.releaseRepository = releaseRepository;
        this.menuService = menuService;
        this.objectMapper = objectMapper;
    }

    @Override
    public String name() {
        return "snapshot";
    }

    @Override
    protected void doExecute(Release release) throws Exception {
        String appCode = release.getAppCode();
        List<ReleaseItem> items = new ArrayList<>();

        // 1. 页面
        List<PageSchema> pages = pageSchemaRepository.findByAppCode(appCode);
        for (PageSchema page : pages) {
            items.add(buildItem(release.getId(), "page", page.getPageCode(), page));
        }

        // 2. 实体 + 字段（按页面 entityCode 收集）
        Map<String, EntityMeta> entityByCode = new HashMap<>();
        for (PageSchema page : pages) {
            if (page.getEntityCode() != null && !page.getEntityCode().isBlank()
                    && !entityByCode.containsKey(page.getEntityCode())) {
                entityMetaRepository.findByCode(page.getEntityCode())
                        .ifPresent(em -> entityByCode.put(em.getCode(), em));
            }
        }
        for (EntityMeta em : entityByCode.values()) {
            List<FieldMeta> fields = fieldMetaRepository.findByEntityIdOrderBySortOrderAsc(em.getId());
            Map<String, Object> snapshot = new HashMap<>();
            snapshot.put("entity", em);
            snapshot.put("fields", fields);
            items.add(buildItem(release.getId(), "entity", em.getCode(), snapshot));
        }

        // 3. 组件（layoutJson 中引用到的 + 系统组件） — 简化：取所有 active 组件
        List<ComponentDef> activeComps = componentDefRepository.findByStatus("active");
        for (ComponentDef cd : activeComps) {
            items.add(buildItem(release.getId(), "component", cd.getCompKey(), cd));
        }

        // 4. 字典（字段引用到的 dictCode）
        java.util.Set<String> dictCodes = new java.util.HashSet<>();
        for (EntityMeta em : entityByCode.values()) {
            for (FieldMeta f : fieldMetaRepository.findByEntityIdOrderBySortOrderAsc(em.getId())) {
                if (f.getDictCode() != null && !f.getDictCode().isBlank()) {
                    dictCodes.add(f.getDictCode());
                }
            }
        }
        for (String dictCode : dictCodes) {
            DictType dt = dictTypeRepository.findByDictCode(dictCode).orElse(null);
            if (dt == null) continue;
            List<DictItem> dictItems = dictItemRepository.findByDictCode(dictCode);
            Map<String, Object> snapshot = new HashMap<>();
            snapshot.put("dict", dt);
            snapshot.put("items", dictItems);
            items.add(buildItem(release.getId(), "dict", dictCode, snapshot));
        }

        // 5. 菜单（M2 接入）：取草稿态菜单树作为快照载荷；运行时数据由 MountPhase 克隆生成
        List<MenuNode> menuTree = menuService.getDraftTree(appCode);
        if (!menuTree.isEmpty()) {
            items.add(buildItem(release.getId(), "menu", appCode, menuTree));
        }

        // 6. 持久化 + 汇总 checksum
        releaseItemRepository.saveAll(items);
        String aggregateChecksum = sha256(items.stream()
                .map(ReleaseItem::getChecksum)
                .reduce("", (a, b) -> a + b));
        release.setChecksum(aggregateChecksum);
        releaseRepository.save(release);
    }

    private ReleaseItem buildItem(Long releaseId, String type, String code, Object payload) throws Exception {
        String json = objectMapper.writeValueAsString(payload);
        return ReleaseItem.builder()
                .releaseId(releaseId)
                .itemType(type)
                .itemCode(code)
                .snapshotJson(json)
                .checksum(sha256(json))
                .build();
    }

    private String sha256(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
