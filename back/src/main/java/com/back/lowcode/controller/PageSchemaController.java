package com.back.lowcode.controller;

import com.back.common.Result;
import com.back.lowcode.entity.PageSchema;
import com.back.lowcode.service.PageSchemaService;
import com.back.lowcode.service.VueFileGeneratorService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/lowcode/page")
@RequiredArgsConstructor
public class PageSchemaController {

    private final PageSchemaService pageSchemaService;
    private final VueFileGeneratorService vueFileGeneratorService;

    @GetMapping
    public Result getAllPages() {
        return Result.success(pageSchemaService.getAllPages());
    }

    @GetMapping("/status/{status}")
    public Result getPagesByStatus(@PathVariable String status) {
        return Result.success(pageSchemaService.getPagesByStatus(status));
    }

    @GetMapping("/code/{pageCode}")
    public Result getByPageCode(@PathVariable String pageCode) {
        PageSchema page = pageSchemaService.getByPageCode(pageCode);
        if (page == null) {
            return Result.error("页面不存在");
        }
        return Result.success(page);
    }

    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        PageSchema page = pageSchemaService.getById(id);
        if (page == null) {
            return Result.error("页面不存在");
        }
        return Result.success(page);
    }

    @PostMapping
    public Result create(@RequestBody PageSchema pageSchema) {
        return Result.success(pageSchemaService.create(pageSchema));
    }

    @PutMapping("/{id}")
    public Result update(@PathVariable Long id, @RequestBody PageSchema pageSchema) {
        return Result.success(pageSchemaService.update(id, pageSchema));
    }

    @PostMapping("/{id}/publish")
    public Result publish(@PathVariable Long id) {
        return Result.success(pageSchemaService.publish(id));
    }

    @PostMapping("/{id}/unpublish")
    public Result unpublish(@PathVariable Long id) {
        return Result.success(pageSchemaService.unpublish(id));
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        pageSchemaService.delete(id);
        return Result.success();
    }

    /**
     * 下载页面为 Vue 文件
     */
    @GetMapping("/{id}/download")
    public void downloadVueFile(@PathVariable Long id, HttpServletResponse response) throws IOException {
        PageSchema page = pageSchemaService.getById(id);
        if (page == null) {
            response.setStatus(404);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"code\":0,\"msg\":\"页面不存在\"}");
            return;
        }

        String vueContent = vueFileGeneratorService.generate(page.getName(), page.getLayoutJson());
        String fileName = page.getPageCode() + ".vue";

        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + URLEncoder.encode(fileName, StandardCharsets.UTF_8) + "\"");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(vueContent);
        response.getWriter().flush();
    }
}
