package com.back.lowcode.codegen;

import com.back.lowcode.enums.FieldType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 字段类型映射 — 把 {@link FieldType} 翻译成生成代码所需的具体类型字面量
 * <p>
 * 三套映射并存：
 * <ul>
 *   <li>Java（JPA 实体 / DTO / 校验导入）</li>
 *   <li>TypeScript（前端 form / table 字段类型 + 默认空值）</li>
 *   <li>MySQL DDL（CREATE TABLE 列定义，复用 {@link FieldType#buildColumnType}）</li>
 * </ul>
 * 设计要点：
 * <ul>
 *   <li>纯函数 — 不依赖 Spring 容器，便于在模板渲染与单测里直接用</li>
 *   <li>{@link Mapping} 同时携带 java import 列表，模板渲染时合并去重生成 import 块</li>
 *   <li>REFERENCE 类型不在这里展开 — 由 {@link CodegenContext} 根据 referenceEntityCode 二次决议</li>
 * </ul>
 */
public final class CodegenTypeMapper {

    private CodegenTypeMapper() {}

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Mapping {
        /** Java 类型字面量，例如 "String" / "BigDecimal" / "LocalDateTime" */
        private String javaType;
        /** 该 Java 类型需要的 import 全限定名（基本类型/同包类返回空集合） */
        private Set<String> javaImports;
        /** TypeScript 类型字面量，例如 "string" / "number" / "boolean" */
        private String tsType;
        /** TypeScript 默认空值字面量，用于表单初始化，例如 "''" / "null" / "false" */
        private String tsDefault;
        /** 是否日期/时间类（前端用 t-date-picker 渲染） */
        private boolean temporal;
        /** 是否数值类（前端 t-input-number） */
        private boolean numeric;
    }

    /**
     * 主入口 — 给定字段元数据，返回完整映射。
     *
     * @param fieldType FieldType 枚举名（大写）；null / 未知值按 VARCHAR 兜底
     */
    public static Mapping resolve(String fieldType) {
        FieldType ft = parse(fieldType);
        switch (ft) {
            case VARCHAR:
            case TEXT:
            case JSON:
                return Mapping.builder()
                        .javaType("String")
                        .javaImports(Set.of())
                        .tsType("string")
                        .tsDefault("''")
                        .temporal(false)
                        .numeric(false)
                        .build();
            case INTEGER:
                return Mapping.builder()
                        .javaType("Integer")
                        .javaImports(Set.of())
                        .tsType("number")
                        .tsDefault("null")
                        .temporal(false)
                        .numeric(true)
                        .build();
            case LONG:
            case REFERENCE:
                return Mapping.builder()
                        .javaType("Long")
                        .javaImports(Set.of())
                        .tsType("number")
                        .tsDefault("null")
                        .temporal(false)
                        .numeric(true)
                        .build();
            case DOUBLE:
                return Mapping.builder()
                        .javaType("Double")
                        .javaImports(Set.of())
                        .tsType("number")
                        .tsDefault("null")
                        .temporal(false)
                        .numeric(true)
                        .build();
            case DECIMAL:
                return Mapping.builder()
                        .javaType("BigDecimal")
                        .javaImports(Set.of("java.math.BigDecimal"))
                        .tsType("number")
                        .tsDefault("null")
                        .temporal(false)
                        .numeric(true)
                        .build();
            case BOOLEAN:
                return Mapping.builder()
                        .javaType("Boolean")
                        .javaImports(Set.of())
                        .tsType("boolean")
                        .tsDefault("false")
                        .temporal(false)
                        .numeric(false)
                        .build();
            case DATE:
                return Mapping.builder()
                        .javaType("LocalDate")
                        .javaImports(Set.of("java.time.LocalDate"))
                        .tsType("string")
                        .tsDefault("''")
                        .temporal(true)
                        .numeric(false)
                        .build();
            case DATETIME:
                return Mapping.builder()
                        .javaType("LocalDateTime")
                        .javaImports(Set.of("java.time.LocalDateTime"))
                        .tsType("string")
                        .tsDefault("''")
                        .temporal(true)
                        .numeric(false)
                        .build();
            default:
                throw new IllegalStateException("Unhandled FieldType: " + ft);
        }
    }

    /**
     * 给定字段集合，归并出 entity / dto 文件需要的 import 全限定名（已去重并排序）。
     */
    public static Set<String> collectJavaImports(List<Map<String, Object>> fields) {
        Set<String> imports = new LinkedHashSet<>();
        for (Map<String, Object> f : fields) {
            String ft = (String) f.get("fieldType");
            Mapping m = resolve(ft);
            imports.addAll(m.getJavaImports());
        }
        return imports;
    }

    /**
     * MySQL 列类型字面量 — 直接转发到 {@link FieldType#buildColumnType}，
     * 但把 {@link FieldType#TEXT} / {@link FieldType#JSON} 的"无长度"语义统一处理。
     */
    public static String mysqlColumnType(String fieldType, Integer length, Integer precision, Integer scale) {
        FieldType ft = parse(fieldType);
        return ft.buildColumnType(length, precision, scale);
    }

    /** 把字段名转为驼峰（首字母小写） */
    public static String toCamelCase(String snake) {
        if (snake == null || snake.isEmpty()) return snake;
        String[] parts = snake.split("_");
        StringBuilder sb = new StringBuilder(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            if (parts[i].isEmpty()) continue;
            sb.append(Character.toUpperCase(parts[i].charAt(0))).append(parts[i].substring(1));
        }
        return sb.toString();
    }

    /** 把字段名转为大驼峰（首字母大写）—— 用于 Java 类名 */
    public static String toPascalCase(String snake) {
        String camel = toCamelCase(snake);
        if (camel == null || camel.isEmpty()) return camel;
        return Character.toUpperCase(camel.charAt(0)) + camel.substring(1);
    }

    private static FieldType parse(String fieldType) {
        if (fieldType == null || fieldType.isBlank()) return FieldType.VARCHAR;
        try {
            return FieldType.valueOf(fieldType.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return FieldType.VARCHAR;
        }
    }
}
