package com.back.lowcode.service;

import com.back.lowcode.entity.DdlLog;
import com.back.lowcode.entity.EntityMeta;
import com.back.lowcode.entity.FieldMeta;
import com.back.lowcode.repository.DdlLogRepository;
import com.back.lowcode.repository.EntityMetaRepository;
import com.back.lowcode.repository.FieldMetaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 动态数据服务 - 处理已发布实体的 CRUD 操作
 */
@Service
@RequiredArgsConstructor
public class DynamicDataService {

    private final EntityMetaRepository entityMetaRepository;
    private final FieldMetaRepository fieldMetaRepository;
    private final DdlLogRepository ddlLogRepository;
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedJdbcTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 确保实体的数据表存在，如果不存在则尝试自动重建
     */
    private void ensureTableExists(EntityMeta entity, List<FieldMeta> fields) {
        String tableName = entity.getTableName();
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = ?",
                    Integer.class, tableName);
            if (count == null || count == 0) {
                tryRebuildTable(entity, fields);
            }
        } catch (DataAccessException e) {
            // information_schema 查询失败（如权限不足），回退方案：直接 SELECT 探测
            try {
                jdbcTemplate.queryForObject(
                        "SELECT 1 FROM `" + tableName + "` LIMIT 1", Integer.class);
            } catch (DataAccessException ex) {
                tryRebuildTable(entity, fields);
            }
        }
    }

    /**
     * 从 DDL 日志获取最近成功的建表语句并重放
     */
    private void tryRebuildTable(EntityMeta entity, List<FieldMeta> fields) {
        List<DdlLog> logs = ddlLogRepository
                .findByEntityIdAndOperationTypeAndResultOrderByExecutedAtDesc(
                        entity.getId(), "CREATE_TABLE", "SUCCESS");
        if (!logs.isEmpty()) {
            try {
                jdbcTemplate.execute(logs.get(0).getSqlStatement());
                return;
            } catch (DataAccessException ex) {
                // DDL 日志回放失败，尝试重新生成 SQL
            }
        }
        // 最后的兜底：重新生成建表 SQL
        try {
            String sql = buildCreateTableSqlFromFields(entity.getTableName(), fields);
            jdbcTemplate.execute(sql);
        } catch (DataAccessException ex) {
            throw new IllegalStateException(
                    "数据表 " + entity.getTableName() + " 不存在且自动重建失败，请联系管理员", ex);
        }
    }

    /**
     * 根据字段元数据生成 CREATE TABLE SQL（兜底方案）
     */
    private String buildCreateTableSqlFromFields(String tableName, List<FieldMeta> fields) {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE IF NOT EXISTS `").append(tableName).append("` (\n");
        sb.append("  `id` BIGINT NOT NULL AUTO_INCREMENT,\n");
        for (int i = 0; i < fields.size(); i++) {
            FieldMeta f = fields.get(i);
            sb.append("  `").append(f.getColumnName()).append("` ");
            String mysqlType = switch (f.getFieldType()) {
                case "VARCHAR" -> "VARCHAR(" + (f.getLength() != null && f.getLength() > 0 ? f.getLength() : 255) + ")";
                case "INTEGER" -> "INT";
                case "LONG" -> "BIGINT";
                case "DOUBLE" -> "DOUBLE";
                case "BOOLEAN" -> "TINYINT(1)";
                case "DATE" -> "DATE";
                case "DATETIME" -> "DATETIME";
                case "TEXT" -> "TEXT";
                case "JSON" -> "JSON";
                case "DECIMAL" -> "DECIMAL(" + (f.getLength() != null ? f.getLength() : 10) + "," + (f.getPrecision() != null ? f.getPrecision() : 2) + ")";
                default -> "VARCHAR(255)";
            };
            sb.append(mysqlType);
            if (f.getNullable() != null && !f.getNullable()) {
                sb.append(" NOT NULL");
            }
            if (f.getDefaultValue() != null && !f.getDefaultValue().isEmpty()) {
                sb.append(" DEFAULT '").append(f.getDefaultValue()).append("'");
            }
            sb.append(",\n");
        }
        sb.append("  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,\n");
        sb.append("  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,\n");
        sb.append("  PRIMARY KEY (`id`)\n");
        sb.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
        return sb.toString();
    }

    public Page<Map<String, Object>> listData(String entityCode, int page, int pageSize, Map<String, String> filters) {
        EntityMeta entity = entityMetaRepository.findByCode(entityCode)
                .orElseThrow(() -> new IllegalArgumentException("实体不存在: " + entityCode));

        if (!"published".equals(entity.getStatus())) {
            throw new IllegalStateException("实体未发布，无法操作数据");
        }

        List<FieldMeta> fields = fieldMetaRepository.findByEntityIdOrderBySortOrderAsc(entity.getId());
        ensureTableExists(entity, fields);

        String tableName = entity.getTableName();

        StringBuilder sql = new StringBuilder("SELECT * FROM ").append(tableName);
        StringBuilder countSql = new StringBuilder("SELECT COUNT(*) FROM ").append(tableName);

        List<String> conditions = new ArrayList<>();
        MapSqlParameterSource params = new MapSqlParameterSource();

        if (filters != null && !filters.isEmpty()) {
            for (FieldMeta field : fields) {
                String filterValue = filters.get(field.getCode());
                if (filterValue != null && !filterValue.isEmpty()) {
                    conditions.add(field.getColumnName() + " LIKE :" + field.getCode());
                    params.addValue(field.getCode(), "%" + filterValue + "%");
                }
            }
        }

        if (!conditions.isEmpty()) {
            String whereClause = " WHERE " + String.join(" AND ", conditions);
            sql.append(whereClause);
            countSql.append(whereClause);
        }

        Long total = namedJdbcTemplate.queryForObject(countSql.toString(), params, Long.class);

        sql.append(" ORDER BY id DESC LIMIT :limit OFFSET :offset");
        params.addValue("limit", pageSize);
        params.addValue("offset", (page - 1) * pageSize);

        List<Map<String, Object>> rows = namedJdbcTemplate.queryForList(sql.toString(), params);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            result.add(convertRowToData(row, fields));
        }

        return new PageImpl<>(result, PageRequest.of(page - 1, pageSize), total != null ? total : 0);
    }

    public Map<String, Object> getDataById(String entityCode, Object id) {
        EntityMeta entity = entityMetaRepository.findByCode(entityCode)
                .orElseThrow(() -> new IllegalArgumentException("实体不存在: " + entityCode));

        if (!"published".equals(entity.getStatus())) {
            throw new IllegalStateException("实体未发布，无法操作数据");
        }

        List<FieldMeta> fields = fieldMetaRepository.findByEntityIdOrderBySortOrderAsc(entity.getId());
        ensureTableExists(entity, fields);

        String tableName = entity.getTableName();

        String sql = "SELECT * FROM " + tableName + " WHERE id = ?";
        Map<String, Object> row = jdbcTemplate.queryForMap(sql, id);

        return convertRowToData(row, fields);
    }

    @Transactional
    public Map<String, Object> createData(String entityCode, Map<String, Object> data) {
        EntityMeta entity = entityMetaRepository.findByCode(entityCode)
                .orElseThrow(() -> new IllegalArgumentException("实体不存在: " + entityCode));

        if (!"published".equals(entity.getStatus())) {
            throw new IllegalStateException("实体未发布，无法操作数据");
        }

        List<FieldMeta> fields = fieldMetaRepository.findByEntityIdOrderBySortOrderAsc(entity.getId());
        ensureTableExists(entity, fields);

        String tableName = entity.getTableName();

        List<String> columns = new ArrayList<>();
        List<String> placeholders = new ArrayList<>();
        List<Object> values = new ArrayList<>();

        for (FieldMeta field : fields) {
            if (field.getIsPrimaryKey() && field.getIsAutoIncrement()) {
                continue;
            }
            Object value = data.get(field.getCode());
            columns.add(field.getColumnName());
            placeholders.add("?");
            values.add(convertValue(value, field));
        }

        columns.add("created_at");
        placeholders.add("NOW()");
        columns.add("updated_at");
        placeholders.add("NOW()");

        String sql = "INSERT INTO " + tableName + " (" + String.join(", ", columns) + 
                    ") VALUES (" + String.join(", ", placeholders) + ")";
        
        jdbcTemplate.update(sql, values.toArray());

        Long newId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        return getDataById(entityCode, newId);
    }

    @Transactional
    public Map<String, Object> updateData(String entityCode, Object id, Map<String, Object> data) {
        EntityMeta entity = entityMetaRepository.findByCode(entityCode)
                .orElseThrow(() -> new IllegalArgumentException("实体不存在: " + entityCode));

        if (!"published".equals(entity.getStatus())) {
            throw new IllegalStateException("实体未发布，无法操作数据");
        }

        List<FieldMeta> fields = fieldMetaRepository.findByEntityIdOrderBySortOrderAsc(entity.getId());
        ensureTableExists(entity, fields);

        String tableName = entity.getTableName();

        List<String> sets = new ArrayList<>();
        List<Object> values = new ArrayList<>();

        for (FieldMeta field : fields) {
            if (field.getIsPrimaryKey()) {
                continue;
            }
            if (data.containsKey(field.getCode())) {
                sets.add(field.getColumnName() + " = ?");
                values.add(convertValue(data.get(field.getCode()), field));
            }
        }

        if (sets.isEmpty()) {
            return getDataById(entityCode, id);
        }

        sets.add("updated_at = NOW()");
        values.add(id);

        String sql = "UPDATE " + tableName + " SET " + String.join(", ", sets) + " WHERE id = ?";
        jdbcTemplate.update(sql, values.toArray());

        return getDataById(entityCode, id);
    }

    @Transactional
    public void deleteData(String entityCode, Object id) {
        EntityMeta entity = entityMetaRepository.findByCode(entityCode)
                .orElseThrow(() -> new IllegalArgumentException("实体不存在: " + entityCode));

        if (!"published".equals(entity.getStatus())) {
            throw new IllegalStateException("实体未发布，无法操作数据");
        }

        List<FieldMeta> fields = fieldMetaRepository.findByEntityIdOrderBySortOrderAsc(entity.getId());
        ensureTableExists(entity, fields);

        String tableName = entity.getTableName();
        String sql = "DELETE FROM " + tableName + " WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Transactional
    public void batchDelete(String entityCode, List<Object> ids) {
        EntityMeta entity = entityMetaRepository.findByCode(entityCode)
                .orElseThrow(() -> new IllegalArgumentException("实体不存在: " + entityCode));

        if (!"published".equals(entity.getStatus())) {
            throw new IllegalStateException("实体未发布，无法操作数据");
        }

        if (ids == null || ids.isEmpty()) {
            return;
        }

        List<FieldMeta> fields = fieldMetaRepository.findByEntityIdOrderBySortOrderAsc(entity.getId());
        ensureTableExists(entity, fields);

        String tableName = entity.getTableName();
        String sql = "DELETE FROM " + tableName + " WHERE id IN (:ids)";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("ids", ids);
        namedJdbcTemplate.update(sql, params);
    }

    private Map<String, Object> convertRowToData(Map<String, Object> row, List<FieldMeta> fields) {
        Map<String, Object> result = new LinkedHashMap<>();
        
        for (FieldMeta field : fields) {
            Object value = row.get(field.getColumnName());
            result.put(field.getCode(), value);
        }
        
        result.put("id", row.get("id"));
        result.put("createdAt", row.get("created_at"));
        result.put("updatedAt", row.get("updated_at"));
        
        return result;
    }

    private Object convertValue(Object value, FieldMeta field) {
        if (value == null) {
            return null;
        }

        try {
            switch (field.getFieldType()) {
                case "INTEGER":
                case "LONG":
                    if (value instanceof Number) {
                        return ((Number) value).longValue();
                    }
                    return Long.parseLong(value.toString());
                case "DOUBLE":
                    if (value instanceof Number) {
                        return ((Number) value).doubleValue();
                    }
                    return Double.parseDouble(value.toString());
                case "DECIMAL":
                    if (value instanceof Number) {
                        return ((Number) value).doubleValue();
                    }
                    return Double.parseDouble(value.toString());
                case "BOOLEAN":
                    if (value instanceof Boolean) {
                        return value;
                    }
                    return Boolean.parseBoolean(value.toString());
                default:
                    return value;
            }
        } catch (Exception e) {
            return value;
        }
    }
}
