package com.back.lowcode.release.pipeline;

import com.back.lowcode.entity.Release;
import com.back.lowcode.repository.ReleaseLogRepository;
import com.back.lowcode.repository.ReleaseRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Phase 5: activate — 原子切换当前 active release
 * <p>
 * 将 release.status 置为 active；若已存在 active release，标记为 rolledback。
 * 通过 ReleaseService 包在事务里保证原子性。
 */
@Component
public class ActivatePhase extends ReleasePhase {

    private final ReleaseRepository releaseRepository;

    public ActivatePhase(ReleaseLogRepository releaseLogRepository,
                         ReleaseRepository releaseRepository) {
        super(releaseLogRepository);
        this.releaseRepository = releaseRepository;
    }

    @Override
    public String name() {
        return "activate";
    }

    @Override
    protected void doExecute(Release release) {
        // 1. 把同 app 下旧 active 标 rolledback
        Optional<Release> oldActive = releaseRepository.findByAppCodeAndStatus(release.getAppCode(), "active");
        if (oldActive.isPresent() && !oldActive.get().getId().equals(release.getId())) {
            Release old = oldActive.get();
            old.setStatus("rolledback");
            releaseRepository.save(old);
        }
        // 2. 当前 release 置 active
        release.setStatus("active");
        release.setActivatedAt(LocalDateTime.now());
        releaseRepository.save(release);
    }
}
