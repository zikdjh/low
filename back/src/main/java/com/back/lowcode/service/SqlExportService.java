package com.back.lowcode.service;

import com.back.lowcode.entity.EntityMeta;
import com.back.lowcode.entity.FieldMeta;
import com.back.lowcode.enums.FieldType;
import com.back.lowcode.repository.EntityMetaRepository;
import com.back.lowcode.repository.FieldMetaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * SQL 导出服务
 * 将实体生成 MySQL 导出脚本（含建表结构与数据）
 */
@Service
@RequiredArgsConstructor
public class SqlExportService {

    private final EntityMetaRepository entityMetaRepository;
    private final FieldMetaRepository fieldMetaRepository;
    private final JdbcTemplate jdbcTemplate;

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 为指定实体列表生成完整 SQL 导出脚本
     */
    public String exportEntities(List<Long> entityIds) {
        StringBuilder sb = new StringBuilder();
        sb.append("-- ==========================================\n");
        sb.append("-- 低代码平台 - 实体数据导出\n");
        sb.append("-- 导出时间: ").append(LocalDateTime.now().format(DT_FMT)).append("\n");
        sb.append("-- ==========================================\n\n");
        sb.append("SET NAMES utf8mb4;\n");
        sb.append("SET FOREIGN_KEY_CHECKS = 0;\n\n");

        for (Long entityId : entityIds) {
            EntityMeta entity = entityMetaRepository.findById(entityId).orElse(null);
            if (entity == null) {
                continue;
            }

            List<FieldMeta> fields = fieldMetaRepository.findByEntityIdOrderBySortOrderAsc(entityId);

            sb.append("-- ----------------------------\n");
            sb.append("-- 实体: ").append(entity.getName()).append(" (").append(entity.getCode()).append(")\n");
            sb.append("-- ----------------------------\n\n");

            // 1. DROP TABLE IF EXISTS
            sb.append("DROP TABLE IF EXISTS `").append(entity.getTableName()).append("`;\n\n");

            // 2. CREATE TABLE
            sb.append(buildCreateTableSql(entity, fields)).append("\n\n");

            // 3. INSERT 数据（仅已发布实体有物理表）
            if ("published".equals(entity.getStatus()) && tableExists(entity.getTableName())) {
                String inserts = buildInsertStatements(entity, fields);
                if (inserts != null && !inserts.isEmpty()) {
                    sb.append(inserts).append("\n");
                }
            }
        }

        sb.append("SET FOREIGN_KEY_CHECKS = 1;\n");
        return sb.toString();
    }

    /**
     * 为单个实体生成导出脚本
     */
    public String exportEntity(Long entityId) {
        return exportEntities(List.of(entityId));
    }

    // ---- CREATE TABLE 构建 ----

