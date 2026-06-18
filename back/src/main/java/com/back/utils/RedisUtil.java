package com.back.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.ObjectUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Slf4j
public class RedisUtil {

    /**
     * 保存对象为json字符串到redis hash结构
     *
     * @param stringRedisTemplate redis 模板
     * @param objectMapper        对象映射器
     * @param key                 键
     * @param t                   值
     * @param getHashKey          获取 hash键的函数
     * @param expireTime          过期时间
     * @param timeUnit            时间单位
     * @param <T>                 值类型
     * @param <ID>                hash 键类型
     */
    public static <T, ID> void saveJsonToRedisHash(
            @Nonnull StringRedisTemplate stringRedisTemplate,
            @Nonnull ObjectMapper objectMapper,
            @Nonnull String key,
            @Nonnull T t,
            @Nonnull Function<T, ID> getHashKey,
            Long expireTime, TimeUnit timeUnit
    ) {
        // 转换为 json字符串
        try {
            String json = objectMapper.writeValueAsString(t);

            // 保存到redis hash结构
            stringRedisTemplate.opsForHash().put(key, getHashKey.apply(t), json);

            // 设置过期时间
            if (!ObjectUtils.isEmpty(expireTime) && !ObjectUtils.isEmpty(timeUnit) && expireTime > 0) {
                stringRedisTemplate.expire(key, expireTime, timeUnit);
            }
        } catch (JsonProcessingException e) {
            log.error("保存对象为json字符串到redis异常: {}", e.getMessage(), e);
        }
    }

