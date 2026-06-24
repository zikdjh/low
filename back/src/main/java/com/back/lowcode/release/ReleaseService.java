package com.back.lowcode.release;

import com.back.lowcode.entity.Release;
import com.back.lowcode.entity.ReleaseItem;
import com.back.lowcode.entity.ReleaseLog;
import com.back.lowcode.release.pipeline.*;
import com.back.lowcode.repository.ReleaseItemRepository;
import com.back.lowcode.repository.ReleaseLogRepository;
import com.back.lowcode.repository.ReleaseRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 发布编排服务 — 串联 validate → snapshot → generate → mount → activate 五个阶段
 * <p>
 * 事务策略：
 * <ul>
 *   <li>{@link #createDraftRelease} —— REQUIRES_NEW，先落库占位</li>
 *   <li>{@link #runPipeline} —— REQUIRED，五阶段共享一个事务，任一失败整体回滚</li>
 *   <li>{@link #markReleaseFailed} —— REQUIRES_NEW，runPipeline 回滚后单独写入失败标记</li>
 * </ul>
 * 通过 self-reference（@Lazy 注入自身代理）触发 @Transactional 代理逻辑。
 */
@Slf4j
@Service
public class ReleaseService {

    private final ReleaseRepository releaseRepository;
    private final ReleaseItemRepository releaseItemRepository;
    private final ReleaseLogRepository releaseLogRepository;

    private final ValidatePhase validatePhase;
    private final SnapshotPhase snapshotPhase;
    private final GeneratePhase generatePhase;
    private final MountPhase mountPhase;
    private final ActivatePhase activatePhase;

    /** 通过 self 调用以触发 @Transactional 代理 */
    private final ReleaseService self;

    public ReleaseService(ReleaseRepository releaseRepository,
                          ReleaseItemRepository releaseItemRepository,
                          ReleaseLogRepository releaseLogRepository,
                          ValidatePhase validatePhase,
                          SnapshotPhase snapshotPhase,
                          GeneratePhase generatePhase,
                          MountPhase mountPhase,
                          ActivatePhase activatePhase,
                          @Lazy @Autowired ReleaseService self) {
        this.releaseRepository = releaseRepository;
        this.releaseItemRepository = releaseItemRepository;
        this.releaseLogRepository = releaseLogRepository;
        this.validatePhase = validatePhase;
        this.snapshotPhase = snapshotPhase;
        this.generatePhase = generatePhase;
        this.mountPhase = mountPhase;
        this.activatePhase = activatePhase;
        this.self = self;
    }

    /**
     * 发起一次发布。
     *
     * @param appCode     应用编码
     * @param versionBump major | minor | patch（基于上一版本号自动递增）
     * @param notes       发布说明
     * @param createdBy   发布人（可空）
     * @param activate    是否在流水线末尾激活；false 时只生成 draft release
     */
    public Release publish(String appCode, String versionBump, String notes, String createdBy, boolean activate) {
        String version = computeNextVersion(appCode, versionBump);
        Release release = self.createDraftRelease(appCode, version, notes, createdBy);
        try {
            self.runPipeline(release, activate);
        } catch (PipelineException ex) {
            self.markReleaseFailed(release.getId(), ex.getMessage());
            throw new RuntimeException("发布失败 @ phase=" + ex.getPhase() + ": " + ex.getMessage(), ex);
        }
        return releaseRepository.findById(release.getId()).orElse(release);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Release createDraftRelease(String appCode, String version, String notes, String createdBy) {
        if (releaseRepository.existsByAppCodeAndVersion(appCode, version)) {
            throw new RuntimeException("版本号已存在: " + appCode + " " + version);
        }
        Release release = Release.builder()
                .appCode(appCode)
                .version(version)
                .status("draft")
                .notes(notes)
                .createdBy(createdBy)
                .build();
        return releaseRepository.save(release);
    }

    @Transactional
    public void runPipeline(Release release, boolean activate) {
        validatePhase.execute(release);
        snapshotPhase.execute(release);
        generatePhase.execute(release);
        mountPhase.execute(release);
        if (activate) {
            activatePhase.execute(release);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markReleaseFailed(Long releaseId, String message) {
        releaseItemRepository.deleteByReleaseId(releaseId);
        Optional<Release> opt = releaseRepository.findById(releaseId);
        if (opt.isPresent()) {
            Release r = opt.get();
            r.setStatus("failed");
            releaseRepository.save(r);
        }
        ReleaseLog rl = ReleaseLog.builder()
                .releaseId(releaseId)
                .phase("pipeline")
                .result("failure")
                .message(message)
                .durationMs(0L)
                .build();
        releaseLogRepository.save(rl);
    }

    @Transactional
    public Release rollback(Long releaseId) {
        Release target = releaseRepository.findById(releaseId)
                .orElseThrow(() -> new RuntimeException("Release 不存在: " + releaseId));
        if (!"rolledback".equals(target.getStatus()) && !"draft".equals(target.getStatus())) {
            throw new RuntimeException("只能回滚到 rolledback 或 draft 状态的 release，当前=" + target.getStatus());
        }
        releaseRepository.findByAppCodeAndStatus(target.getAppCode(), "active").ifPresent(active -> {
            active.setStatus("rolledback");
            releaseRepository.save(active);
        });
        target.setStatus("active");
        target.setActivatedAt(LocalDateTime.now());
        return releaseRepository.save(target);
    }

    @Transactional
    public Release activate(Long releaseId) {
        Release target = releaseRepository.findById(releaseId)
                .orElseThrow(() -> new RuntimeException("Release 不存在: " + releaseId));
        if (!"draft".equals(target.getStatus())) {
            throw new RuntimeException("只能激活 draft 状态的 release，当前=" + target.getStatus());
        }
        releaseRepository.findByAppCodeAndStatus(target.getAppCode(), "active").ifPresent(active -> {
            active.setStatus("rolledback");
            releaseRepository.save(active);
        });
        target.setStatus("active");
        target.setActivatedAt(LocalDateTime.now());
        return releaseRepository.save(target);
    }

    public List<Release> listByApp(String appCode) {
        return releaseRepository.findByAppCodeOrderByCreatedAtDesc(appCode);
    }

    public Release getById(Long id) {
        return releaseRepository.findById(id).orElse(null);
    }

    public List<ReleaseLog> getLogs(Long releaseId) {
        return releaseLogRepository.findByReleaseIdOrderByCreatedAtAsc(releaseId);
    }

    public List<ReleaseItem> getItems(Long releaseId) {
        return releaseItemRepository.findByReleaseId(releaseId);
    }

    /** 基于历史最新版本号 + bump 类型生成下一版本号 */
    private String computeNextVersion(String appCode, String bump) {
        List<Release> history = releaseRepository.findByAppCodeOrderByCreatedAtDesc(appCode);
        int[] cur = {1, 0, 0};
        if (!history.isEmpty()) {
            String last = history.get(0).getVersion();
            String[] parts = last.split("\\.");
            try {
                cur[0] = Integer.parseInt(parts[0]);
                cur[1] = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
                cur[2] = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
            } catch (NumberFormatException ignore) {
                // 回退到默认值
            }
            switch (bump == null ? "patch" : bump.toLowerCase()) {
                case "major" -> { cur[0]++; cur[1] = 0; cur[2] = 0; }
                case "minor" -> { cur[1]++; cur[2] = 0; }
                default -> cur[2]++;
            }
        } else {
            // 首次发布固定为 1.0.0，不再 bump
            return "1.0.0";
        }
        return cur[0] + "." + cur[1] + "." + cur[2];
    }
}
