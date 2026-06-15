package com.back.lowcode.controller;

import com.back.common.Result;
import com.back.lowcode.entity.ComponentDef;
import com.back.lowcode.service.ComponentDefService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lowcode/component")
@RequiredArgsConstructor
public class ComponentDefController {

    private final ComponentDefService componentDefService;

    @GetMapping
    public Result getAllComponents() {
        return Result.success(componentDefService.getAllActiveComponents());
    }

    @GetMapping("/grouped")
    public Result getComponentsGroupedByCategory() {
        return Result.success(componentDefService.getComponentsGroupedByCategory());
    }

    @GetMapping("/category/{category}")
    public Result getComponentsByCategory(@PathVariable String category) {
        return Result.success(componentDefService.getComponentsByCategory(category));
    }

    @GetMapping("/key/{compKey}")
    public Result getByCompKey(@PathVariable String compKey) {
        return componentDefService.getByCompKey(compKey)
                .map(Result::success)
                .orElse(Result.error("组件不存在"));
    }

    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        ComponentDef component = componentDefService.getById(id);
        if (component == null) {
            return Result.error("组件不存在");
        }
        return Result.success(component);
    }

    @PostMapping
    public Result create(@RequestBody ComponentDef componentDef) {
        return Result.success(componentDefService.create(componentDef));
    }

    @PutMapping("/{id}")
    public Result update(@PathVariable Long id, @RequestBody ComponentDef componentDef) {
        return Result.success(componentDefService.update(id, componentDef));
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        componentDefService.delete(id);
        return Result.success();
    }

    @PostMapping("/batch")
    public Result batchSave(@RequestBody List<ComponentDef> components) {
        componentDefService.batchSave(components);
        return Result.success();
    }
}
