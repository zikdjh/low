package ${pkgBase}.repository;

import ${pkgBase}.entity.${entity.className};
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * ${entity.description!entity.name} Repository
 * 由低代码代码生成器生成 @ ${now}
 */
@Repository
public interface ${entity.className}Repository extends JpaRepository<${entity.className}, Long>, JpaSpecificationExecutor<${entity.className}> {
}