    /**
     * 从redis hash结构获取json字符串
     *
     * @param stringRedisTemplate redis 模板
     * @param objectMapper        对象映射器
     * @param key                 键
     * @param hashKey             hash键
     * @param type                值类型
     * @param expireTime          过期时间
     * @param timeUnit            时间单位
     * @param <T>                 值类型
     * @return 值
     */
    public static <T> T getJsonFromRedisHash(
            @Nonnull StringRedisTemplate stringRedisTemplate,
            @Nonnull ObjectMapper objectMapper,
            @Nonnull String key,
            @Nonnull String hashKey,
            @Nonnull Class<T> type,
            Long expireTime, TimeUnit timeUnit
    ) {

        // 从redis hash结构获取json字符串
        Object object = stringRedisTemplate.opsForHash().get(key, hashKey);

        // 如果为空，直接返回null
        if (ObjectUtils.isEmpty(object)) {
            return null;
        }

        try {
            // 转换为对象
            String json = object.toString();

            // 转换为对象
            T t = objectMapper.readValue(json, type);

            // 设置过期时间
            if (!ObjectUtils.isEmpty(expireTime) && !ObjectUtils.isEmpty(timeUnit) && expireTime > 0) {
                stringRedisTemplate.expire(key, expireTime, timeUnit);
            }

            return t;
        } catch (JsonProcessingException e) {
            log.error("从Redis Hash获取json字符串失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 保存对象为json字符串到redis hash结构
     *
     * @param stringRedisTemplate redis 模板
     * @param objectMapper        对象映射器
     * @param keyPrefix           键前缀
     * @param t                   值
     * @param getHashKey          获取 hash键的函数
     * @param expireTime          过期时间
     * @param timeUnit            时间单位
     * @param <T>                 值类型
     * @param <ID>                hash 键类型
     */
    public static <T, ID> void saveObjectToRedisHash(
            @Nonnull StringRedisTemplate stringRedisTemplate,
            @Nonnull ObjectMapper objectMapper,
            @Nonnull String keyPrefix,
            @Nonnull T t,
            @Nonnull Function<T, ID> getHashKey,
            Long expireTime, TimeUnit timeUnit
    ) {

        String key = keyPrefix + getHashKey.apply(t);
        try {
            // 获取 hashmap
            Map<String, String> stringMap = ObjectToMap(objectMapper, t);

            // 保存到redis hash结构
            stringRedisTemplate.opsForHash().putAll(key, stringMap);

            // 设置过期时间
            if (!ObjectUtils.isEmpty(expireTime) && !ObjectUtils.isEmpty(timeUnit) && expireTime > 0) {
                stringRedisTemplate.expire(key, expireTime, timeUnit);
            }
        } catch (Exception e) {
            log.error("保存对象为hash结构到redis异常\n键：{}\n,值：{}\n异常信息：{}", key, t, e.getMessage());
        }
    }

    /**
     * 从redis hash结构获取对象
     *
     * @param stringRedisTemplate redis 模板
     * @param objectMapper        对象映射器
     * @param key                 键
     * @param type                值类型
     * @param expireTime          过期时间
     * @param timeUnit            时间单位
     * @param <T>                 值类型
     * @return 值
     */
    public static <T> T getObjectFromRedisHash(
            @Nonnull StringRedisTemplate stringRedisTemplate,
            @Nonnull ObjectMapper objectMapper,
            @Nonnull String key,
            @Nonnull Class<T> type,
            Long expireTime, TimeUnit timeUnit
    ) {

        try {

            // 设置过期时间
            if (!ObjectUtils.isEmpty(expireTime) && !ObjectUtils.isEmpty(timeUnit) && expireTime > 0) {
                stringRedisTemplate.expire(key, expireTime, timeUnit);
            }

            // 获取所有字段
            Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(key);

            // 如果为空，直接返回null
            if (entries.isEmpty()) {
                return null;
            }

            return mapToBean(entries, type, objectMapper);

        } catch (Exception e) {
            log.error("从Redis Hash获取{}}失败: {}", key, e.getMessage(), e);
            return null;
        }
    }


    /**
     * 从redis hash结构获取列表对象
     *
     * @param stringRedisTemplate redis 模板
     * @param objectMapper        对象映射器
     * @param hashKeyPrefix       hash 键前缀
     * @param type                值类型
     * @param expire              过期时间
     * @param unit                时间单位
     * @param <T>                 值类型
     * @param count               扫描数量
     * @return 值列表
     */
    public static <T> List<T> getListObjectFromRedisHash(
            @Nonnull StringRedisTemplate stringRedisTemplate,
            @Nonnull ObjectMapper objectMapper,
            @Nonnull String hashKeyPrefix,
            @Nonnull Class<T> type,
            Long expire, TimeUnit unit,
            Long count
    ) {

        // 首先获取所有匹配的 key
        Set<String> keys = scanKeys(stringRedisTemplate, hashKeyPrefix, count);

        return getList(stringRedisTemplate, objectMapper, type, expire, unit, keys);
    }

    /**
     * 保存列表对象为 json字符串到redis hash结构
     *
     * @param stringRedisTemplate redis 模板
     * @param objectMapper        对象映射器
     * @param hashKeyPrefix       hash 键前缀
     * @param list                值列表
     * @param getId               获取 hash键的函数
     * @param expireTime          过期时间
     * @param timeUnit            时间单位
     * @param <T>                 值类型
     * @param <ID>                hash 键类型
     */
    public static <T, ID> void saveListObjectToRedisHash(
            @Nonnull StringRedisTemplate stringRedisTemplate,
            @Nonnull ObjectMapper objectMapper,
            @Nonnull String hashKeyPrefix,
            @Nonnull List<T> list,
            @Nonnull Function<T, ID> getId,
            Long expireTime, TimeUnit timeUnit
    ) {

        // 如果为空，直接返回
        if (list.isEmpty()) {
            return;
        }

        // 使用executePipelined实现管道批量操作，这在集群模式下是安全的
        stringRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            StringRedisConnection stringConnection = (StringRedisConnection) connection;
            try {
                for (T t : list) {
                    ID id = getId.apply(t);
                    String hashKey = hashKeyPrefix + id;

                    // 转换为String Map
                    Map<String, String> stringMap = ObjectToMap(objectMapper, t);

                    // 批量存储到 Hash
                    stringConnection.hMSet(hashKey, stringMap);

                    // 设置过期时间
                    if (!ObjectUtils.isEmpty(expireTime) && !ObjectUtils.isEmpty(timeUnit) && expireTime > 0) {
                        stringConnection.expire(hashKey, timeUnit.toSeconds(expireTime));
                    }
                }
            } catch (Exception e) {
                log.error("批量保存对象到Redis Hash失败: {}", e.getMessage(), e);
                // 在管道中，单个命令的失败不会中止整个管道，但会记录错误。
                // 这里抛出异常以通知调用者操作可能部分失败。
                throw new RuntimeException("批量保存失败", e);
            }
            return null; // 返回值在管道操作中通常是 null
        });
    }

    private static <T> List<T> getList(
            @Nonnull StringRedisTemplate stringRedisTemplate,
            @Nonnull ObjectMapper objectMapper,
            @Nonnull Class<T> type,
            Long expire, TimeUnit unit,
            Set<String> keys) {

        // 如果为空，直接返回空列表
        if (keys.isEmpty()) {
            return List.of();
        }

        // 使用 RedisCallback实现管道批量获取
        List<Object> results = stringRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            StringRedisConnection stringConnection = (StringRedisConnection) connection;

            for (String key : keys) {
                // 批量获取 Hash的所有字段
                stringConnection.hGetAll(key);

                // 刷新过期时间
                if (!ObjectUtils.isEmpty(expire) && (!ObjectUtils.isEmpty(unit)) && expire > 0) {
                    stringConnection.expire(key, unit.toSeconds(expire));
                }
            }

            return null; // RedisCallback的返回值会被忽略，结果通过executePipelined返回
        });

        // 转换结果为对象列表
        List<T> resultList = new ArrayList<>();
        for (Object result : results) {
            if (result instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<Object, Object> hashData = (Map<Object, Object>) result;

                if (!hashData.isEmpty()) {
                    try {
                        T object = mapToBean(hashData, type, objectMapper);
                        resultList.add(object);
                    } catch (Exception e) {
                        log.error("转换Redis Hash数据为对象失败: {}", e.getMessage(), e);
                    }
                }
            }
        }

