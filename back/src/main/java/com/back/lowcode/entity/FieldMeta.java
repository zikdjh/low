package com.back.lowcode.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 字段元数据 — 实体下的字段定义
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "lc_field_meta")
public class FieldMeta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 所属实体 ID */
    @Column(nullable = false)
    private Long entityId;

    /** 字段编码，如 "customer_name" */
    @Column(nullable = false, length = 64)
    private String code;

    /** 字段显示名称，如 "客户姓名" */
    @Column(name = "field_name", nullable = false, length = 128)
    private String name;

    /** 数据库列名，与 code 相同或由系统生成 */
    @Column(nullable = false, length = 64)
    private String columnName;

    /** 字段类型，对应 FieldType 枚举名 */
    @Column(nullable = false, length = 16)
    private String fieldType;

    /** VARCHAR 长度 */
    @Column(name = "str_length")
    private Integer length;

    /** DECIMAL 精度 */
    @Column(name = "dec_precision")
    private Integer precision;

    /** DECIMAL 标度 */
    private Integer scale;

    /** 是否可为空 */
    @Builder.Default
    private Boolean nullable = true;

    /** 默认值（字符串形式） */
    @Column(length = 512)
    private String defaultValue;

    /** 是否为主键 */
    @Builder.Default
    private Boolean isPrimaryKey = false;

    /** 是否自增 */
    @Builder.Default
    private Boolean isAutoIncrement = false;

    /** 排序号 */
    @Builder.Default
    private Integer sortOrder = 0;

    /** 是否在列表页显示 */
    @Builder.Default
    private Boolean showInList = true;

    /** 是否在表单中显示 */
    @Builder.Default
    private Boolean showInForm = true;

    /** 是否在搜索栏中显示 */
    @Builder.Default
    private Boolean showInSearch = false;

    /** 关联字典编码（下拉框数据源） */
    @Column(length = 64)
    private String dictCode;

    /** 关联实体编码（外键引用，用于 REFERENCE 类型字段） */
    @Column(name = "reference_entity_code", length = 64)
    private String referenceEntityCode;

    /** 引用实体的显示字段编码，用于下拉显示文本（默认取主键） */
    @Column(name = "reference_display_field_code", length = 64)
    private String referenceDisplayFieldCode;

    /**
     * 校验规则 JSON 字符串
     * 格式: {"required":true,"min":1,"max":100,"pattern":"..."}
     */
    @Column(length = 1024)
    private String validationRule;
}
