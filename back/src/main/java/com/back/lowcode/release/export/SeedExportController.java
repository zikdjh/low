package com.back.lowcode.release.export;

import com.back.common.Result;
import com.back.lowcode.entity.BusinessApp;
import com.back.lowcode.entity.Menu;
import com.back.lowcode.entity.Release;
import com.back.lowcode.entity.ReleaseItem;
import com.back.lowcode.repository.BusinessAppRepository;
import com.back.lowcode.repository.MenuRepository;
import com.back.lowcode.repository.ReleaseItemRepository;
import com.back.lowcode.repository.ReleaseRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 把一次 release 导出为可直接落到 {@code back/src/main/resources/seed/} 的 JSON 种子文件。
 * <p>
 * 序列化形态与 {@link com.back.lowcode.release.pipeline.SnapshotPhase} 写入的
 * {@code lc_release_item.snapshot_json} 完全对齐，外加 BusinessApp 行 + Menu 行 + Release 元数据，
 * 由 {@code SeedReleaseLoader} 启动时反向回灌。
 * <ul>
 *   <li>{@code GET /lowcode/release/{id}/export}        —— 返回 Result 包装的 JSON（便于人工查看）</li>
 *   <li>{@code GET /lowcode/release/{id}/export/file}   —— 直接以下载方式返回纯 JSON 文件流（用于 IDE 内一键 Save As）</li>
 * </ul>
 * 注意：本接口不修改任何数据，仅做读侧导出。
 */
@Slf4j
@RestController
@RequestMapping("/lowcode/release")
@RequiredArgsConstructor
public class SeedExportController {

    private final ReleaseRepository releaseRepository;
    private final ReleaseItemRepository releaseItemRepository;
    private final MenuRepository menuRepository;
    private final BusinessAppRepository businessAppRepository;
    private final ObjectMapper objectMapper;

    @GetMapping("/{id}/export")
    public Result export(@PathVariable Long id) {
        return Result.success(buildSeed(id));
    }

    @GetMapping("/{id}/export/file")
    public void exportFile(@PathVariable Long id,
                           @RequestParam(value = "pretty", required = false, defaultValue = "true") boolean pretty,
                           HttpServletResponse response) throws IOException {
        Map<String, Object> seed = buildSeed(id);
        Release release = releaseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Release 不存在: " + id));
        String filename = release.getAppCode() + "-v" + release.getVersion() + ".json";
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + filename + "\"");
        ObjectMapper writer = objectMapper.copy();
        if (pretty) {
            writer.enable(SerializationFeature.INDENT_OUTPUT);
        }
        try (OutputStream os = response.getOutputStream()) {
            os.write(writer.writeValueAsString(seed).getBytes(StandardCharsets.UTF_8));
        }
    }

    /** 构造与 SeedReleaseLoader 反向消费形态一一对应的 seed map */
    private Map<String, Object> buildSeed(Long releaseId) {
        Release release = releaseRepository.findById(releaseId)
                .orElseThrow(() -> new IllegalArgumentException("Release 不存在: " + releaseId));

        List<ReleaseItem> items = releaseItemRepository.findByReleaseId(releaseId);
        // Menu 取该 release 自身的快照行（MountPhase 已克隆草稿至 release_id）
        List<Menu> menus = menuRepository.findReleaseMenu(release.getAppCode(), releaseId);
        BusinessApp app = businessAppRepository.findByCode(release.getAppCode()).orElse(null);

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("schemaVersion", 1);
        root.put("release", releaseMeta(release));
        root.put("businessApp", app);
        root.put("items", items.stream().map(this::itemView).toList());
        root.put("menus", menus.stream().map(this::menuView).toList());
        return root;
    }

    /** 只导出业务可重放字段，剥掉 id / activatedAt / createdAt */
    private Map<String, Object> releaseMeta(Release r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("appCode", r.getAppCode());
        m.put("version", r.getVersion());
        m.put("status", r.getStatus());
        m.put("notes", r.getNotes());
        m.put("checksum", r.getChecksum());
        m.put("createdBy", r.getCreatedBy());
        return m;
    }

    private Map<String, Object> itemView(ReleaseItem item) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("itemType", item.getItemType());
        m.put("itemCode", item.getItemCode());
        m.put("checksum", item.getChecksum());
        m.put("snapshotJson", item.getSnapshotJson());
        return m;
    }

    /** Menu 行也剥 id —— 重放时主键由 SeedReleaseLoader 重建并按 parent 引用关系重新拼接 */
    private Map<String, Object> menuView(Menu menu) {
        Map<String, Object> m = new LinkedHashMap<>();
        // 用导出时的 id 做"业务 key"，让 reload 端可以根据这个值串联 parent
        m.put("exportId", menu.getId());
        m.put("exportParentId", menu.getParentId());
        m.put("appCode", menu.getAppCode());
        m.put("name", menu.getName());
        m.put("icon", menu.getIcon());
        m.put("sortOrder", menu.getSortOrder());
        m.put("pageCode", menu.getPageCode());
        m.put("routePath", menu.getRoutePath());
        m.put("menuType", menu.getMenuType());
        m.put("visible", menu.getVisible());
        return m;
    }
}
