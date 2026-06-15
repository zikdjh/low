package com.back.lowcode.controller;

import com.back.common.Result;
import com.back.lowcode.entity.PageSchema;
import com.back.lowcode.service.PageSchemaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/lowcode/page")
@RequiredArgsConstructor
public class PageSchemaController {

    private final PageSchemaService pageSchemaService;

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
}
