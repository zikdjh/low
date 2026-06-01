package com.back.lowcode.dto;

import lombok.Data;

import java.util.List;

/**
 * 动态数据请求 DTO
 */
@Data
public class DynamicDataRequest {
    
    private List<Object> ids;
}
