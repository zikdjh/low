package com.back.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target({ElementType.METHOD}) // 标识该注解类用于方法
@Retention(RetentionPolicy.RUNTIME) // 任何时候都保留
public @interface RateLimit {

    // 限流key前缀
    String prefix() default "rate_limit";

    // 限流时间
    int time() default 60;

    // 单位秒
    TimeUnit unit() default TimeUnit.SECONDS;

    // 限流次数
    int count() default 1;

    // 限流提示语
    String message() default "请求过于频繁，请稍后再试";
}
