package ${basePackage}.controller;

import ${basePackage}.entity.${entity.className};
import ${basePackage}.service.${entity.className}Service;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ${entity.name!entity.code} REST 接口 —— 自动生成
 */
@RestController
@RequestMapping("/${appCode}/${entity.code}")
@RequiredArgsConstructor
public class ${entity.className}Controller {

    private final ${entity.className}Service service;

    @PostMapping
    public ${entity.className} create(@RequestBody ${entity.className} body) {
        return service.save(body);
    }

    @PutMapping("/{id}")
    public ${entity.className} update(@PathVariable ${pkType} id, @RequestBody ${entity.className} body) {
<#if pkType == "Long" || pkType == "Integer">
        body.set${entity.primaryKeyField.pascalName}(id);
<#else>
        // 主键非数值类型，update 入参 id 仅做路径校验
</#if>
        return service.save(body);
    }

    @GetMapping("/{id}")
    public ${entity.className} get(@PathVariable ${pkType} id) {
        return service.findById(id).orElse(null);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable ${pkType} id) {
        service.deleteById(id);
    }

    @GetMapping
    public List<${entity.className}> list() {
        return service.findAll();
    }

    @GetMapping("/page")
    public Page<${entity.className}> page(@RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "10") int size) {
        return service.findPage(PageRequest.of(page, size));
    }
}
