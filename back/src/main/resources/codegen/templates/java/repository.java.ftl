package ${basePackage}.repository;

import ${basePackage}.entity.${entity.className};
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * ${entity.name!entity.code} 数据访问层 —— 自动生成
 */
@Repository
public interface ${entity.className}Repository extends JpaRepository<${entity.className}, ${pkType}> {
}
