package com.back.lowcode.release.pipeline;

import com.back.lowcode.entity.Release;
import com.back.lowcode.lcmenu.MenuService;
import com.back.lowcode.repository.MenuRepository;
import com.back.lowcode.repository.ReleaseLogRepository;
import org.springframework.stereotype.Component;

/**
 * Phase 4: mount — 把草稿菜单挂载为本次 release 的快照行
 * <p>
 * 实现策略：
 * <ol>
 *   <li>清空 {@code lc_app_menu} 中 {@code release_id = release.id} 的旧行（容错重跑）</li>
 *   <li>调 {@link MenuService#cloneDraftToRelease} 把当前应用草稿菜单复制到本 release</li>
 * </ol>
 * 草稿菜单本身不动；终端运行时通过 active release 的快照菜单获取一致视图。
 */
@Component
public class MountPhase extends ReleasePhase {

    private final MenuService menuService;
    private final MenuRepository menuRepository;

    public MountPhase(ReleaseLogRepository releaseLogRepository,
                      MenuService menuService,
                      MenuRepository menuRepository) {
        super(releaseLogRepository);
        this.menuService = menuService;
        this.menuRepository = menuRepository;
    }

    @Override
    public String name() {
        return "mount";
    }

    @Override
    protected void doExecute(Release release) {
        menuRepository.deleteByReleaseId(release.getId());
        menuService.cloneDraftToRelease(release.getAppCode(), release.getId());
    }
}
