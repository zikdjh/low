-- ============================================================
-- 一次性迁移：把 low_end 库 + lc_* 系列表/列升级到 utf8mb4
-- 适用：原表是 utf8 / utf8mb3，写入 4-byte 字符（emoji）报错被全局兜底成「操作失败」。
-- 安全性：ALTER TABLE 大表会锁表+复制，请在低峰期执行；先备份。
-- 执行：在 MySQL 客户端选中 low_end 库后整文件粘贴。
-- ============================================================

USE low_end;

-- 1) 库级默认字符集（影响之后 JPA ddl-auto 新建的表）
ALTER DATABASE low_end CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 2) 逐表 ALTER（PREPARE 不支持多语句，必须用存储过程游标循环）
DROP PROCEDURE IF EXISTS lc_convert_utf8mb4;
DELIMITER $$
CREATE PROCEDURE lc_convert_utf8mb4()
BEGIN
    DECLARE done INT DEFAULT 0;
    DECLARE tname VARCHAR(128);
    DECLARE cur CURSOR FOR
        SELECT table_name
        FROM information_schema.tables
        WHERE table_schema = 'low_end'
          AND table_name LIKE 'lc\_%' ESCAPE '\\';
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

    OPEN cur;
    convert_loop: LOOP
        FETCH cur INTO tname;
        IF done THEN
            LEAVE convert_loop;
        END IF;
        SET @sql := CONCAT('ALTER TABLE `', tname,
                           '` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci');
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END LOOP;
    CLOSE cur;
END$$
DELIMITER ;

CALL lc_convert_utf8mb4();
DROP PROCEDURE lc_convert_utf8mb4;

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
