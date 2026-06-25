package ${pkgBase}.entity;

<#list imports as imp>
import ${imp};
</#list>
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ${entity.description!entity.name} — 由低代码代码生成器生成
 * 源实体编码: ${entity.code}
 * 生成时间: ${now}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "${entity.tableName}")
public class ${entity.className} {

<#if !hasUserPk>
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

</#if>
<#list fields as f>
    /** ${f.name}<#if f.dictCode??> （字典: ${f.dictCode}）</#if> */
    <#if f.isPrimaryKey>
    @Id
    <#if f.isAutoIncrement>@GeneratedValue(strategy = GenerationType.IDENTITY)</#if>
    </#if>
    @Column(name = "${f.columnName}"<#if !f.nullable>, nullable = false</#if><#if f.maxLength??>, length = ${f.maxLength}</#if>)
    private ${f.javaType} ${f.camelName};

</#list>
    /** 创建时间 */
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
