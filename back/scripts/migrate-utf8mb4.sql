-- ============================================================
-- 一次性迁移：把 low_end 库 + lc_* 系列表/列升级到 utf8mb4
-- 适用：原表是 utf8 / utf8mb3，写入 4-byte 字符（emoji）报错被全局兜底成「操作失败」。
-- 安全性：ALTER TABLE 大表会锁表+复制，请在低峰期执行；先备份。
-- 执行：在 MySQL 客户端选中 low_end 库后整文件粘贴。
-- ============================================================

USE low_end;

-- 1) 库级默认字符集（影响之后 JPA ddl-auto 新建的表）
ALTER DATABASE low_end CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 2) 把所有 lc_ 前缀的低代码表转 utf8mb4（含表 default 和已有列）
--    使用 INFORMATION_SCHEMA 自动批量，避免漏表
SET @s := NULL;
SELECT GROUP_CONCAT(
         CONCAT('ALTER TABLE `', table_name, '` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci')
         SEPARATOR '; '
       )
INTO @s
FROM information_schema.tables
WHERE table_schema = 'low_end'
  AND table_name LIKE 'lc\_%' ESCAPE '\\';

-- 直接拼成单语句执行；@s 为 NULL（无 lc_* 表）则跳过
SET @s := IFNULL(CONCAT(@s, ';'), 'SELECT 1');
PREPARE stmt FROM @s;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3) 校验：列出当前 lc_* 表的字符集与列字符集
SELECT t.table_name,
       t.table_collation,
       (SELECT GROUP_CONCAT(DISTINCT character_set_name)
          FROM information_schema.columns c
         WHERE c.table_schema = t.table_schema AND c.table_name = t.table_name) AS column_charsets
FROM information_schema.tables t
WHERE t.table_schema = 'low_end' AND t.table_name LIKE 'lc\_%' ESCAPE '\\'
ORDER BY t.table_name;

-- 期望：全部表 table_collation = utf8mb4_unicode_ci，column_charsets = utf8mb4
