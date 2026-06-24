package com.back.lowcode.codegen;

import com.back.common.Result;
import com.back.lowcode.entity.Release;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 代码生成 REST 接口
 * <ul>
 *   <li>POST /lowcode/codegen/preview/{releaseId} —— 内存预览：返回 path → content map</li>
 *   <li>POST /lowcode/codegen/build/{releaseId}   —— 落盘到 target/generated/{appCode}/{version}/</li>
 *   <li>GET  /lowcode/codegen/download/{releaseId} —— 直接以 zip 流返回</li>
 * </ul>
 * <p>
 * preview 不写盘，便于设计器在 Monaco 编辑器里实时显示；
 * build 是显式动作，主要用于 release 流水线归档；
 * download 不依赖 build，临时打包后流式输出。
 */
@Slf4j
@RestController
@RequestMapping("/lowcode/codegen")
@RequiredArgsConstructor
public class CodegenController {

    private final CodegenService codegenService;
    private final CodegenContext codegenContext;

    @PostMapping("/preview/{releaseId}")
    public Result preview(@PathVariable Long releaseId) {
        Map<String, String> files = codegenService.generate(releaseId);
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("releaseId", releaseId);
        resp.put("fileCount", files.size());
        resp.put("files", files);
        return Result.success(resp);
    }

    @PostMapping("/build/{releaseId}")
    public Result build(@PathVariable Long releaseId) {
        Release release = codegenContext.getRelease(releaseId)
                .orElseThrow(() -> new IllegalArgumentException("Release 不存在: id=" + releaseId));
        Map<String, String> files = codegenService.generate(releaseId);

        Path baseDir = Paths.get("target", "generated",
                release.getAppCode(), release.getVersion());
        int written = 0;
        try {
            Files.createDirectories(baseDir);
            for (Map.Entry<String, String> e : files.entrySet()) {
                Path target = baseDir.resolve(e.getKey());
                Files.createDirectories(target.getParent());
                Files.writeString(target, e.getValue(), StandardCharsets.UTF_8);
                written++;
            }
        } catch (IOException io) {
            log.error("build 写盘失败: {}", io.getMessage(), io);
            return Result.error("写盘失败: " + io.getMessage());
        }

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("releaseId", releaseId);
        resp.put("appCode", release.getAppCode());
        resp.put("version", release.getVersion());
        resp.put("baseDir", baseDir.toAbsolutePath().toString());
        resp.put("fileCount", written);
        return Result.success(resp);
    }

    @GetMapping("/download/{releaseId}")
    public void download(@PathVariable Long releaseId, HttpServletResponse response) throws IOException {
        Release release = codegenContext.getRelease(releaseId)
                .orElseThrow(() -> new IllegalArgumentException("Release 不存在: id=" + releaseId));
        Map<String, String> files = codegenService.generate(releaseId);

        String filename = release.getAppCode() + "-v" + release.getVersion() + ".zip";
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + filename + "\"");

        try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream(), StandardCharsets.UTF_8)) {
            for (Map.Entry<String, String> e : files.entrySet()) {
                ZipEntry entry = new ZipEntry(e.getKey());
                zos.putNextEntry(entry);
                writeContent(zos, e.getValue());
                zos.closeEntry();
            }
            zos.finish();
        }
    }

    private void writeContent(OutputStream os, String content) throws IOException {
        if (content == null) return;
        os.write(content.getBytes(StandardCharsets.UTF_8));
    }
}
