package com.back.common;

import java.util.concurrent.TimeUnit;

public class Constant {

    // 项目名称
    public static final String PROJECT_NAME = "end";

    // 一般通用线程池核心线程数
    public static final Integer THREAD_POOL_EXECUTOR_CORE_POOL_SIZE = 5;
    // 轻量通用线程池核心线程数
    public static final Integer THREAD_POOL_SMALL_EXECUTOR_CORE_POOL_SIZE = 1;
    // 重度通用线程池核心线程数
    public static final Integer THREAD_POOL_BIG_EXECUTOR_CORE_POOL_SIZE = 15;
    // 浏览线程池专用核心线程数
    public static final Integer THREAD_POOL_VIEW_EXECUTOR_CORE_POOL_SIZE = 20;

    // 一般通用线程池最大线程数
    public static final Integer THREAD_POOL_EXECUTOR_MAXIMUM_POOL_SIZE = 20;
    // 轻量通用线程池最大线程数
    public static final Integer THREAD_POOL_SMALL_EXECUTOR_MAXIMUM_POOL_SIZE = 4;
    // 重度通用线程池最大线程数
    public static final Integer THREAD_POOL_BIG_EXECUTOR_MAXIMUM_POOL_SIZE = 50;
    // 浏览线程池最大线程数
    public static final Integer THREAD_POOL_VIEW_EXECUTOR_MAXIMUM_POOL_SIZE = 50;

    // 通用线程池线程空闲时间
    public static final Long THREAD_POOL_EXECUTOR_KEEP_ALIVE_TIME = 60L;

    // 通用线程池线程空闲时间单位
    public static final TimeUnit THREAD_POOL_EXECUTOR_UNIT = TimeUnit.SECONDS;

    // 一般通用线程池队列容量
    public static final Integer THREAD_POOL_EXECUTOR_THREAD = 50;
    // 轻量通用线程池队列容量
    public static final Integer THREAD_POOL_SMALL_EXECUTOR_THREAD = 5;
    // 重度通用线程池队列容量
    public static final Integer THREAD_POOL_BIG_EXECUTOR_THREAD = 100;
    // 浏览线程池队列容量
    public static final Integer THREAD_POOL_VIEW_EXECUTOR_THREAD = 100;
    // 删除数据队列容量
    public static final Integer THREAD_POOL_DELETE_EXECUTOR_THREAD_FACTORY = 100;

    // 令牌类型
    public static final String ACCESS_TOKEN_TYPE = "access";
    public static final String REFRESH_TOKEN_TYPE = "refresh";
}
