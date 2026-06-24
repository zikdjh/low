package com.back.lowcode.release.pipeline;

import com.back.lowcode.codegen.CodegenService;
import com.back.lowcode.entity.Release;
import com.back.lowcode.repository.ReleaseLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Phase 3: generate — 代码生成阶段
 * <p>
 * M3 接入：调用 {@link CodegenService} 渲染所有 Freemarker 模板，产物落
 * {@code target/generated/{appCode}/{version}/}。
 * <p>
 * 设计原则（见 05-改造方案 §5.5）：generate 失败不阻塞 release，仅记录 warn —
 * 因此本 phase 内部捕获异常并降级为 success+warning，让 mount/activate 继续。
 */
@Slf4j
@Component
public class GeneratePhase extends ReleasePhase {

    private final CodegenService codegenService;

    public GeneratePhase(ReleaseLogRepository releaseLogRepository,
                        CodegenService codegenService) {
        super(releaseLogRepository);
        this.codegenService = codegenService;
    }

    @Override
    public String name() {
        return "generate";
    }

    @Override
    protected void doExecute(Release release) {
        try {
            Map<String, String> files = codegenService.generate(release.getId());
            Path baseDir = Paths.get("target", "generated",
                    release.getAppCode(), release.getVersion());
            Files.createDirectories(baseDir);
            for (Map.Entry<String, String> e : files.entrySet()) {
                Path target = baseDir.resolve(e.getKey());
                Files.createDirectories(target.getParent());
                Files.writeString(target, e.getValue(), StandardCharsets.UTF_8);
            }
            log.info("[release#{}] generate ok: {} files → {}",
                    release.getId(), files.size(), baseDir.toAbsolutePath());
        } catch (IOException io) {
            // 不抛出 — generate 阶段失败不阻塞 release
            log.warn("[release#{}] generate 写盘失败（不阻塞）: {}",
                    release.getId(), io.getMessage());
        } catch (Exception ex) {
            log.warn("[release#{}] generate 渲染失败（不阻塞）: {}",
                    release.getId(), ex.getMessage());
        }
    }
}
