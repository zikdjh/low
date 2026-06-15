package com.back.lowcode.service;

import com.back.lowcode.config.LowCodeConstants;
import com.back.lowcode.config.MySQLReservedWords;
import com.back.lowcode.entity.DdlLog;
import com.back.lowcode.entity.EntityMeta;
import com.back.lowcode.entity.FieldMeta;
import com.back.lowcode.enums.FieldType;
import com.back.lowcode.repository.DdlLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 动态 DDL 服务
 * 根据实体元数据生成并执行 CREATE TABLE / ALTER TABLE 语句
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DDLService {

    private final JdbcTemplate jdbcTemplate;
    private final DdlLogRepository ddlLogRepository;

    private static final Pattern COLUMN_PATTERN = Pattern.compile(LowCodeConstants.COLUMN_NAME_PATTERN);

    /**
     * 根据实体元数据和字段列表生成并执行 CREATE TABLE 语句
     *
     * @param entity 实体元数据
     * @param fields 字段列表
     */
    @Transactional
    public void generateCreateTable(EntityMeta entity, List<FieldMeta> fields) {
        validateTableName(entity.getTableName());

        String sql = buildCreateTableSql(entity, fields);
        log.warn("[DDL] Executing: {}", sql);

        DdlLog.DdlLogBuilder logBuilder = DdlLog.builder()
                .entityId(entity.getId())
                .sqlStatement(sql)
                .operationType("CREATE_TABLE");

        try {
            jdbcTemplate.execute(sql);
            ddlLogRepository.save(logBuilder.result("SUCCESS").build());
            log.info("[DDL] Table {} created successfully", entity.getTableName());
        } catch (Exception e) {
            ddlLogRepository.save(logBuilder.result("FAILED").errorMessage(e.getMessage()).build());
            log.error("[DDL] Failed to create table {}: {}", entity.getTableName(), e.getMessage(), e);
            throw new RuntimeException("创建数据表失败: " + e.getMessage(), e);
        }
    }

    /**
     * Diff 新旧字段并生成 ALTER TABLE 语句
     * 新字段 → ADD COLUMN；旧字段被删除 → 软删除（重命名为 __deleted_xxx）
     */
    @Transactional
    public void generateAlterTable(EntityMeta entity, List<FieldMeta> oldFields, List<FieldMeta> newFields) {
        validateTableName(entity.getTableName());

        // 找出新增字段（在新列表但不在旧列表中，按 code 比较）
        java.util.Set<String> oldCodes = oldFields.stream().map(FieldMeta::getCode).collect(Collectors.toSet());
        List<FieldMeta> toAdd = newFields.stream()
                .filter(f -> !oldCodes.contains(f.getCode()))
                .toList();

        // 找出删除字段（在旧列表但不在新列表中）
        java.util.Set<String> newCodes = newFields.stream().map(FieldMeta::getCode).collect(Collectors.toSet());
        List<FieldMeta> toRemove = oldFields.stream()
                .filter(f -> !newCodes.contains(f.getCode()))
                .toList();

        for (FieldMeta field : toAdd) {
            String sql = buildAddColumnSql(entity.getTableName(), field);
            executeDDL(entity.getId(), sql, "ALTER_TABLE");
        }

        for (FieldMeta field : toRemove) {
            // 软删除：重命名而非 DROP
            String sql = buildRenameColumnSql(entity.getTableName(), field.getColumnName(),
                    "__deleted_" + field.getColumnName());
            executeDDL(entity.getId(), sql, "ALTER_TABLE");
        }
    }

    /**
     * 删除动态表
     */
    @Transactional
    public void dropTable(EntityMeta entity) {
        validateTableName(entity.getTableName());
        String sql = "DROP TABLE IF EXISTS `" + entity.getTableName() + "`";
        executeDDL(entity.getId(), sql, "DROP_TABLE");
    }

    // ---- SQL 构建 ----

    private String buildCreateTableSql(EntityMeta entity, List<FieldMeta> fields) {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE IF NOT EXISTS `").append(entity.getTableName()).append("` (\n");

        List<FieldMeta> pkFields = fields.stream()
                .filter(FieldMeta::getIsPrimaryKey)
                .toList();

        boolean hasAutoId = pkFields.isEmpty();
        if (hasAutoId) {
            sb.append("  `id` BIGINT NOT NULL AUTO_INCREMENT,\n");
        }

        for (int i = 0; i < fields.size(); i++) {
            FieldMeta f = fields.get(i);
            validateColumnName(f.getColumnName());

            sb.append("  `").append(f.getColumnName()).append("` ");

            FieldType type = FieldType.valueOf(f.getFieldType());
            sb.append(type.buildColumnType(f.getLength(), f.getPrecision(), f.getScale()));

            if (!f.getNullable() && !f.getIsPrimaryKey()) {
                sb.append(" NOT NULL");
            }

            if (f.getDefaultValue() != null && !f.getDefaultValue().isEmpty()) {
                sb.append(" DEFAULT '").append(f.getDefaultValue()).append("'");
            }

            if (f.getIsPrimaryKey() && f.getIsAutoIncrement()) {
                sb.append(" AUTO_INCREMENT");
            }

            sb.append(",\n");
        }

        sb.append("  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,\n");
        sb.append("  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,\n");

        if (hasAutoId) {
            sb.append("  PRIMARY KEY (`id`)\n");
        } else if (!pkFields.isEmpty()) {
            sb.append("  PRIMARY KEY (");
            sb.append(pkFields.stream()
                    .map(f -> "`" + f.getColumnName() + "`")
                    .collect(Collectors.joining(", ")));
            sb.append(")\n");
        }

        sb.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
        return sb.toString();
    }

    private String buildAddColumnSql(String tableName, FieldMeta field) {
        FieldType type = FieldType.valueOf(field.getFieldType());
        StringBuilder sb = new StringBuilder();
        sb.append("ALTER TABLE `").append(tableName).append("` ")
                .append("ADD COLUMN `").append(field.getColumnName()).append("` ")
                .append(type.buildColumnType(field.getLength(), field.getPrecision(), field.getScale()));

        if (!field.getNullable()) {
            sb.append(" NOT NULL");
        }
        if (field.getDefaultValue() != null && !field.getDefaultValue().isEmpty()) {
            sb.append(" DEFAULT '").append(field.getDefaultValue()).append("'");
        }
        return sb.toString();
    }

    private String buildRenameColumnSql(String tableName, String oldName, String newName) {
        // MySQL 8.0+: ALTER TABLE ... RENAME COLUMN old TO new
        return "ALTER TABLE `" + tableName + "` RENAME COLUMN `" + oldName + "` TO `" + newName + "`";
    }

    private void executeDDL(Long entityId, String sql, String operationType) {
        log.warn("[DDL] Executing: {}", sql);
        DdlLog.DdlLogBuilder logBuilder = DdlLog.builder()
                .entityId(entityId)
                .sqlStatement(sql)
                .operationType(operationType);

        try {
            jdbcTemplate.execute(sql);
            ddlLogRepository.save(logBuilder.result("SUCCESS").build());
        } catch (Exception e) {
            ddlLogRepository.save(logBuilder.result("FAILED").errorMessage(e.getMessage()).build());
            log.error("[DDL] Failed: {}", e.getMessage(), e);
            throw new RuntimeException("执行 DDL 失败: " + e.getMessage(), e);
        }
    }

    private void validateTableName(String tableName) {
        if (tableName == null || !tableName.startsWith(LowCodeConstants.TABLE_PREFIX)) {
            throw new IllegalArgumentException(
                    "表名必须以 '" + LowCodeConstants.TABLE_PREFIX + "' 开头: " + tableName);
        }
    }

    private void validateColumnName(String columnName) {
        if (columnName == null || !COLUMN_PATTERN.matcher(columnName).matches()) {
            throw new IllegalArgumentException("非法列名: " + columnName);
        }
        if (MySQLReservedWords.isReserved(columnName)) {
            throw new IllegalArgumentException(
                    "列名不能使用 MySQL 保留字: '" + columnName + "'，请换一个名称");
        }
    }
}
