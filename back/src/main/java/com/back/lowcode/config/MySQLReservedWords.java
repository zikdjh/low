package com.back.lowcode.config;

import java.util.Set;

/**
 * MySQL 保留字集合
 * 用于校验用户定义的实体编码、字段编码（列名）是否与 MySQL 保留字冲突
 */
public final class MySQLReservedWords {

    private MySQLReservedWords() {
    }

    /**
     * MySQL 8.0 保留字关键字集合（常用）
     * 完整列表参考: https://dev.mysql.com/doc/refman/8.0/en/keywords.html
     */
    public static final Set<String> RESERVED_WORDS = Set.of(
            // 最常用/最容易误用的保留字
            "ACCESSIBLE", "ADD", "ALL", "ALTER", "ANALYZE", "AND", "AS",
            "ASC", "ASENSITIVE", "BEFORE", "BETWEEN", "BIGINT", "BINARY",
            "BLOB", "BOTH", "BY", "CALL", "CASCADE", "CASE", "CHANGE",
            "CHAR", "CHARACTER", "CHECK", "COLLATE", "COLUMN", "CONDITION",
            "CONSTRAINT", "CONTINUE", "CONVERT", "CREATE", "CROSS", "CUBE",
            "CUME_DIST", "CURRENT_DATE", "CURRENT_TIME", "CURRENT_TIMESTAMP",
            "CURRENT_USER", "CURSOR", "DATABASE", "DATABASES", "DAY_HOUR",
            "DAY_MINUTE", "DAY_SECOND", "DEC", "DECIMAL", "DECLARE",
            "DEFAULT", "DELAYED", "DELETE", "DESC", "DESCRIBE",
            "DISTINCT", "DISTINCTROW", "DIV", "DOUBLE", "DROP", "DUAL",
            "EACH", "ELSE", "ELSEIF", "ENCLOSED", "ESCAPED", "EXCEPT",
            "EXISTS", "EXIT", "EXPLAIN", "FALSE", "FETCH", "FIRST_VALUE",
            "FLOAT", "FLOAT4", "FLOAT8", "FOR", "FORCE", "FOREIGN", "FROM",
            "FULLTEXT", "FUNCTION", "GENERATED", "GET", "GRANT", "GROUP",
            "GROUPING", "GROUPS", "HAVING", "HIGH_PRIORITY", "HOUR_MINUTE",
            "HOUR_SECOND", "IF", "IGNORE", "IN", "INDEX", "INFILE",
            "INNER", "INOUT", "INSENSITIVE", "INSERT", "INT", "INT1",
            "INT2", "INT3", "INT4", "INT8", "INTEGER", "INTERVAL", "INTO",
            "IO_AFTER_GTIDS", "IO_BEFORE_GTIDS", "IS", "ITERATE", "JOIN",
            "JSON_TABLE", "KEY", "KEYS", "KILL", "LAG", "LAST_VALUE",
            "LATERAL", "LEAD", "LEADING", "LEFT", "LIKE", "LIMIT",
            "LINEAR", "LINES", "LOAD", "LOCALTIME", "LOCALTIMESTAMP",
            "LOCK", "LONG", "LONGBLOB", "LONGTEXT", "LOOP", "LOW_PRIORITY",
            "MASTER_BIND", "MASTER_SSL_VERIFY_SERVER_CERT", "MATCH",
            "MAXVALUE", "MEDIUMBLOB", "MEDIUMINT", "MEDIUMTEXT",
            "MIDDLEINT", "MINUTE_SECOND", "MOD", "MODIFIES", "NATURAL",
            "NOT", "NO_WRITE_TO_BINLOG", "NTH_VALUE", "NTILE", "NULL",
            "NUMERIC", "OF", "ON", "OPTIMIZE", "OPTIMIZER_COSTS",
            "OPTION", "OPTIONALLY", "OR", "ORDER", "OUT", "OUTER",
            "OUTFILE", "OVER", "PARTITION", "PERCENT_RANK", "PRECISION",
            "PRIMARY", "PROCEDURE", "PURGE", "RANGE", "RANK", "READ",
            "READS", "READ_WRITE", "REAL", "RECURSIVE", "REFERENCES",
            "REGEXP", "RELEASE", "RENAME", "REPEAT", "REPLACE",
            "REQUIRE", "RESIGNAL", "RESTRICT", "RETURN", "REVOKE",
            "RIGHT", "RLIKE", "ROW", "ROWS", "ROW_NUMBER", "SCHEMA",
            "SCHEMAS", "SECOND_MICROSECTION", "SELECT", "SENSITIVE",
            "SEPARATOR", "SET", "SHOW", "SIGNAL", "SMALLINT", "SPATIAL",
            "SPECIFIC", "SQL", "SQLEXCEPTION", "SQLSTATE", "SQLWARNING",
            "SQL_BIG_RESULT", "SQL_CALC_FOUND_ROWS", "SQL_SMALL_RESULT",
            "SSL", "STARTING", "STORED", "STRAIGHT_JOIN", "SYSTEM",
            "TABLE", "TERMINATED", "THEN", "TINYBLOB", "TINYINT",
            "TINYTEXT", "TO", "TRAILING", "TRIGGER", "TRUE", "UNDO",
            "UNION", "UNIQUE", "UNLOCK", "UNSIGNED", "UPDATE", "USAGE",
            "USE", "USING", "UTC_DATE", "UTC_TIME", "UTC_TIMESTAMP",
            "VALUES", "VARBINARY", "VARCHAR", "VARCHARACTER", "VARYING",
            "VIRTUAL", "WHEN", "WHERE", "WHILE", "WINDOW", "WITH",
            "WRITE", "XOR", "YEAR_MONTH", "ZEROFILL",

            // 低代码场景中极易误用的保留字
            "ORDER", "GROUP", "STATUS", "TYPE", "NAME", "KEY", "VALUE",
            "DATE", "DESC", "USER", "PASSWORD", "TABLE", "INDEX", "LOCK",
            "LEVEL", "COUNT", "SUM", "AVG", "MIN", "MAX", "SIZE", "COMMENT",
            "OPTION", "SESSION", "TRANSACTION", "VIEW", "FIELD", "DEFAULT"
    );

    /**
     * 判断给定名称是否为 MySQL 保留字（不区分大小写）
     *
     * @param name 待检测的名称
     * @return 如果是保留字返回 true
     */
    public static boolean isReserved(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        return RESERVED_WORDS.contains(name.toUpperCase());
    }
}
