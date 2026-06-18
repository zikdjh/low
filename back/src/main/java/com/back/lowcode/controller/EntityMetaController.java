package com.back.lowcode.controller;

import com.back.common.Result;
import com.back.lowcode.dto.EntityListRequest;
import com.back.lowcode.entity.EntityMeta;
import com.back.lowcode.entity.FieldMeta;
import com.back.lowcode.service.EntityMetaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 实体元数据管理 Controller
 */
@RestController
@RequestMapping("/lowcode/entity")
@RequiredArgsConstructor
public class EntityMetaController {

    private final EntityMetaService entityMetaService;

    // ---- 实体 CRUD ----

    /** 分页查询实体列表 */
    @GetMapping("/list")
    public Result listEntities(@Valid EntityListRequest request) {
        Page<EntityMeta> page = entityMetaService.listEntities(request);
        return Result.success(page);
    }

    /** 根据 ID 获取实体详情（含字段） */
    @GetMapping("/{id}")
    public Result getEntity(@PathVariable Long id) {
        EntityMeta entity = entityMetaService.getEntityById(id)
                .orElseThrow(() -> new IllegalArgumentException("实体不存在: " + id));

        List<FieldMeta> fields = entityMetaService.getFieldsByEntityId(id);

        var result = new java.util.HashMap<String, Object>();
        result.put("entity", entity);
        result.put("fields", fields);
        return Result.success(result);
    }

    /** 根据 Code 获取实体详情（含字段） */
    @GetMapping("/code/{code}")
    public Result getEntityByCode(@PathVariable String code) {
        EntityMeta entity = entityMetaService.getEntityByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("实体不存在: " + code));

        List<FieldMeta> fields = entityMetaService.getFieldsByEntityId(entity.getId());

        var result = new java.util.HashMap<String, Object>();
        result.put("entity", entity);
        result.put("fields", fields);
        return Result.success(result);
    }

    /** 创建实体 */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Result createEntity(@RequestBody EntityMeta entity) {
        EntityMeta created = entityMetaService.createEntity(entity);
        return Result.success(created);
    }

    /** 更新实体元数据 */
    @PutMapping("/{id}")
    public Result updateEntity(@PathVariable Long id, @RequestBody EntityMeta entity) {
        EntityMeta updated = entityMetaService.updateEntity(id, entity);
        return Result.success(updated);
    }

    /** 删除实体（仅草稿状态可删） */
    @DeleteMapping("/{id}")
    public Result deleteEntity(@PathVariable Long id) {
        entityMetaService.deleteEntity(id);
        return Result.success();
    }

    // ---- 发布 / 归档 ----

    /** 发布实体（创建物理表） */
    @PostMapping("/{id}/publish")
    public Result publishEntity(@PathVariable Long id) {
        EntityMeta published = entityMetaService.publishEntity(id);
        return Result.success(published);
    }

    /** 归档实体 */
    @PostMapping("/{id}/archive")
    public Result archiveEntity(@PathVariable Long id) {
        EntityMeta archived = entityMetaService.archiveEntity(id);
        return Result.success(archived);
    }

    // ---- 字段管理 ----

    /** 获取实体字段列表 */
    @GetMapping("/{entityId}/fields")
    public Result getFields(@PathVariable Long entityId) {
        List<FieldMeta> fields = entityMetaService.getFieldsByEntityId(entityId);
        return Result.success(fields);
    }

    /** 更新实体字段（全量替换） */
    @PutMapping("/{entityId}/fields")
    public Result updateFields(@PathVariable Long entityId, @RequestBody List<FieldMeta> fields) {
        List<FieldMeta> saved = entityMetaService.updateFields(entityId, fields);
        return Result.success(saved);
    }
}
