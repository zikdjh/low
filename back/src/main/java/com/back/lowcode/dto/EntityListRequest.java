package com.back.lowcode.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 实体列表查询请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntityListRequest {

    @Builder.Default
    private Integer page = 1;

    @Builder.Default
    private Integer pageSize = 20;

    /** 搜索关键字（匹配名称或编码） */
    private String keyword;
}