        return resultList;
    }

    /**
     * 将Map转换为对象 (Fix: Parses JSON strings back to complex types)
     */
    private static <T> T mapToBean(Map<Object, Object> map, Class<T> type, ObjectMapper objectMapper) {
        Map<String, Object> convertedMap = new HashMap<>();

        // 获取所有字段类型映射，避免将本来就是String的JSON字符串错误解析
        Map<String, Class<?>> fieldTypes = new HashMap<>();
        Class<?> clazz = type;
        while (!ObjectUtils.isEmpty(clazz) && clazz != Object.class) {
            for (Field f : clazz.getDeclaredFields()) {
                fieldTypes.put(f.getName(), f.getType());
            }
            clazz = clazz.getSuperclass();
        }

        for (Map.Entry<Object, Object> entry : map.entrySet()) {
            String key = entry.getKey().toString();
            Object valueObj = entry.getValue();
            if (ObjectUtils.isEmpty(valueObj)) continue;

            String value = valueObj.toString();
            Class<?> fieldType = fieldTypes.get(key);

            boolean shouldParse = (value.startsWith("[") || value.startsWith("{"));

            if (!ObjectUtils.isEmpty(fieldType) && CharSequence.class.isAssignableFrom(fieldType)) {
                shouldParse = false;
            }

            if (shouldParse) {
                try {
                    Object parsed = objectMapper.readValue(value, Object.class);
                    convertedMap.put(key, parsed);
                } catch (Exception e) {
                    convertedMap.put(key, value);
                }
            } else {
                convertedMap.put(key, value);
            }
        }
        return objectMapper.convertValue(convertedMap, type);
    }

    /**
     * 将对象转换为字符串 Map
     *
     * @param objectMapper 对象映射器
     * @param t            对象
     * @param <T>          对象类型
     * @return 字符串 Map
     */
    public static <T> Map<String, String> ObjectToMap(
            @Nonnull ObjectMapper objectMapper,
            @Nonnull T t
    ) {

        // 将对象转换为 Map
        Map<String, Object> beanMap = objectMapper.convertValue(t, new TypeReference<>() {
        });

        // 将Map中的值转换为 String（Redis Hash要求）
        // Fix: Serialize complex types (List, Map, Object) to JSON strings instead of calling toString()
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, Object> entry : beanMap.entrySet()) {
            Object value = entry.getValue();
            if (ObjectUtils.isEmpty(value)) {
                continue;
            }

            String key = entry.getKey();
            if (value instanceof String) {
                result.put(key, (String) value);
            } else if (value instanceof Number || value instanceof Boolean) {
                result.put(key, String.valueOf(value));
            } else {
                try {
                    // Serialize complex objects/collections to JSON string
                    result.put(key, objectMapper.writeValueAsString(value));
                } catch (Exception e) {
                    log.warn("Failed to serialize field {} to JSON, falling back to toString", key);
                    result.put(key, value.toString());
                }
            }
        }
        return result;
    }

    /**
     * 使用SCAN命令获取匹配的键，替代KEYS命令，避免阻塞
     *
     * @param stringRedisTemplate redis 模板
     * @param pattern             匹配模式，如 "user:*"
     * @param count               每次SCAN返回的键数量建议值，默认10
     * @return 匹配的键集合
     */
    private static Set<String> scanKeys(
            @Nonnull StringRedisTemplate stringRedisTemplate,
            @Nonnull String pattern,
            Long count
    ) {
        Set<String> keys = new HashSet<>();

        // 构建 SCAN选项
        ScanOptions.ScanOptionsBuilder builder = ScanOptions.scanOptions()
                .match(pattern);

        if (!ObjectUtils.isEmpty(count) && count > 0) {
            builder.count(count);
        }

        ScanOptions scanOptions = builder.build();

        // 使用 SCAN命令遍历键
        try (Cursor<String> cursor = stringRedisTemplate.scan(scanOptions)) {
            while (cursor.hasNext()) {
                keys.add(cursor.next());
            }
        } catch (Exception e) {
            log.error("SCAN键失败，pattern: {}, 错误: {}", pattern, e.getMessage(), e);
        }

        return keys;
    }

    /**
     * 使用SCAN命令检查键是否存在，替代KEYS命令，避免阻塞
     *
     * @param stringRedisTemplate redis 模板
     * @param pattern             匹配模式，如 "user:*"
     * @param count               每次 SCAN返回的键数量建议值
     * @return 是否存在匹配的键
     */
    public static boolean unExistsKeysByPattern(
            @Nonnull StringRedisTemplate stringRedisTemplate,
            @Nonnull String pattern,
            Long count
    ) {
        // 构建 SCAN选项
        ScanOptions.ScanOptionsBuilder builder = ScanOptions.scanOptions()
                .match(pattern);

        if (count != null && count > 0) {
            builder.count(count);
        }

        ScanOptions scanOptions = builder.build();

        // 使用 SCAN命令检查是否存在匹配的键
        try (Cursor<String> cursor = stringRedisTemplate.scan(scanOptions)) {
            return !cursor.hasNext();
        } catch (Exception e) {
            log.error("SCAN检查键存在性失败，pattern: {}, 错误: {}", pattern, e.getMessage(), e);
            return true;
        }
    }
}

