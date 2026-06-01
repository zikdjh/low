package com.back.lowcode.controller;

import com.back.common.Result;
import com.back.lowcode.dto.DynamicDataRequest;
import com.back.lowcode.service.DynamicDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 动态数据 Controller - 处理已发布实体的 CRUD 操作
 */
@RestController
@RequestMapping("/lowcode/data/{entityCode}")
@RequiredArgsConstructor
public class DynamicDataController {

    private final DynamicDataService dynamicDataService;

    /** 分页查询数据列表 */
    @GetMapping
    public Result listData(
            @PathVariable String entityCode,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) Map<String, String> filters) {
        Page<Map<String, Object>> result = dynamicDataService.listData(entityCode, page, pageSize, filters);
        return Result.success(result);
    }

    /** 根据 ID 获取数据详情 */
    @GetMapping("/{id}")
    public Result getData(@PathVariable String entityCode, @PathVariable Object id) {
        Map<String, Object> data = dynamicDataService.getDataById(entityCode, id);
        return Result.success(data);
    }

    /** 创建数据 */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Result createData(@PathVariable String entityCode, @RequestBody Map<String, Object> data) {
        Map<String, Object> created = dynamicDataService.createData(entityCode, data);
        return Result.success(created);
    }

    /** 更新数据 */
    @PutMapping("/{id}")
    public Result updateData(
            @PathVariable String entityCode,
            @PathVariable Object id,
            @RequestBody Map<String, Object> data) {
        Map<String, Object> updated = dynamicDataService.updateData(entityCode, id, data);
        return Result.success(updated);
    }

    /** 删除数据 */
    @DeleteMapping("/{id}")
    public Result deleteData(@PathVariable String entityCode, @PathVariable Object id) {
        dynamicDataService.deleteData(entityCode, id);
        return Result.success();
    }

    /** 批量删除 */
    @PostMapping("/batch-delete")
    public Result batchDelete(@PathVariable String entityCode, @RequestBody DynamicDataRequest request) {
        dynamicDataService.batchDelete(entityCode, request.getIds());
        return Result.success();
    }
}