    private String buildCreateTableSql(EntityMeta entity, List<FieldMeta> fields) {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE `").append(entity.getTableName()).append("` (\n");

        List<FieldMeta> pkFields = fields.stream()
                .filter(f -> f.getIsPrimaryKey() != null && f.getIsPrimaryKey())
                .toList();

        boolean hasAutoId = pkFields.isEmpty();
        if (hasAutoId) {
            sb.append("  `id` BIGINT NOT NULL AUTO_INCREMENT,\n");
        }

        for (int i = 0; i < fields.size(); i++) {
            FieldMeta f = fields.get(i);
            sb.append("  `").append(f.getColumnName()).append("` ");
            sb.append(buildColumnDef(f));

            if (f.getIsPrimaryKey() != null && f.getIsPrimaryKey() && f.getIsAutoIncrement() != null && f.getIsAutoIncrement()) {
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

        sb.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");
        return sb.toString();
    }

    private String buildColumnDef(FieldMeta f) {
        try {
            FieldType type = FieldType.valueOf(f.getFieldType());
            String colType = type.buildColumnType(f.getLength(), f.getPrecision(), f.getScale());

            StringBuilder sb = new StringBuilder(colType);

            if (f.getNullable() != null && !f.getNullable() && (f.getIsPrimaryKey() == null || !f.getIsPrimaryKey())) {
                sb.append(" NOT NULL");
            }

            if (f.getDefaultValue() != null && !f.getDefaultValue().isEmpty()) {
                sb.append(" DEFAULT '").append(escapeSql(f.getDefaultValue())).append("'");
            }

            // 添加注释
            sb.append(" COMMENT '").append(escapeSql(f.getName())).append("'");

            return sb.toString();
        } catch (IllegalArgumentException e) {
            // 未知类型，回退为 VARCHAR
            return "VARCHAR(255) COMMENT '" + escapeSql(f.getName()) + "'";
        }
    }

    // ---- INSERT 语句构建 ----

    private String buildInsertStatements(EntityMeta entity, List<FieldMeta> fields) {
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT * FROM `" + entity.getTableName() + "`");

            if (rows.isEmpty()) {
                return "-- 无数据\n";
            }

            // 收集所有列名（包括 id, created_at, updated_at）
            List<String> allColumns = new ArrayList<>();
            boolean hasAutoId = fields.stream().noneMatch(f -> f.getIsPrimaryKey() != null && f.getIsPrimaryKey());
            if (hasAutoId) {
                allColumns.add("id");
            }
            for (FieldMeta f : fields) {
                allColumns.add(f.getColumnName());
            }
            allColumns.add("created_at");
            allColumns.add("updated_at");

            StringBuilder sb = new StringBuilder();
            sb.append("-- 数据: ").append(entity.getName()).append(" (").append(rows.size()).append(" 条记录)\n");

            // 批量 INSERT，每批最多 50 条
            int batchSize = 50;
            for (int i = 0; i < rows.size(); i += batchSize) {
                int end = Math.min(i + batchSize, rows.size());
                List<Map<String, Object>> batch = rows.subList(i, end);

                sb.append("INSERT INTO `").append(entity.getTableName()).append("` (");
                sb.append(allColumns.stream().map(c -> "`" + c + "`").collect(Collectors.joining(", ")));
                sb.append(") VALUES\n");

                for (int j = 0; j < batch.size(); j++) {
                    Map<String, Object> row = batch.get(j);
                    String values = allColumns.stream()
                            .map(col -> formatValue(row.get(col)))
                            .collect(Collectors.joining(", "));
                    sb.append("  (").append(values).append(")");
                    sb.append(j < batch.size() - 1 ? ",\n" : ";\n");
                }
                sb.append("\n");
            }

            return sb.toString();
        } catch (Exception e) {
            return "-- 读取数据失败: " + e.getMessage() + "\n";
        }
    }

    // ---- 工具方法 ----

    private boolean tableExists(String tableName) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.tables " +
                    "WHERE table_schema = DATABASE() AND table_name = ?",
                    Integer.class, tableName);
            return count != null && count > 0;
        } catch (Exception e) {
            // 回退方案：直接查表
            try {
                jdbcTemplate.queryForObject(
                        "SELECT 1 FROM `" + tableName + "` LIMIT 1", Integer.class);
                return true;
            } catch (Exception ex) {
                return false;
            }
        }
    }

    /**
     * 格式化 SQL 值
     */
    private String formatValue(Object value) {
        if (value == null) {
            return "NULL";
        }
        if (value instanceof Number) {
            return value.toString();
        }
        if (value instanceof Boolean) {
            return (Boolean) value ? "1" : "0";
        }
        if (value instanceof LocalDateTime) {
            return "'" + ((LocalDateTime) value).format(DT_FMT) + "'";
        }
        if (value instanceof java.time.LocalDate) {
            return "'" + value.toString() + "'";
        }
        if (value instanceof java.sql.Date) {
            return "'" + value.toString() + "'";
        }
        if (value instanceof java.sql.Timestamp) {
            return "'" + value.toString() + "'";
        }
        if (value instanceof byte[]) {
            return "X'" + bytesToHex((byte[]) value) + "'";
        }
        // 字符串及其他类型
        return "'" + escapeSql(value.toString()) + "'";
    }

    private String escapeSql(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
