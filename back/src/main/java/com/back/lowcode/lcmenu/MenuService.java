package com.back.lowcode.lcmenu;

import com.back.lowcode.entity.Menu;
import com.back.lowcode.entity.Release;
import com.back.lowcode.repository.MenuRepository;
import com.back.lowcode.repository.ReleaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 应用菜单服务
 * <p>
 * 草稿态 / 快照态切换由 {@link Menu#getReleaseId()} 决定：
 * <ul>
 *   <li>设计器只读写 {@code release_id IS NULL} 的草稿菜单</li>
 *   <li>{@code MountPhase} 在发布流水线中调 {@link #cloneDraftToRelease} 把草稿克隆为快照</li>
 *   <li>终端运行时调 {@link #getActiveMenuTree} 读 active release 的快照树</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;
    private final ReleaseRepository releaseRepository;

    /** 设计器读草稿菜单（树形） */
    public List<MenuNode> getDraftTree(String appCode) {
        List<Menu> flat = menuRepository.findDraftMenu(appCode);
        return buildTree(flat);
    }

    /** 终端用户读当前 active release 的菜单（树形）；无 active release 时返回空 */
    public List<MenuNode> getActiveMenuTree(String appCode) {
        Optional<Release> active = releaseRepository.findByAppCodeAndStatus(appCode, "active");
        if (active.isEmpty()) {
            return List.of();
        }
        List<Menu> flat = menuRepository.findReleaseMenu(appCode, active.get().getId());
        return buildTree(flat);
    }

    /** 读指定 release 的菜单（用于 release 详情页 / 回滚预览） */
    public List<MenuNode> getMenuTreeByRelease(String appCode, Long releaseId) {
        return buildTree(menuRepository.findReleaseMenu(appCode, releaseId));
    }

    /**
     * 全量保存草稿：删旧 + 插新。
     * 入参是设计器提交的树（含或不含 id），方法按层级递归落库；
     * 完成后返回的树带新生成的 id，供前端继续在新 id 上做局部 reorder。
     */
    @Transactional
    public List<MenuNode> saveDraft(String appCode, List<MenuNode> tree) {
        menuRepository.deleteDraftByApp(appCode);
        return saveTreeRecursive(appCode, null, tree);
    }

    /** 拖拽局部排序：批量更新 parentId / sortOrder */
    @Transactional
    public void reorder(String appCode, List<ReorderItem> items) {
        for (ReorderItem it : items) {
            Menu m = menuRepository.findById(it.getId())
                    .orElseThrow(() -> new IllegalArgumentException("菜单不存在: " + it.getId()));
            if (!appCode.equals(m.getAppCode()) || m.getReleaseId() != null) {
                throw new IllegalStateException("仅允许调整当前应用的草稿菜单: id=" + it.getId());
            }
            m.setParentId(it.getParentId());
            m.setSortOrder(it.getSortOrder() == null ? 0 : it.getSortOrder());
        }
    }

    /**
     * 由 MountPhase 调用：把当前草稿菜单克隆为快照行（{@code release_id = releaseId}）。
     * 保留草稿不动，未来仍在草稿上迭代。
     */
    @Transactional
    public int cloneDraftToRelease(String appCode, Long releaseId) {
        List<Menu> draft = menuRepository.findDraftMenu(appCode);
        if (draft.isEmpty()) {
            return 0;
        }
        // 先按草稿 id -> 新快照 Menu 建映射，第二轮用映射回填 parentId
        Map<Long, Menu> map = new HashMap<>();
        for (Menu d : draft) {
            Menu copy = Menu.builder()
                    .appCode(d.getAppCode())
                    .releaseId(releaseId)
                    .parentId(null) // 占位，下面回填
                    .name(d.getName())
                    .icon(d.getIcon())
                    .sortOrder(d.getSortOrder())
                    .pageCode(d.getPageCode())
                    .routePath(d.getRoutePath())
                    .menuType(d.getMenuType())
                    .visible(d.getVisible())
                    .build();
            map.put(d.getId(), copy);
        }
        // 先入库根节点拿到 id，再分层入库；这里采用更简单的两轮：
        // 第一轮 saveAll(map.values())；第二轮根据 draftId -> copyId 回填 parentId 后再 save。
        List<Menu> savedRoots = new ArrayList<>();
        for (Menu d : draft) {
            Menu copy = map.get(d.getId());
            if (d.getParentId() == null) {
                Menu persisted = menuRepository.save(copy);
                savedRoots.add(persisted);
                map.put(d.getId(), persisted);
            }
        }
        // 非根节点：按 draft.parentId 引用对应已保存 copy.id
        for (Menu d : draft) {
            if (d.getParentId() == null) continue;
            Menu parentCopy = map.get(d.getParentId());
            if (parentCopy == null || parentCopy.getId() == null) {
                // 父节点尚未保存（草稿数据有问题），跳过该子树
                continue;
            }
            Menu copy = map.get(d.getId());
            copy.setParentId(parentCopy.getId());
            Menu persisted = menuRepository.save(copy);
            map.put(d.getId(), persisted);
        }
        return draft.size();
    }

    /** 序列化 release 菜单快照为 JSON 字符串（供 ReleaseItem.snapshotJson 使用） */
    public List<Menu> listReleaseMenuFlat(String appCode, Long releaseId) {
        return menuRepository.findReleaseMenu(appCode, releaseId);
    }

    // ----------------- 私有工具 -----------------

    private List<MenuNode> saveTreeRecursive(String appCode, Long parentDbId, List<MenuNode> nodes) {
        if (nodes == null || nodes.isEmpty()) return List.of();
        List<MenuNode> result = new ArrayList<>();
        int order = 0;
        for (MenuNode n : nodes) {
            Menu m = Menu.builder()
                    .appCode(appCode)
                    .releaseId(null)
                    .parentId(parentDbId)
                    .name(n.getName())
                    .icon(n.getIcon())
                    .sortOrder(n.getSortOrder() == null ? order : n.getSortOrder())
                    .pageCode(n.getPageCode())
                    .routePath(n.getRoutePath())
                    .menuType(n.getMenuType() == null ? "menu" : n.getMenuType())
                    .visible(n.getVisible() == null ? Boolean.TRUE : n.getVisible())
                    .build();
            Menu saved = menuRepository.save(m);
            n.setId(saved.getId());
            n.setParentId(parentDbId);
            n.setSortOrder(saved.getSortOrder());
            List<MenuNode> children = saveTreeRecursive(appCode, saved.getId(), n.getChildren());
            n.setChildren(children);
            result.add(n);
            order++;
        }
        return result;
    }

    private List<MenuNode> buildTree(List<Menu> flat) {
        // parentId -> list
        Map<Long, List<Menu>> byParent = new HashMap<>();
        for (Menu m : flat) {
            byParent.computeIfAbsent(m.getParentId() == null ? 0L : m.getParentId(), k -> new ArrayList<>()).add(m);
        }
        return assembleChildren(byParent, 0L);
    }

    private List<MenuNode> assembleChildren(Map<Long, List<Menu>> byParent, Long parentKey) {
        List<Menu> rows = byParent.get(parentKey);
        if (rows == null) return new ArrayList<>();
        rows.sort(Comparator.comparingInt((Menu x) -> x.getSortOrder() == null ? 0 : x.getSortOrder())
                .thenComparing(Menu::getId));
        List<MenuNode> result = new ArrayList<>();
        for (Menu m : rows) {
            MenuNode n = MenuNode.from(m);
            n.setChildren(assembleChildren(byParent, m.getId()));
            result.add(n);
        }
        return result;
    }
}
