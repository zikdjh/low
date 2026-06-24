package ${basePackage}.service;

import ${basePackage}.entity.${entity.className};
import ${basePackage}.repository.${entity.className}Repository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * ${entity.name!entity.code} 业务层 —— 自动生成
 * <p>
 * 包含基础 CRUD + 分页；自定义校验逻辑建议在外层 controller 或独立 validator 中补充。
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ${entity.className}Service {

    private final ${entity.className}Repository repository;

    public ${entity.className} save(${entity.className} entity) {
        return repository.save(entity);
    }

    @Transactional(readOnly = true)
    public Optional<${entity.className}> findById(${pkType} id) {
        return repository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<${entity.className}> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<${entity.className}> findPage(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public void deleteById(${pkType} id) {
        repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public long count() {
        return repository.count();
    }
}
