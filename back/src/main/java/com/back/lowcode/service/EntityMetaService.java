package com.back.lowcode.service;

import com.back.lowcode.config.LowCodeConstants;
import com.back.lowcode.config.MySQLReservedWords;
import com.back.lowcode.dto.EntityListRequest;
import com.back.lowcode.entity.EntityMeta;
import com.back.lowcode.entity.FieldMeta;
import com.back.lowcode.repository.EntityMetaRepository;
import com.back.lowcode.repository.FieldMetaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 实体元数据管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EntityMetaService {

    private final EntityMetaRepository entityMetaRepository;
    private final FieldMetaRepository fieldMetaRepository;
    private final DDLService ddlService;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    private static final Pattern CODE_PATTERN = Pattern.compile(LowCodeConstants.ENTITY_CODE_PATTERN);

    // ---- 实体 CRUD ----

    public Page<EntityMeta> listEntities(EntityListRequest request) {
        Specification<EntityMeta> spec = buildSpecification(request.getKeyword(), request.getStatus());

        Sort sort = buildSort(request.getSortBy(), request.getSortOrder());
        PageRequest pageRequest = PageRequest.of(request.getPage() - 1, request.getPageSize(), sort);

        Page<EntityMeta> page = entityMetaRepository.findAll(spec, pageRequest);

        // 批量填充 fieldCount
        if (page.hasContent()) {
            List<Long> entityIds = page.getContent().stream()
                    .map(EntityMeta::getId)
                    .collect(Collectors.toList());
            Map<Long, Long> countMap = fieldMetaRepository.countByEntityIdIn(entityIds)
                    .stream()
                    .collect(Collectors.toMap(
                            row -> (Long) row[0],
                            row -> (Long) row[1]
                    ));
            page.getContent().forEach(e ->
                    e.setFieldCount(countMap.getOrDefault(e.getId(), 0L)));
        }

        return page;
    }

    /**
     * 构建动态查询条件
     */
    private Specification<EntityMeta> buildSpecification(String keyword, String status) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(keyword)) {
                String pattern = "%" + keyword + "%";
                predicates.add(cb.or(
                        cb.like(root.get("name"), pattern),
                        cb.like(root.get("code"), pattern)
                ));
            }

            if (StringUtils.hasText(status)) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            Predicate[] arr = new Predicate[predicates.size()];
            return cb.and(predicates.toArray(arr));
        };
    }

    /**
     * 构建排序
     */
    private Sort buildSort(String sortBy, String sortOrder) {
        if (!StringUtils.hasText(sortBy)) {
            return Sort.by(Sort.Direction.DESC, "updatedAt");
        }

        Sort.Direction direction = "asc".equalsIgnoreCase(sortOrder)
                ? Sort.Direction.ASC : Sort.Direction.DESC;

        // 允许排序的字段白名单
        Set<String> allowedFields = Set.of("name", "code", "status", "updatedAt");
        if (!allowedFields.contains(sortBy)) {
            return Sort.by(Sort.Direction.DESC, "updatedAt");
        }

        return Sort.by(direction, sortBy);
    }

    public Optional<EntityMeta> getEntityById(Long id) {
        return entityMetaRepository.findById(id);
    }

    public Optional<EntityMeta> getEntityByCode(String code) {
        return entityMetaRepository.findByCode(code);
    }

    @Transactional
    public EntityMeta createEntity(EntityMeta entity) {
        validateEntityCode(entity.getCode());

        if (entityMetaRepository.existsByCode(entity.getCode())) {
            throw new IllegalArgumentException("实体编码已存在: " + entity.getCode());
        }

        // 自动生成 tableName
        if (!StringUtils.hasText(entity.getTableName())) {
            entity.setTableName(LowCodeConstants.TABLE_PREFIX + entity.getCode());
        }

        return entityMetaRepository.save(entity);
    }

    @Transactional
    public EntityMeta updateEntity(Long id, EntityMeta updated) {
        EntityMeta existing = entityMetaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("实体不存在: " + id));

        if (!"draft".equals(existing.getStatus())) {
            throw new IllegalStateException("仅草稿状态的实体可编辑");
        }

        if (!existing.getCode().equals(updated.getCode()) && entityMetaRepository.existsByCode(updated.getCode())) {
            throw new IllegalArgumentException("实体编码已存在: " + updated.getCode());
        }

        existing.setName(updated.getName());
        existing.setCode(updated.getCode());
        existing.setDescription(updated.getDescription());

        return entityMetaRepository.save(existing);
    }

    @Transactional
    public void deleteEntity(Long id) {
        EntityMeta entity = entityMetaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("实体不存在: " + id));

        if ("published".equals(entity.getStatus())) {
            throw new IllegalStateException("已发布的实体不可删除，请先归档");
        }

        fieldMetaRepository.deleteByEntityId(id);
        entityMetaRepository.delete(entity);
    }

    // ---- 发布 ----

    @Transactional
    public EntityMeta publishEntity(Long id) {
        EntityMeta entity = entityMetaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("实体不存在: " + id));

        List<FieldMeta> fields = fieldMetaRepository.findByEntityIdOrderBySortOrderAsc(id);
        if (fields.isEmpty()) {
            throw new IllegalStateException("请至少添加一个字段");
        }

        // 校验 REFERENCE 类型字段的关联实体
        validateReferenceFields(fields);

        ddlService.generateCreateTable(entity, fields);

        entity.setStatus("published");
        entity = entityMetaRepository.save(entity);

        // 缓存字段元数据到 Redis
        cacheFieldMeta(entity.getCode(), fields);

        return entity;
    }

    /**
     * 校验 REFERENCE 类型字段的关联实体是否存在且已发布
     */
    private void validateReferenceFields(List<FieldMeta> fields) {
        for (FieldMeta field : fields) {
            if ("REFERENCE".equals(field.getFieldType())) {
                if (field.getReferenceEntityCode() == null || field.getReferenceEntityCode().isEmpty()) {
                    throw new IllegalArgumentException(
                            "REFERENCE 类型字段 '" + field.getName() + "' 必须指定关联实体编码");
                }
                
                EntityMeta refEntity = entityMetaRepository.findByCode(field.getReferenceEntityCode())
                        .orElse(null);
                
                if (refEntity == null) {
                    throw new IllegalArgumentException(
                            "REFERENCE 字段 '" + field.getName() + "' 关联的实体 '" + 
                            field.getReferenceEntityCode() + "' 不存在");
                }
                
                if (!"published".equals(refEntity.getStatus())) {
                    throw new IllegalArgumentException(
                            "REFERENCE 字段 '" + field.getName() + "' 关联的实体 '" + 
                            field.getReferenceEntityCode() + "' 尚未发布，请先发布该实体");
                }
            }
        }
    }

    @Transactional
    public EntityMeta archiveEntity(Long id) {
        EntityMeta entity = entityMetaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("实体不存在: " + id));

        if ("draft".equals(entity.getStatus())) {
            throw new IllegalStateException("草稿状态无需归档，直接删除即可");
        }

        entity.setStatus("archived");
        return entityMetaRepository.save(entity);
    }

    // ---- 字段管理 ----

    public List<FieldMeta> getFieldsByEntityId(Long entityId) {
        return fieldMetaRepository.findByEntityIdOrderBySortOrderAsc(entityId);
    }

    @Transactional
    public List<FieldMeta> updateFields(Long entityId, List<FieldMeta> newFields) {
        EntityMeta entity = entityMetaRepository.findById(entityId)
                .orElseThrow(() -> new IllegalArgumentException("实体不存在: " + entityId));

        // 校验字段编码和 REFERENCE 字段引用
        for (FieldMeta f : newFields) {
            if (f.getCode() == null || !f.getCode().matches("^[a-z][a-zA-Z0-9_]*$")) {
                throw new IllegalArgumentException("非法字段编码: " + f.getCode());
            }
            if (MySQLReservedWords.isReserved(f.getCode())) {
                throw new IllegalArgumentException(
                        "字段编码不能使用 MySQL 保留字: '" + f.getCode() + "'，请换一个名称");
            }

            if ("REFERENCE".equals(f.getFieldType())) {
                if (f.getReferenceEntityCode() == null || f.getReferenceEntityCode().isEmpty()) {
                    throw new IllegalArgumentException("引用类型字段 '" + f.getCode() + "' 必须指定引用实体编码");
                }
                EntityMeta refEntity = entityMetaRepository.findByCode(f.getReferenceEntityCode())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "引用实体 '" + f.getReferenceEntityCode() + "' 不存在"));
                if (!"published".equals(refEntity.getStatus())) {
                    throw new IllegalArgumentException(
                            "引用实体 '" + f.getReferenceEntityCode() + "' 必须是已发布状态");
                }
            }
        }

        List<FieldMeta> oldFields = fieldMetaRepository.findByEntityIdOrderBySortOrderAsc(entityId);

        // 如果实体已发布，执行 ALTER TABLE
        if ("published".equals(entity.getStatus())) {
            ddlService.generateAlterTable(entity, oldFields, newFields);
        }

        // 删除旧字段，保存新字段
        fieldMetaRepository.deleteByEntityId(entityId);

        // 设置 entityId 和 columnName
        for (int i = 0; i < newFields.size(); i++) {
            FieldMeta f = newFields.get(i);
            f.setId(null); // 新记录
            f.setEntityId(entityId);
            if (f.getColumnName() == null || f.getColumnName().isEmpty()) {
                f.setColumnName(f.getCode());
            }
            if (f.getSortOrder() == null) {
                f.setSortOrder(i);
            }
        }

        List<FieldMeta> saved = fieldMetaRepository.saveAll(newFields);

        // 更新 Redis 缓存
        if ("published".equals(entity.getStatus())) {
            cacheFieldMeta(entity.getCode(), saved);
        }

        return saved;
    }

    // ---- Redis 缓存 ----

    /**
     * 将字段元数据缓存到 Redis
     */
    private void cacheFieldMeta(String entityCode, List<FieldMeta> fields) {
        String redisKey = LowCodeConstants.REDIS_META_KEY_PREFIX + entityCode + ":fields";
        try {
            String json = objectMapper.writeValueAsString(fields);
            stringRedisTemplate.opsForValue().set(redisKey, json, 30, TimeUnit.DAYS);
        } catch (Exception e) {
            log.warn("Failed to cache field metadata for entity {}: {}", entityCode, e.getMessage());
        }
    }

    /**
     * 从 Redis 获取缓存的字段元数据
     */
    public List<FieldMeta> getCachedFields(String entityCode) {
        String redisKey = LowCodeConstants.REDIS_META_KEY_PREFIX + entityCode + ":fields";
        String json = stringRedisTemplate.opsForValue().get(redisKey);
        if (json == null) {
            // 缓存未命中，从数据库加载
            EntityMeta entity = entityMetaRepository.findByCode(entityCode)
                    .orElse(null);
            if (entity == null) return List.of();

            List<FieldMeta> fields = fieldMetaRepository.findByEntityIdOrderBySortOrderAsc(entity.getId());
            cacheFieldMeta(entityCode, fields);
            return fields;
        }
        try {
            return objectMapper.readValue(json,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, FieldMeta.class));
        } catch (Exception e) {
            log.warn("Failed to deserialize cached fields for {}: {}", entityCode, e.getMessage());
            return List.of();
        }
    }

    /**
     * 清除缓存
     */
    public void invalidateCache(String entityCode) {
        String redisKey = LowCodeConstants.REDIS_META_KEY_PREFIX + entityCode + ":fields";
        stringRedisTemplate.delete(redisKey);
    }

    // ---- 工具方法 ----

    public List<EntityMeta> searchByKeyword(String keyword) {
        return entityMetaRepository.search(keyword);
    }

    private void validateEntityCode(String code) {
        if (code == null || !CODE_PATTERN.matcher(code).matches()) {
            throw new IllegalArgumentException(
                    "实体编码格式非法: '" + code + "'，需匹配 " + LowCodeConstants.ENTITY_CODE_PATTERN);
        }
        if (MySQLReservedWords.isReserved(code)) {
            throw new IllegalArgumentException(
                    "实体编码不能使用 MySQL 保留字: '" + code + "'，请换一个名称");
        }
    }
}
