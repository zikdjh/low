package com.back.lowcode.repository;

import com.back.lowcode.entity.EntityRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 实体关系数据访问层
 */
@Repository
public interface EntityRelationRepository extends JpaRepository<EntityRelation, Long> {

    /**
     * 根据主实体编码查询关系列表
     */
    List<EntityRelation> findBySourceEntityCode(String sourceEntityCode);

    /**
     * 根据目标实体编码查询关系列表（反向查询）
     */
    List<EntityRelation> findByTargetEntityCode(String targetEntityCode);

    /**
     * 根据主实体编码和字段编码查询关系
     */
    Optional<EntityRelation> findBySourceEntityCodeAndSourceFieldCode(String sourceEntityCode, String sourceFieldCode);

    /**
     * 查询两个实体之间的关系
     */
    List<EntityRelation> findBySourceEntityCodeAndTargetEntityCode(String sourceEntityCode, String targetEntityCode);

    /**
     * 根据关系类型查询
     */
    List<EntityRelation> findByRelationType(String relationType);

    /**
     * 查询启用的关系
     */
    List<EntityRelation> findByEnabledTrue();

    /**
     * 根据主实体编码查询启用的关系
     */
    List<EntityRelation> findBySourceEntityCodeAndEnabledTrue(String sourceEntityCode);

    /**
     * 查询与指定实体相关的所有关系（作为主实体或目标实体）
     */
    @Query("SELECT r FROM EntityRelation r WHERE r.sourceEntityCode = :entityCode OR r.targetEntityCode = :entityCode")
    List<EntityRelation> findRelationsByEntityCode(@Param("entityCode") String entityCode);

    /**
     * 删除指定实体的所有关系
     */
    void deleteBySourceEntityCode(String sourceEntityCode);

    /**
     * 删除指定目标实体的所有关系
     */
    void deleteByTargetEntityCode(String targetEntityCode);
}