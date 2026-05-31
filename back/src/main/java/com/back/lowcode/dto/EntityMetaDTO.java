package com.back.lowcode.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 实体元数据请求/响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntityMetaDTO {

    private Long id;
    private String code;
    private String name;
    private String tableName;
    private String description;
    private String status;
    private String createdAt;
    private String updatedAt;
}
