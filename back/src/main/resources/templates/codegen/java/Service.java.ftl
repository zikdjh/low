package ${pkgBase}.service;

import ${pkgBase}.dto.${entity.className}DTO;
import ${pkgBase}.entity.${entity.className};
import ${pkgBase}.repository.${entity.className}Repository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * ${entity.description!entity.name} Service
 * 由低代码代码生成器生成 @ ${now}
 */
@Service
@RequiredArgsConstructor
public class ${entity.className}Service {

    private final ${entity.className}Repository repository;

    public Page<${entity.className}> page(${entity.className}DTO query, int pageNum, int pageSize) {
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        PageRequest pageable = PageRequest.of(Math.max(pageNum - 1, 0), pageSize, sort);
        Specification<${entity.className}> spec = buildSpec(query);
        return repository.findAll(spec, pageable);
    }

    public ${entity.className} getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Transactional
    public ${entity.className} create(${entity.className}DTO dto) {
        ${entity.className} entity = new ${entity.className}();
        BeanUtils.copyProperties(dto, entity);
        return repository.save(entity);
    }

    @Transactional
    public ${entity.className} update(Long id, ${entity.className}DTO dto) {
        ${entity.className} entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("${entity.name}不存在: " + id));
        BeanUtils.copyProperties(dto, entity, "id");
        return repository.save(entity);
    }

    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Transactional
    public void deleteBatch(List<Long> ids) {
        repository.deleteAllById(ids);
    }

    private Specification<${entity.className}> buildSpec(${entity.className}DTO query) {
        return (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (query == null) return cb.conjunction();
<#list searchFields as f>
    <#if f.javaType == "String">
            if (query.get${f.pascalName}() != null && !query.get${f.pascalName}().isEmpty()) {
                predicates.add(cb.like(root.get("${f.camelName}"), "%" + query.get${f.pascalName}() + "%"));
            }
    <#else>
            if (query.get${f.pascalName}() != null) {
                predicates.add(cb.equal(root.get("${f.camelName}"), query.get${f.pascalName}()));
            }
    </#if>
</#list>
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
