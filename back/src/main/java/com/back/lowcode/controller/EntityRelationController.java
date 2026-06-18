package com.back.lowcode.controller;

import com.back.common.Result;
import com.back.lowcode.entity.EntityRelation;
import com.back.lowcode.service.EntityRelationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lowcode/relation")
@RequiredArgsConstructor
public class EntityRelationController {

    private final EntityRelationService entityRelationService;

    @GetMapping
    public Result listAll() {
        List<EntityRelation> relations = entityRelationService.getAllRelations();
        return Result.success(relations);
    }

    @GetMapping("/enabled")
    public Result listEnabled() {
        List<EntityRelation> relations = entityRelationService.getEnabledRelations();
        return Result.success(relations);
    }

    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        EntityRelation relation = entityRelationService.getRelationById(id)
                .orElseThrow(() -> new IllegalArgumentException("关系不存在: " + id));
        return Result.success(relation);
    }

    @GetMapping("/source/{entityCode}")
    public Result getBySourceEntity(@PathVariable String entityCode) {
        List<EntityRelation> relations = entityRelationService.getRelationsBySourceEntity(entityCode);
        return Result.success(relations);
    }

    @GetMapping("/target/{entityCode}")
    public Result getByTargetEntity(@PathVariable String entityCode) {
        List<EntityRelation> relations = entityRelationService.getRelationsByTargetEntity(entityCode);
        return Result.success(relations);
    }

    @GetMapping("/entity/{entityCode}")
    public Result getByEntity(@PathVariable String entityCode) {
        List<EntityRelation> relations = entityRelationService.getRelationsByEntity(entityCode);
        return Result.success(relations);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Result create(@RequestBody EntityRelation relation) {
        EntityRelation created = entityRelationService.createRelation(relation);
        return Result.success(created);
    }

    @PutMapping("/{id}")
    public Result update(@PathVariable Long id, @RequestBody EntityRelation relation) {
        EntityRelation updated = entityRelationService.updateRelation(id, relation);
        return Result.success(updated);
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        entityRelationService.deleteRelation(id);
        return Result.success();
    }

    @PostMapping("/{id}/enable")
    public Result enable(@PathVariable Long id) {
        EntityRelation updated = entityRelationService.toggleRelation(id, true);
        return Result.success(updated);
    }

    @PostMapping("/{id}/disable")
    public Result disable(@PathVariable Long id) {
        EntityRelation updated = entityRelationService.toggleRelation(id, false);
        return Result.success(updated);
    }
}