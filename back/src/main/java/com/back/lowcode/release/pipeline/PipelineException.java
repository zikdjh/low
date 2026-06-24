package com.back.lowcode.release.pipeline;

/**
 * 流水线阶段受控失败 — 任意 phase 抛出此异常即触发整体回滚
 */
public class PipelineException extends RuntimeException {

    private final String phase;

    public PipelineException(String phase, String message, Throwable cause) {
        super("[" + phase + "] " + message, cause);
        this.phase = phase;
    }

    public String getPhase() {
        return phase;
    }
}
