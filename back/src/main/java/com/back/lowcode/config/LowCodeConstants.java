package com.back.lowcode.config;

/**
 * 低代码平台常量
 */
public final class LowCodeConstants {

    private LowCodeConstants() {
    }

    /** 动态表名前缀，DDLService 仅操作此前缀的表 */
    public static final String TABLE_PREFIX = "lc_";

    /** Redis 缓存键前缀：实体字段元数据 */
    public static final String REDIS_META_KEY_PREFIX = "lc:meta:";

    /** API 路径前缀 */
    public static final String API_PREFIX = "/lowcode";

    /** 实体编码正则：小写字母开头，字母数字下划线 */
    public static final String ENTITY_CODE_PATTERN = "^[a-z][a-z0-9_]*$";

    /** 列名正则：字母数字下划线 */
    public static final String COLUMN_NAME_PATTERN = "^[a-zA-Z][a-zA-Z0-9_]*$";
}
