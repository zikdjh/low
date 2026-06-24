package ${basePackage}.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
<#list entity.imports as imp>
import ${imp};
</#list>

/**
 * ${entity.name!entity.code} —— 由低代码平台从 release#${releaseId} 生成
 * <p>
 * 应用：${appCode} · 版本：${version}
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "${entity.tableName}")
public class ${entity.className} {

<#list entity.fields as f>
    <#if f.isPrimaryKey>
    @Id
    <#if f.isAutoIncrement>
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    </#if>
    </#if>
    <#if f.fieldType == "REFERENCE" && f.referenceEntityCode??>
    /** ${f.name!f.code} —— 引用 ${f.referenceEntityCode} */
    <#else>
    /** ${f.name!f.code} */
    </#if>
    @Column(name = "${f.columnName}"<#if !f.nullable>, nullable = false</#if><#if f.fieldType == "VARCHAR" && f.length??>, length = ${f.length?c}</#if><#if f.fieldType == "TEXT">, columnDefinition = "TEXT"</#if><#if f.fieldType == "JSON">, columnDefinition = "JSON"</#if>)
    private ${f.javaType} ${f.camelName};

</#list>
}
