package com.back.lowcode.controller;

import com.back.common.Result;
import com.back.lowcode.entity.ComponentDef;
import com.back.lowcode.service.ComponentDefService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/lowcode/component")
public class ComponentDefController {

    @Autowired
    private ComponentDefService componentDefService;

    @GetMapping
    public Result<List<ComponentDef>> getAllComponents() {
        return Result.success(componentDefService.getAllActiveComponents());
    }

    @GetMapping("/grouped")
    public Result<Map<String, List<ComponentDef>>> getComponentsGroupedByCategory() {
        return Result.success(componentDefService.getComponentsGroupedByCategory());
    }

    @GetMapping("/category/{category}")
    public Result<List<ComponentDef>> getComponentsByCategory(@PathVariable String category) {
        return Result.success(componentDefService.getComponentsByCategory(category));
    }

    @GetMapping("/key/{compKey}")
    public Result<ComponentDef> getByCompKey(@PathVariable String compKey) {
        return componentDefService.getByCompKey(compKey)
                .map(Result::success)
                .orElse(Result.error("组件不存在"));
    }

    @GetMapping("/{id}")
    public Result<ComponentDef> getById(@PathVariable Long id) {
        ComponentDef component = componentDefService.getById(id);
        if (component == null) {
            return Result.error("组件不存在");
        }
        return Result.success(component);
    }

    @PostMapping
    public Result<ComponentDef> create(@RequestBody ComponentDef componentDef) {
        return Result.success(componentDefService.create(componentDef));
    }

    @PutMapping("/{id}")
    public Result<ComponentDef> update(@PathVariable Long id, @RequestBody ComponentDef componentDef) {
        return Result.success(componentDefService.update(id, componentDef));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        componentDefService.delete(id);
        return Result.success();
    }

    @PostMapping("/batch")
    public Result<Void> batchSave(@RequestBody List<ComponentDef> components) {
        componentDefService.batchSave(components);
        return Result.success();
    }
}