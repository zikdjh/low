package ${pkgBase}.controller;

import ${pkgBase}.dto.${entity.className}DTO;
import ${pkgBase}.entity.${entity.className};
import ${pkgBase}.service.${entity.className}Service;
import com.back.common.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * ${entity.description!entity.name} Controller
 * 由低代码代码生成器生成 @ ${now}
 */
@RestController
@RequestMapping("/${entity.classNameLower}")
@RequiredArgsConstructor
public class ${entity.className}Controller {

    private final ${entity.className}Service service;

    @GetMapping("/list")
    public Result list(${entity.className}DTO query,
                       @RequestParam(defaultValue = "1") int pageNum,
                       @RequestParam(defaultValue = "10") int pageSize) {
        Page<${entity.className}> page = service.page(query, pageNum, pageSize);
        return Result.success(Map.of(
                "list", page.getContent(),
                "total", page.getTotalElements(),
                "pageNum", pageNum,
                "pageSize", pageSize
        ));
    }

    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        ${entity.className} entity = service.getById(id);
        if (entity == null) {
            return Result.error("${entity.name}不存在: " + id);
        }
        return Result.success(entity);
    }

    @PostMapping
    public Result create(@Valid @RequestBody ${entity.className}DTO dto) {
        return Result.success(service.create(dto));
    }

    @PutMapping("/{id}")
    public Result update(@PathVariable Long id, @Valid @RequestBody ${entity.className}DTO dto) {
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
