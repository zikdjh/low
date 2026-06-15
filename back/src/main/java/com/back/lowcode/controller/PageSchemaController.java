package com.back.lowcode.controller;

import com.back.common.Result;
import com.back.lowcode.entity.PageSchema;
import com.back.lowcode.service.PageSchemaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lowcode/page")
public class PageSchemaController {

    @Autowired
    private PageSchemaService pageSchemaService;

    @GetMapping
    public Result<List<PageSchema>> getAllPages() {
        return Result.success(pageSchemaService.getAllPages());
    }

    @GetMapping("/status/{status}")
    public Result<List<PageSchema>> getPagesByStatus(@PathVariable String status) {
        return Result.success(pageSchemaService.getPagesByStatus(status));
    }

    @GetMapping("/code/{pageCode}")
    public Result<PageSchema> getByPageCode(@PathVariable String pageCode) {
        PageSchema page = pageSchemaService.getByPageCode(pageCode);
        if (page == null) {
            return Result.error("页面不存在");
        }
        return Result.success(page);
    }

    @GetMapping("/{id}")
    public Result<PageSchema> getById(@PathVariable Long id) {
        PageSchema page = pageSchemaService.getById(id);
        if (page == null) {
            return Result.error("页面不存在");
        }
        return Result.success(page);
    }

    @PostMapping
    public Result<PageSchema> create(@RequestBody PageSchema pageSchema) {
        return Result.success(pageSchemaService.create(pageSchema));
    }

    @PutMapping("/{id}")
    public Result<PageSchema> update(@PathVariable Long id, @RequestBody PageSchema pageSchema) {
        return Result.success(pageSchemaService.update(id, pageSchema));
    }

    @PostMapping("/{id}/publish")
    public Result<PageSchema> publish(@PathVariable Long id) {
        return Result.success(pageSchemaService.publish(id));
    }

    @PostMapping("/{id}/unpublish")
    public Result<PageSchema> unpublish(@PathVariable Long id) {
        return Result.success(pageSchemaService.unpublish(id));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        pageSchemaService.delete(id);
        return Result.success();
    }
}