package ${basePackage}.dto;

import lombok.Data;
<#list entity.imports as imp>
import ${imp};
</#list>

/**
 * ${entity.name!entity.code} DTO —— 自动生成（用于接口入参/出参解耦）
 */
@Data
public class ${entity.className}DTO {

<#list entity.fields as f>
    /** ${f.name!f.code} */
    private ${f.javaType} ${f.camelName};

</#list>
}
