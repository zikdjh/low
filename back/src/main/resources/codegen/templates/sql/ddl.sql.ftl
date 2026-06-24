-- =========================================================================
-- 应用 ${appCode}（${appName!''}）DDL —— release#${releaseId} · v${version}
-- 由低代码平台从 release 快照自动生成；执行前请确认目标数据库已存在
-- checksum: ${checksum!''}
-- =========================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

<#list entities as entity>
-- -------------------------------------------------------------------------
-- 表：${entity.tableName} (${entity.name!entity.code})
-- -------------------------------------------------------------------------
DROP TABLE IF EXISTS `${entity.tableName}`;
CREATE TABLE `${entity.tableName}` (
<#list entity.fields as f>
    `${f.columnName}` ${f.mysqlType}<#if f.isPrimaryKey && f.isAutoIncrement> AUTO_INCREMENT</#if><#if !f.nullable> NOT NULL</#if><#if f.defaultValue?? && f.defaultValue?length gt 0> DEFAULT '${f.defaultValue}'</#if><#if f.name?? && f.name?length gt 0> COMMENT '${f.name?replace("'", "''")}'</#if>,
</#list>
<#if entity.primaryKeyField??>
    PRIMARY KEY (`${entity.primaryKeyField.columnName}`)
<#else>
    PRIMARY KEY (`id`)
</#if>
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci<#if entity.description?? && entity.description?length gt 0> COMMENT='${entity.description?replace("'", "''")}'</#if>;

</#list>
SET FOREIGN_KEY_CHECKS = 1;
