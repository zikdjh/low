package com.back.lowcode.codegen.controller;

import com.back.common.Result;
import com.back.lowcode.codegen.dto.CodeGenPreviewDTO;
import com.back.lowcode.codegen.service.CodeGenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 低代码代码生成器 REST 入口。
 *
 * <ul>
 *   <li>{@code GET /lowcode/codegen/preview/{entityId}} — 预览，返回 path -> content</li>
 *   <li>{@code GET /lowcode/codegen/download/{entityId}} — 打包 zip 下载</li>
 * </ul>
 */
@RestController
@RequestMapping("/lowcode/codegen")
@RequiredArgsConstructor
public class CodeGenController {

    private final CodeGenService codeGenService;

    @GetMapping("/preview/{entityId}")
    public Result preview(@PathVariable Long entityId) {
        CodeGenPreviewDTO dto = codeGenService.preview(entityId);
        return Result.success(dto);
    }

    @GetMapping("/download/{entityId}")
    public ResponseEntity<byte[]> download(@PathVariable Long entityId) {
        byte[] zip = codeGenService.downloadZip(entityId);
        String fileName = codeGenService.preview(entityId).getClassName() + "-codegen.zip";
        String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.set(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + fileName + "\"; filename*=UTF-8''" + encoded);
        headers.setContentLength(zip.length);
        return new ResponseEntity<>(zip, headers, org.springframework.http.HttpStatus.OK);
    }

    /**
     * 安装到当前项目源码目录 —— 重启后端 + 重启前端 dev server 后生效。
     *
     * @param force {@code true} 强制覆盖已存在文件，默认 {@code false}
     */
    @PostMapping("/install/{entityId}")
    public Result install(@PathVariable Long entityId,
                          @RequestParam(defaultValue = "false") boolean force) {
        CodeGenService.InstallResult r = codeGenService.installToProject(entityId, force);
        Map<String, Object> body = Map.of(
                "className", r.className,
                "entityCode", r.entityCode,
                "projectRoot", r.projectRoot.toString(),
                "written", r.written,
                "skipped", r.skipped,
                "force", r.force,
                "note", r.skipped.isEmpty()
                        ? "全部文件已写入，请重启后端和前端 dev server 后访问 /lowcode/gen/" + r.entityCode + "/list"
                        : "存在 " + r.skipped.size() + " 个文件已存在被跳过，如需覆盖请传 force=true"
        );
        return Result.success(body);
    }
}
