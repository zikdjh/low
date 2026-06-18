package com.back.lowcode.dto;

import lombok.Data;

/**
 * 页面 Schema 请求/响应 DTO
 */
@Data
public class PageSchemaDTO {

    private Long id;
    private String name;
    private String code;
    private String pageType;
    private String layoutJson;
    private Integer version;
    private String status;
}
