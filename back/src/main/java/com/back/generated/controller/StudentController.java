package com.back.generated.controller;

import com.back.generated.dto.StudentDTO;
import com.back.generated.entity.Student;
import com.back.generated.service.StudentService;
import com.back.common.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 学生基本信息，包含学号、班级、院系、联系方式等 Controller
 * 由低代码代码生成器生成 @ 2026-06-25T21:02:06.2970011
 */
@RestController
@RequestMapping("/lowcode/gen/student")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService service;

    @GetMapping("/list")
    public Result list(StudentDTO query,
                       @RequestParam(defaultValue = "1") int pageNum,
                       @RequestParam(defaultValue = "10") int pageSize) {
        Page<Student> page = service.page(query, pageNum, pageSize);
        return Result.success(Map.of(
                "list", page.getContent(),
                "total", page.getTotalElements(),
                "pageNum", pageNum,
                "pageSize", pageSize
        ));
    }

    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        Student entity = service.getById(id);
        if (entity == null) {
            return Result.error("学生信息不存在: " + id);
        }
        return Result.success(entity);
    }

    @PostMapping
    public Result create(@Valid @RequestBody StudentDTO dto) {
        return Result.success(service.create(dto));
    }

    @PutMapping("/{id}")
    public Result update(@PathVariable Long id, @Valid @RequestBody StudentDTO dto) {
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
