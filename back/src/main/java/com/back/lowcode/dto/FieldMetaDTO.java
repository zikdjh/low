package com.back.lowcode.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 字段元数据请求/响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FieldMetaDTO {

    private Long id;
    private Long entityId;
    private String code;
    private String name;
    private String columnName;
    private String fieldType;
    private Integer length;
    private Integer precision;
    private Integer scale;
    private Boolean nullable;
    private String defaultValue;
    private Boolean isPrimaryKey;
    private Boolean isAutoIncrement;
    private Integer sortOrder;
    private Boolean showInList;
    private Boolean showInForm;
    private Boolean showInSearch;
    private String dictCode;
    private String validationRule;
}
