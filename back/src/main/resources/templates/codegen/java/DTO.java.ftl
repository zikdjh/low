package ${pkgBase}.dto;

<#list imports as imp>
import ${imp};
</#list>
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * ${entity.description!entity.name} — DTO
 * 由低代码代码生成器生成 @ ${now}
 */
@Data
public class ${entity.className}DTO {

<#if !hasUserPk>
    private Long id;

</#if>
<#list fields as f>
    /** ${f.name} */
    <#if f.required>@NotNull(message = "${f.name}不能为空")</#if>
    <#if f.javaType == "String" && f.maxLength??>@Size(max = ${f.maxLength}, message = "${f.name}长度不能超过${f.maxLength}")</#if>
    private ${f.javaType} ${f.camelName};

</#list>
}
