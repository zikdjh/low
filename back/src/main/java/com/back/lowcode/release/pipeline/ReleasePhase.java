package com.back.lowcode.release.pipeline;

import com.back.lowcode.entity.Release;
import com.back.lowcode.entity.ReleaseLog;
import com.back.lowcode.repository.ReleaseLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 发布流水线阶段基类 — 模板方法封装计时、日志、异常包装
 * <p>
 * 各阶段实现 {@link #doExecute} 即可，公共逻辑（写 {@code lc_release_log}、duration_ms）由基类处理。
 * 任何阶段抛 {@link PipelineException} 视为受控失败，{@link ReleaseService} 据此触发整体回滚。
 */
@Slf4j
@RequiredArgsConstructor
public abstract class ReleasePhase {

    protected final ReleaseLogRepository releaseLogRepository;

    /** 阶段名，写入 lc_release_log.phase */
    public abstract String name();

    /** 阶段实际逻辑 */
    protected abstract void doExecute(Release release) throws Exception;

    /** 执行入口 — 不抛异常，结果记入 release log，失败时包装抛出 */
    public final void execute(Release release) {
        long start = System.currentTimeMillis();
        String phase = name();
        try {
            doExecute(release);
            long duration = System.currentTimeMillis() - start;
            writeLog(release.getId(), phase, "success", null, duration);
            log.info("[release#{}] phase={} success duration={}ms", release.getId(), phase, duration);
        } catch (Exception ex) {
            long duration = System.currentTimeMillis() - start;
            String msg = ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
            writeLog(release.getId(), phase, "failure", msg, duration);
            log.warn("[release#{}] phase={} failure: {}", release.getId(), phase, msg);
            throw new PipelineException(phase, msg, ex);
        }
    }

    protected void writeLog(Long releaseId, String phase, String result, String message, long durationMs) {
        ReleaseLog rl = ReleaseLog.builder()
                .releaseId(releaseId)
                .phase(phase)
                .result(result)
                .message(message)
                .durationMs(durationMs)
                .build();
        releaseLogRepository.save(rl);
    }
}
