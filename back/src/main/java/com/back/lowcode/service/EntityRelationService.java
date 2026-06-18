package com.back.lowcode.service;

import com.back.lowcode.entity.EntityRelation;
import com.back.lowcode.repository.EntityRelationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EntityRelationService {

    private final EntityRelationRepository entityRelationRepository;

    public List<EntityRelation> getAllRelations() {
        return entityRelationRepository.findAll();
    }

    public List<EntityRelation> getEnabledRelations() {
        return entityRelationRepository.findByEnabledTrue();
    }

    public Optional<EntityRelation> getRelationById(Long id) {
        return entityRelationRepository.findById(id);
    }

    public List<EntityRelation> getRelationsBySourceEntity(String sourceEntityCode) {
        return entityRelationRepository.findBySourceEntityCode(sourceEntityCode);
    }

    public List<EntityRelation> getEnabledRelationsBySourceEntity(String sourceEntityCode) {
        return entityRelationRepository.findBySourceEntityCodeAndEnabledTrue(sourceEntityCode);
    }

    public List<EntityRelation> getRelationsByTargetEntity(String targetEntityCode) {
        return entityRelationRepository.findByTargetEntityCode(targetEntityCode);
    }

    public Optional<EntityRelation> getRelationByField(String sourceEntityCode, String sourceFieldCode) {
        return entityRelationRepository.findBySourceEntityCodeAndSourceFieldCode(sourceEntityCode, sourceFieldCode);
    }

    public List<EntityRelation> getRelationsBetween(String sourceEntityCode, String targetEntityCode) {
        return entityRelationRepository.findBySourceEntityCodeAndTargetEntityCode(sourceEntityCode, targetEntityCode);
    }

    public List<EntityRelation> getRelationsByEntity(String entityCode) {
        return entityRelationRepository.findRelationsByEntityCode(entityCode);
    }

    @Transactional
    public EntityRelation createRelation(EntityRelation relation) {
        validateRelation(relation);
        return entityRelationRepository.save(relation);
    }

    @Transactional
    public EntityRelation updateRelation(Long id, EntityRelation updated) {
        EntityRelation existing = entityRelationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("关系不存在: " + id));

        existing.setSourceEntityCode(updated.getSourceEntityCode());
        existing.setSourceFieldCode(updated.getSourceFieldCode());
        existing.setTargetEntityCode(updated.getTargetEntityCode());
        existing.setTargetDisplayFieldCode(updated.getTargetDisplayFieldCode());
        existing.setRelationType(updated.getRelationType());
        existing.setDescription(updated.getDescription());
        existing.setCascadeDelete(updated.getCascadeDelete());
        existing.setEnabled(updated.getEnabled());

        return entityRelationRepository.save(existing);
    }

    @Transactional
    public void deleteRelation(Long id) {
        if (!entityRelationRepository.existsById(id)) {
            throw new IllegalArgumentException("关系不存在: " + id);
        }
        entityRelationRepository.deleteById(id);
    }

    @Transactional
    public EntityRelation toggleRelation(Long id, boolean enabled) {
        EntityRelation relation = entityRelationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("关系不存在: " + id));
        relation.setEnabled(enabled);
        return entityRelationRepository.save(relation);
    }

    @Transactional
    public void deleteRelationsByEntity(String entityCode) {
        entityRelationRepository.deleteBySourceEntityCode(entityCode);
        entityRelationRepository.deleteByTargetEntityCode(entityCode);
    }

    private void validateRelation(EntityRelation relation) {
        if (relation.getSourceEntityCode() == null || relation.getSourceEntityCode().isEmpty()) {
            throw new IllegalArgumentException("主实体编码不能为空");
        }
        if (relation.getSourceFieldCode() == null || relation.getSourceFieldCode().isEmpty()) {
            throw new IllegalArgumentException("主实体字段编码不能为空");
        }
        if (relation.getTargetEntityCode() == null || relation.getTargetEntityCode().isEmpty()) {
            throw new IllegalArgumentException("目标实体编码不能为空");
        }
        if (relation.getRelationType() == null || relation.getRelationType().isEmpty()) {
            throw new IllegalArgumentException("关系类型不能为空");
        }

        String type = relation.getRelationType();
        if (!List.of("ONE_TO_ONE", "ONE_TO_MANY", "MANY_TO_ONE", "MANY_TO_MANY").contains(type)) {
            throw new IllegalArgumentException("无效的关系类型: " + type);
        }

        if (relation.getSourceEntityCode().equals(relation.getTargetEntityCode())) {
            throw new IllegalArgumentException("实体不能与自身建立关系");
        }
    }
}