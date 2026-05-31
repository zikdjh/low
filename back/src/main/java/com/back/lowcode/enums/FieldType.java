package com.back.lowcode.enums;

import lombok.Getter;

/**
 * 动态实体字段类型枚举
 * 定义支持的字段类型及其对应的 MySQL 列类型和 Java 类型
 */
@Getter
public enum FieldType {

    VARCHAR("VARCHAR", "String", true, false, false),
    INTEGER("INT", "Integer", false, false, false),
    LONG("BIGINT", "Long", false, false, false),
    DOUBLE("DOUBLE", "Double", false, false, false),
    BOOLEAN("TINYINT(1)", "Boolean", false, false, false),
    DATE("DATE", "LocalDate", false, false, false),
    DATETIME("DATETIME", "LocalDateTime", false, false, false),
    TEXT("TEXT", "String", false, false, false),
    JSON("JSON", "String", false, false, false),
    DECIMAL("DECIMAL", "BigDecimal", false, true, true);

    /** MySQL 列类型名 */
    private final String mysqlType;
    /** 对应的 Java 类型名 */
    private final String javaType;
    /** 是否需要指定长度（如 VARCHAR(255)） */
    private final boolean needsLength;
    /** 是否需要指定精度（如 DECIMAL(10,2)） */
    private final boolean needsPrecision;
    /** 是否需要指定标度 */
    private final boolean needsScale;

    FieldType(String mysqlType, String javaType, boolean needsLength, boolean needsPrecision, boolean needsScale) {
        this.mysqlType = mysqlType;
        this.javaType = javaType;
        this.needsLength = needsLength;
        this.needsPrecision = needsPrecision;
        this.needsScale = needsScale;
    }

    /**
     * 构建完整的 MySQL 列类型字符串
     * @param length 长度（VARCHAR 使用）
     * @param precision 精度（DECIMAL 使用）
     * @param scale 标度（DECIMAL 使用）
     * @return 例如 "VARCHAR(255)" 或 "DECIMAL(10,2)" 或 "INT"
     */
    public String buildColumnType(Integer length, Integer precision, Integer scale) {
        StringBuilder sb = new StringBuilder(mysqlType);
        if (needsPrecision && precision != null && precision > 0) {
            sb.append("(").append(precision);
            if (needsScale && scale != null && scale >= 0) {
                sb.append(",").append(scale);
            }
            sb.append(")");
        } else if (needsLength && length != null && length > 0) {
            sb.append("(").append(length).append(")");
        }
        return sb.toString();
    }
}
