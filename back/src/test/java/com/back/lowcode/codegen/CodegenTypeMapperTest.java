package com.back.lowcode.codegen;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link CodegenTypeMapper} 单元测试 — 覆盖每个 FieldType 的 Java/TS/SQL 映射闭环
 */
class CodegenTypeMapperTest {

    @Test
    void resolve_varcharBaseline() {
        CodegenTypeMapper.Mapping m = CodegenTypeMapper.resolve("VARCHAR");
        assertEquals("String", m.getJavaType());
        assertEquals("string", m.getTsType());
        assertEquals("''", m.getTsDefault());
        assertTrue(m.getJavaImports().isEmpty());
        assertFalse(m.isTemporal());
        assertFalse(m.isNumeric());
    }

    @Test
    void resolve_textJsonReuseString() {
        assertEquals("String", CodegenTypeMapper.resolve("TEXT").getJavaType());
        assertEquals("String", CodegenTypeMapper.resolve("JSON").getJavaType());
    }

    @Test
    void resolve_integerLongRef() {
        assertEquals("Integer", CodegenTypeMapper.resolve("INTEGER").getJavaType());
        assertEquals("Long", CodegenTypeMapper.resolve("LONG").getJavaType());
        assertEquals("Long", CodegenTypeMapper.resolve("REFERENCE").getJavaType(),
                "REFERENCE 在 Java 层落到 Long（外键 id）");
    }

    @Test
    void resolve_decimalCarriesImport() {
        CodegenTypeMapper.Mapping m = CodegenTypeMapper.resolve("DECIMAL");
        assertEquals("BigDecimal", m.getJavaType());
        assertTrue(m.getJavaImports().contains("java.math.BigDecimal"));
        assertEquals("number", m.getTsType());
        assertTrue(m.isNumeric());
    }

    @Test
    void resolve_dateAndDateTimeAreTemporal() {
        CodegenTypeMapper.Mapping date = CodegenTypeMapper.resolve("DATE");
        assertEquals("LocalDate", date.getJavaType());
        assertTrue(date.getJavaImports().contains("java.time.LocalDate"));
        assertTrue(date.isTemporal());

        CodegenTypeMapper.Mapping dt = CodegenTypeMapper.resolve("DATETIME");
        assertEquals("LocalDateTime", dt.getJavaType());
        assertTrue(dt.getJavaImports().contains("java.time.LocalDateTime"));
        assertTrue(dt.isTemporal());
    }

    @Test
    void resolve_booleanDefaultsFalse() {
        CodegenTypeMapper.Mapping m = CodegenTypeMapper.resolve("BOOLEAN");
        assertEquals("Boolean", m.getJavaType());
        assertEquals("boolean", m.getTsType());
        assertEquals("false", m.getTsDefault());
    }

    @Test
    void resolve_doubleNumeric() {
        CodegenTypeMapper.Mapping m = CodegenTypeMapper.resolve("DOUBLE");
        assertEquals("Double", m.getJavaType());
        assertEquals("number", m.getTsType());
        assertTrue(m.isNumeric());
    }

    @Test
    void resolve_unknownFallsBackToVarchar() {
        assertEquals("String", CodegenTypeMapper.resolve(null).getJavaType());
        assertEquals("String", CodegenTypeMapper.resolve("").getJavaType());
        assertEquals("String", CodegenTypeMapper.resolve("UNKNOWN_TYPE").getJavaType());
    }

    @Test
    void resolve_caseInsensitive() {
        assertEquals("Integer", CodegenTypeMapper.resolve("integer").getJavaType());
        assertEquals("Integer", CodegenTypeMapper.resolve("Integer").getJavaType());
    }

    @Test
    void collectJavaImports_dedupsAcrossFields() {
        List<Map<String, Object>> fields = new ArrayList<>();
        fields.add(field("DECIMAL"));
        fields.add(field("DECIMAL"));
        fields.add(field("DATETIME"));
        fields.add(field("VARCHAR"));
        Set<String> imports = CodegenTypeMapper.collectJavaImports(fields);
        assertEquals(2, imports.size());
        assertTrue(imports.contains("java.math.BigDecimal"));
        assertTrue(imports.contains("java.time.LocalDateTime"));
    }

    @Test
    void mysqlColumnType_varcharWithLength() {
        assertEquals("VARCHAR(64)", CodegenTypeMapper.mysqlColumnType("VARCHAR", 64, null, null));
        assertEquals("VARCHAR", CodegenTypeMapper.mysqlColumnType("VARCHAR", null, null, null));
    }

    @Test
    void mysqlColumnType_decimalPrecisionScale() {
        assertEquals("DECIMAL(10,2)", CodegenTypeMapper.mysqlColumnType("DECIMAL", null, 10, 2));
        assertEquals("DECIMAL(8)", CodegenTypeMapper.mysqlColumnType("DECIMAL", null, 8, null));
        assertEquals("DECIMAL", CodegenTypeMapper.mysqlColumnType("DECIMAL", null, null, null));
    }

    @Test
    void mysqlColumnType_referenceIsBigint() {
        assertEquals("BIGINT", CodegenTypeMapper.mysqlColumnType("REFERENCE", null, null, null));
    }

    @Test
    void toCamelCase_basic() {
        assertEquals("customerName", CodegenTypeMapper.toCamelCase("customer_name"));
        assertEquals("id", CodegenTypeMapper.toCamelCase("id"));
        assertEquals("orderItemPrice", CodegenTypeMapper.toCamelCase("order_item_price"));
        assertEquals("", CodegenTypeMapper.toCamelCase(""));
        assertNull(CodegenTypeMapper.toCamelCase(null));
    }

    @Test
    void toPascalCase_basic() {
        assertEquals("CustomerName", CodegenTypeMapper.toPascalCase("customer_name"));
        assertEquals("Id", CodegenTypeMapper.toPascalCase("id"));
        assertEquals("OrderItemPrice", CodegenTypeMapper.toPascalCase("order_item_price"));
    }

    private Map<String, Object> field(String type) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("fieldType", type);
        return m;
    }
}
