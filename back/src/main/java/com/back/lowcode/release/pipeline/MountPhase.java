package com.back.lowcode.release.pipeline;

import com.back.lowcode.entity.Release;
import com.back.lowcode.repository.ReleaseLogRepository;
import org.springframework.stereotype.Component;

/**
 * Phase 4: mount — 把 release 关联的菜单结构写入 lc_app_menu
 * <p>
 * M1 阶段留空 stub。M2（菜单与路由挂载）落地后写入：
 * 拷贝当前 appCode 下 release_id IS NULL（草稿态）的 AppMenu 行，
 * 将 release_id 置为当前 release.id，形成菜单的不可变快照。
 */
@Component
public class MountPhase extends ReleasePhase {

    public MountPhase(ReleaseLogRepository releaseLogRepository) {
        super(releaseLogRepository);
    }

    @Override
    public String name() {
        return "mount";
    }

    @Override
    protected void doExecute(Release release) {
        // TODO M2: 拷贝草稿 AppMenu 到 release_id 下 — 此处 stub
    }
}
