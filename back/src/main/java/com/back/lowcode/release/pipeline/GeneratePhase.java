package com.back.lowcode.release.pipeline;

import com.back.lowcode.entity.Release;
import com.back.lowcode.repository.ReleaseLogRepository;
import org.springframework.stereotype.Component;

/**
 * Phase 3: generate — 代码生成阶段
 * <p>
 * M1 阶段留空 stub，仅记录 skipped 日志。M3 落地后接入 FreeMarker 模板渲染，
 * 产物落 {@code target/generated/{appCode}/{version}/} 并写入 {@code lc_release_item(itemType='codegen')}。
 */
@Component
public class GeneratePhase extends ReleasePhase {

    public GeneratePhase(ReleaseLogRepository releaseLogRepository) {
        super(releaseLogRepository);
    }

    @Override
    public String name() {
        return "generate";
    }

    @Override
    protected void doExecute(Release release) {
        // TODO M3: 调 CodegenService.build(releaseId) — 此处 stub
    }
}
