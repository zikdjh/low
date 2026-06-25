package com.back.generated.controller;

import com.back.generated.dto.LeaveApplicationDTO;
import com.back.generated.entity.LeaveApplication;
import com.back.generated.service.LeaveApplicationService;
import com.back.common.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 学生请假申请记录，关联学生实体，通过工作流完成审批流程 Controller
 * 由低代码代码生成器生成 @ 2026-06-25T20:49:37.0509793
 */
@RestController
@RequestMapping("/lowcode/gen/leave_application")
@RequiredArgsConstructor
public class LeaveApplicationController {

    private final LeaveApplicationService service;

    @GetMapping("/list")
    public Result list(LeaveApplicationDTO query,
                       @RequestParam(defaultValue = "1") int pageNum,
                       @RequestParam(defaultValue = "10") int pageSize) {
        Page<LeaveApplication> page = service.page(query, pageNum, pageSize);
        return Result.success(Map.of(
                "list", page.getContent(),
                "total", page.getTotalElements(),
                "pageNum", pageNum,
                "pageSize", pageSize
        ));
    }

    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        LeaveApplication entity = service.getById(id);
        if (entity == null) {
            return Result.error("请假申请不存在: " + id);
        }
        return Result.success(entity);
    }

    @PostMapping
    public Result create(@Valid @RequestBody LeaveApplicationDTO dto) {
        return Result.success(service.create(dto));
    }

    @PutMapping("/{id}")
    public Result update(@PathVariable Long id, @Valid @RequestBody LeaveApplicationDTO dto) {
        return Result.success(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        service.delete(id);
        return Result.success();
    }

    @DeleteMapping("/batch")
    public Result deleteBatch(@RequestBody List<Long> ids) {
        service.deleteBatch(ids);
        return Result.success();
    }
}
