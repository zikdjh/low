package com.back.lowcode.release.seed;

import com.back.lowcode.entity.*;
import com.back.lowcode.repository.*;
import com.back.lowcode.service.DDLService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 启动时从 {@code classpath:seed/*.json} 反向回灌 release 快照到数据库。
 * <p>
 * 触发条件：每个 seed 文件对应 release 在 {@code lc_release} 中无任何记录（{@code findByAppCodeAndVersion} 空）。
 * 已存在则跳过；这样允许把 seed 当作"出厂数据"且不会与设计器的后续修改冲突。
 * <p>
 * 回灌顺序（与 {@link com.back.lowcode.release.pipeline.SnapshotPhase} 反向对齐）：
 * <ol>
 *   <li>BusinessApp（按 code upsert）</li>
 *   <li>entity ReleaseItem → EntityMeta + FieldMeta + DDLService.generateCreateTable（status=published）</li>
 *   <li>component / dict ReleaseItem → ComponentDef / DictType+DictItem（按自然 key upsert）</li>
 *   <li>page ReleaseItem → PageSchema（按 pageCode upsert，status=published）</li>
 *   <li>Release 行 + ReleaseItem 原样回灌（保留 snapshotJson / checksum 不变）</li>
 *   <li>menu 行 —— 用 exportId/exportParentId 重建树并按 release_id 入 lc_app_menu，
 *       同时按草稿态（release_id=null）再写一份，便于设计器立即可编辑</li>
 *   <li>把 release 设为 active</li>
 * </ol>
 * 设计取舍：物理表已存在时跳过 CREATE TABLE，避免冲突；DDL 错误仅 warn 不中断（业务库的实际 schema
 * 可能由 {@code LeaveDataInitializer} 既已建好）。
 */
@Slf4j
@Component
@Order(50)
public class SeedReleaseLoader implements ApplicationRunner {

    private static final String SEED_LOCATION = "classpath:seed/*.json";

    private final ReleaseRepository releaseRepository;
    private final ReleaseItemRepository releaseItemRepository;
    private final MenuRepository menuRepository;
    private final BusinessAppRepository businessAppRepository;
    private final EntityMetaRepository entityMetaRepository;
    private final FieldMetaRepository fieldMetaRepository;
    private final PageSchemaRepository pageSchemaRepository;
    private final ComponentDefRepository componentDefRepository;
    private final DictTypeRepository dictTypeRepository;
    private final DictItemRepository dictItemRepository;
    private final DDLService ddlService;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final SeedReleaseLoader self;

    public SeedReleaseLoader(ReleaseRepository releaseRepository,
                             ReleaseItemRepository releaseItemRepository,
                             MenuRepository menuRepository,
                             BusinessAppRepository businessAppRepository,
                             EntityMetaRepository entityMetaRepository,
                             FieldMetaRepository fieldMetaRepository,
                             PageSchemaRepository pageSchemaRepository,
                             ComponentDefRepository componentDefRepository,
                             DictTypeRepository dictTypeRepository,
                             DictItemRepository dictItemRepository,
                             DDLService ddlService,
                             JdbcTemplate jdbcTemplate,
                             ObjectMapper objectMapper,
                             @Lazy @Autowired SeedReleaseLoader self) {
        this.releaseRepository = releaseRepository;
        this.releaseItemRepository = releaseItemRepository;
        this.menuRepository = menuRepository;
        this.businessAppRepository = businessAppRepository;
        this.entityMetaRepository = entityMetaRepository;
        this.fieldMetaRepository = fieldMetaRepository;
        this.pageSchemaRepository = pageSchemaRepository;
        this.componentDefRepository = componentDefRepository;
        this.dictTypeRepository = dictTypeRepository;
        this.dictItemRepository = dictItemRepository;
        this.ddlService = ddlService;
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
        this.self = self;
    }

    @Override
    public void run(org.springframework.boot.ApplicationArguments args) {
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources;
        try {
            resources = resolver.getResources(SEED_LOCATION);
        } catch (IOException io) {
            log.warn("[seed] 解析 {} 失败：{}（启用 seed 目录或忽略）", SEED_LOCATION, io.getMessage());
            return;
        }
        if (resources.length == 0) {
            log.debug("[seed] 无 seed 文件，跳过");
            return;
        }
        for (Resource r : resources) {
            try (InputStream is = r.getInputStream()) {
                String name = r.getFilename();
                JsonNode root = objectMapper.readTree(
                        new String(is.readAllBytes(), StandardCharsets.UTF_8));
                self.loadOne(name == null ? "(unnamed)" : name, root);
            } catch (Exception ex) {
                log.error("[seed] 加载 {} 失败：{}", r.getFilename(), ex.getMessage(), ex);
            }
        }
    }

    @Transactional
    public void loadOne(String fileName, JsonNode root) throws Exception {
        JsonNode meta = root.path("release");
        String appCode = meta.path("appCode").asText(null);
        String version = meta.path("version").asText(null);
        if (appCode == null || version == null) {
            log.warn("[seed:{}] release.appCode/version 缺失，跳过", fileName);
            return;
        }
        if (releaseRepository.findByAppCodeAndVersion(appCode, version).isPresent()) {
            log.info("[seed:{}] release {} v{} 已存在，跳过", fileName, appCode, version);
            return;
        }

        log.info("[seed:{}] 开始回灌 release {} v{}", fileName, appCode, version);

        // 1. BusinessApp upsert
        JsonNode appNode = root.path("businessApp");
        if (appNode.isObject()) {
            upsertBusinessApp(appNode);
        }

        // 2-4. items 三阶段：先 entity/dict/component，后 page —— 后者会查 entity_code
        JsonNode itemsNode = root.path("items");
        for (JsonNode item : itemsNode) {
            String type = item.path("itemType").asText();
            if ("entity".equals(type)) {
                upsertEntity(item);
            } else if ("dict".equals(type)) {
                upsertDict(item);
            } else if ("component".equals(type)) {
                upsertComponent(item);
            }
        }
        for (JsonNode item : itemsNode) {
            if ("page".equals(item.path("itemType").asText())) {
                upsertPage(item);
            }
        }

        // 5. Release 行（先 draft，最后再 active）
        Release release = Release.builder()
                .appCode(appCode)
                .version(version)
                .status("draft")
                .notes(orNull(meta.path("notes")))
                .checksum(orNull(meta.path("checksum")))
                .createdBy(orNull(meta.path("createdBy")))
                .build();
        release = releaseRepository.save(release);

        // ReleaseItem 原样回灌 —— 保留 snapshotJson / checksum
        List<ReleaseItem> riList = new ArrayList<>();
        for (JsonNode item : itemsNode) {
            ReleaseItem ri = ReleaseItem.builder()
                    .releaseId(release.getId())
                    .itemType(item.path("itemType").asText())
                    .itemCode(item.path("itemCode").asText())
                    .snapshotJson(item.path("snapshotJson").asText())
                    .checksum(item.path("checksum").asText())
                    .build();
            riList.add(ri);
        }
        releaseItemRepository.saveAll(riList);

        // 6. menu 行 —— 写 release 快照 + 草稿
        JsonNode menusNode = root.path("menus");
        if (menusNode.isArray() && menusNode.size() > 0) {
            cloneMenusFromExport(menusNode, appCode, release.getId());
            cloneMenusFromExport(menusNode, appCode, null);
        }

        // 7. 切 active
        Release activeOpt = release;
        releaseRepository.findByAppCodeAndStatus(appCode, "active").ifPresent(active -> {
            if (!active.getId().equals(activeOpt.getId())) {
                active.setStatus("rolledback");
                releaseRepository.save(active);
            }
        });
        release.setStatus("active");
        release.setActivatedAt(LocalDateTime.now());
        releaseRepository.save(release);

        log.info("[seed:{}] 回灌完成：release#{} {} v{} (items={})",
                fileName, release.getId(), appCode, version, riList.size());
    }

    // ---------------- upsert helpers ----------------

    private void upsertBusinessApp(JsonNode node) {
        String code = node.path("code").asText(null);
        if (code == null) return;
        BusinessApp app = businessAppRepository.findByCode(code).orElseGet(BusinessApp::new);
        app.setCode(code);
        app.setName(orNull(node.path("name")));
        app.setDescription(orNull(node.path("description")));
        app.setIcon(orNull(node.path("icon")));
        app.setColor(orNull(node.path("color")));
        businessAppRepository.save(app);
    }

    private void upsertEntity(JsonNode item) throws Exception {
        JsonNode snap = objectMapper.readTree(item.path("snapshotJson").asText());
        JsonNode entityNode = snap.path("entity");
        JsonNode fieldsNode = snap.path("fields");

        String code = entityNode.path("code").asText();
        EntityMeta em = entityMetaRepository.findByCode(code).orElseGet(EntityMeta::new);
        em.setCode(code);
        em.setName(orNull(entityNode.path("name")));
        em.setTableName(orNull(entityNode.path("tableName")));
        em.setDescription(orNull(entityNode.path("description")));
        em.setStatus("published");
        em = entityMetaRepository.save(em);

        Map<String, FieldMeta> existing = fieldMetaRepository
                .findByEntityIdOrderBySortOrderAsc(em.getId()).stream()
                .collect(Collectors.toMap(FieldMeta::getCode, f -> f));
        List<FieldMeta> toSave = new ArrayList<>();
        for (JsonNode f : fieldsNode) {
            String fc = f.path("code").asText();
            FieldMeta fm = existing.getOrDefault(fc, new FieldMeta());
            fm.setEntityId(em.getId());
            fm.setCode(fc);
            fm.setName(orNull(f.path("name")));
            fm.setColumnName(orNull(f.path("columnName")));
            fm.setFieldType(orNull(f.path("fieldType")));
            fm.setLength(intOrNull(f.path("length")));
            fm.setPrecision(intOrNull(f.path("precision")));
            fm.setScale(intOrNull(f.path("scale")));
            fm.setNullable(boolOr(f.path("nullable"), true));
            fm.setDefaultValue(orNull(f.path("defaultValue")));
            fm.setIsPrimaryKey(boolOr(f.path("isPrimaryKey"), false));
            fm.setIsAutoIncrement(boolOr(f.path("isAutoIncrement"), false));
            fm.setSortOrder(intOr(f.path("sortOrder"), 0));
            fm.setShowInList(boolOr(f.path("showInList"), true));
            fm.setShowInForm(boolOr(f.path("showInForm"), true));
            fm.setShowInSearch(boolOr(f.path("showInSearch"), false));
            fm.setDictCode(orNull(f.path("dictCode")));
            fm.setReferenceEntityCode(orNull(f.path("referenceEntityCode")));
            fm.setReferenceDisplayFieldCode(orNull(f.path("referenceDisplayFieldCode")));
            fm.setValidationRule(orNull(f.path("validationRule")));
            toSave.add(fm);
        }
        fieldMetaRepository.saveAll(toSave);

        try {
            if (!tableExists(em.getTableName())) {
                ddlService.generateCreateTable(em, toSave);
            }
        } catch (Exception ex) {
            log.warn("[seed] DDL {} 失败：{}（业务表可能已由 LeaveDataInitializer 建好，可忽略）",
                    em.getTableName(), ex.getMessage());
        }
    }

    private void upsertDict(JsonNode item) throws Exception {
        JsonNode snap = objectMapper.readTree(item.path("snapshotJson").asText());
        JsonNode dictNode = snap.path("dict");
        JsonNode itemsArr = snap.path("items");

        String code = dictNode.path("dictCode").asText();
        DictType dt = dictTypeRepository.findByDictCode(code).orElseGet(DictType::new);
        dt.setDictCode(code);
        dt.setName(orNull(dictNode.path("name")));
        dt.setDescription(orNull(dictNode.path("description")));
        dt.setIsSystem(boolOr(dictNode.path("isSystem"), false));
        dt.setStatus("active");
        dictTypeRepository.save(dt);

        List<DictItem> existing = dictItemRepository.findByDictCode(code);
        if (!existing.isEmpty()) {
            dictItemRepository.deleteAll(existing);
        }
        List<DictItem> toInsert = new ArrayList<>();
        for (JsonNode di : itemsArr) {
            DictItem d = new DictItem();
            d.setDictCode(code);
            d.setItemKey(orNull(di.path("itemKey")));
            d.setItemValue(orNull(di.path("itemValue")));
            d.setSortOrder(intOr(di.path("sortOrder"), 0));
            d.setColor(orNull(di.path("color")));
            d.setStatus(or(di.path("status"), "active"));
            toInsert.add(d);
        }
        dictItemRepository.saveAll(toInsert);
    }

    private void upsertComponent(JsonNode item) throws Exception {
        JsonNode c = objectMapper.readTree(item.path("snapshotJson").asText());
        String key = c.path("compKey").asText();
        ComponentDef cd = componentDefRepository.findByCompKey(key).orElseGet(ComponentDef::new);
        cd.setCompKey(key);
        cd.setName(orNull(c.path("name")));
        cd.setCategory(orNull(c.path("category")));
        cd.setIcon(orNull(c.path("icon")));
        cd.setDescription(orNull(c.path("description")));
        cd.setDefaultPropsJson(orNull(c.path("defaultPropsJson")));
        cd.setPropsSchemaJson(orNull(c.path("propsSchemaJson")));
        cd.setGroupIndex(intOrNull(c.path("groupIndex")));
        cd.setIsSystem(boolOr(c.path("isSystem"), true));
        cd.setStatus(or(c.path("status"), "active"));
        componentDefRepository.save(cd);
    }

    private void upsertPage(JsonNode item) throws Exception {
        JsonNode p = objectMapper.readTree(item.path("snapshotJson").asText());
        String pageCode = p.path("pageCode").asText();
        PageSchema ps = pageSchemaRepository.findByPageCode(pageCode).orElseGet(PageSchema::new);
        ps.setPageCode(pageCode);
        ps.setName(orNull(p.path("name")));
        ps.setLayoutJson(orNull(p.path("layoutJson")));
        ps.setPageType(or(p.path("pageType"), "list"));
        ps.setEntityCode(orNull(p.path("entityCode")));
        ps.setVersion(intOr(p.path("version"), 1));
        ps.setStatus("published");
        ps.setDescription(orNull(p.path("description")));
        ps.setAppCode(orNull(p.path("appCode")));
        if (ps.getPublishedAt() == null) {
            ps.setPublishedAt(LocalDateTime.now());
        }
        pageSchemaRepository.save(ps);
    }

    /** 用 exportId / exportParentId 重建菜单父子关系，落 release_id（null 则为草稿） */
    private void cloneMenusFromExport(JsonNode menusNode, String appCode, Long releaseId) {
        Map<Long, Long> exportIdToNewId = new LinkedHashMap<>();
        // 第一轮：根节点
        for (JsonNode m : menusNode) {
            Long exportParentId = longOrNull(m.path("exportParentId"));
            if (exportParentId != null) continue;
            Menu menu = newMenuFromNode(m, appCode, releaseId, null);
            menu = menuRepository.save(menu);
            exportIdToNewId.put(longOrNull(m.path("exportId")), menu.getId());
        }
        // 第二轮：子节点，multi-pass 直到全部映射或轮数耗尽
        int rounds = 0;
        while (exportIdToNewId.size() < menusNode.size() && rounds < menusNode.size() + 1) {
            for (JsonNode m : menusNode) {
                Long exportId = longOrNull(m.path("exportId"));
                if (exportIdToNewId.containsKey(exportId)) continue;
                Long exportParentId = longOrNull(m.path("exportParentId"));
                Long newParentId = exportIdToNewId.get(exportParentId);
                if (newParentId == null) continue;
                Menu menu = newMenuFromNode(m, appCode, releaseId, newParentId);
                menu = menuRepository.save(menu);
                exportIdToNewId.put(exportId, menu.getId());
            }
            rounds++;
        }
    }

    private Menu newMenuFromNode(JsonNode n, String appCode, Long releaseId, Long parentId) {
        return Menu.builder()
                .appCode(appCode)
                .releaseId(releaseId)
                .parentId(parentId)
                .name(orNull(n.path("name")))
                .icon(orNull(n.path("icon")))
                .sortOrder(intOr(n.path("sortOrder"), 0))
                .pageCode(orNull(n.path("pageCode")))
                .routePath(orNull(n.path("routePath")))
                .menuType(or(n.path("menuType"), "menu"))
                .visible(boolOr(n.path("visible"), true))
                .build();
    }

    // ---------------- info_schema 检查 ----------------

    private boolean tableExists(String tableName) {
        try {
            Integer cnt = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = ?",
                    Integer.class, tableName);
            return cnt != null && cnt > 0;
        } catch (Exception ex) {
            return false;
        }
    }

    // ---------------- JsonNode utils ----------------

    private String orNull(JsonNode n) {
        if (n == null || n.isNull() || n.isMissingNode()) return null;
        if (n.isTextual()) return n.asText();
        return n.toString();
    }

    private String or(JsonNode n, String def) {
        String v = orNull(n);
        return v == null ? def : v;
    }

    private Integer intOrNull(JsonNode n) {
        if (n == null || n.isNull() || n.isMissingNode()) return null;
        if (n.isNumber()) return n.asInt();
        try { return Integer.parseInt(n.asText()); } catch (Exception ex) { return null; }
    }

    private int intOr(JsonNode n, int def) {
        Integer v = intOrNull(n);
        return v == null ? def : v;
    }

    private Long longOrNull(JsonNode n) {
        if (n == null || n.isNull() || n.isMissingNode()) return null;
        if (n.isNumber()) return n.asLong();
        try { return Long.parseLong(n.asText()); } catch (Exception ex) { return null; }
    }

    private boolean boolOr(JsonNode n, boolean def) {
        if (n == null || n.isNull() || n.isMissingNode()) return def;
        if (n.isBoolean()) return n.asBoolean();
        return Boolean.parseBoolean(n.asText());
    }
}
