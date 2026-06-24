package ${basePackage}.dto;

import lombok.Data;
<#list entity.imports as imp>
import ${imp};
</#list>

/**
 * ${entity.name!entity.code} 查询条件 —— 自动生成
 * <p>
 * 仅包含 showInSearch = true 的字段，配合 service 中的查询方法使用。
 */
@Data
public class ${entity.className}Query {

    /** 页码（从 0 开始） */
    private Integer page = 0;
    /** 每页条数 */
    private Integer size = 10;
    /** 排序字段 */
    private String sort;
    /** 排序方向：asc / desc */
    private String order;

<#list entity.fields as f>
    <#if f.showInSearch>
    /** ${f.name!f.code} */
    private ${f.javaType} ${f.camelName};

    </#if>
</#list>
}
