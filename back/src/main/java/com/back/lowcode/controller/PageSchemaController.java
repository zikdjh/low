package com.back.lowcode.controller;

import com.back.common.Result;
import com.back.lowcode.dto.PageSchemaDTO;
import com.back.lowcode.entity.PageSchema;
import com.back.lowcode.service.PageSchemaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lowcode/page-schema")
@RequiredArgsConstructor
public class PageSchemaController {

    private final PageSchemaService pageSchemaService;

    @GetMapping("/list")
    public Result list() {
        List<PageSchema> pages = pageSchemaService.listAll();
        return Result.success(pages);
    }

    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        PageSchema page = pageSchemaService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("页面不存在: " + id));
        return Result.success(page);
    }

    @GetMapping("/code/{code}")
    public Result getByCode(@PathVariable String code) {
        PageSchema page = pageSchemaService.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("页面不存在: " + code));
        return Result.success(page);
    }

    @GetMapping("/published/{code}")
    public Result getPublishedByCode(@PathVariable String code) {
        PageSchema page = pageSchemaService.findPublishedByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("已发布页面不存在: " + code));
        return Result.success(page);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Result createPage(@RequestBody PageSchemaDTO dto) {
        PageSchema created = pageSchemaService.create(dto);
        return Result.success(created);
    }

    @PutMapping("/{id}")
    public Result updatePage(@PathVariable Long id, @RequestBody PageSchemaDTO dto) {
        PageSchema updated = pageSchemaService.update(id, dto);
        return Result.success(updated);
    }

    @DeleteMapping("/{id}")
    public Result deletePage(@PathVariable Long id) {
        pageSchemaService.delete(id);
        return Result.success();
    }
}
