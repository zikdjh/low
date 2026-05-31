package com.back.config.basic;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ObjectUtils;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.back.common.Constant.*;


@Slf4j
@Configuration
public class ThreadPoolConfig {

    @Bean("deleteDataExecutor")
    public ThreadPoolExecutor deleteDataExecutor() {
        return new ThreadPoolExecutor(
                THREAD_POOL_EXECUTOR_CORE_POOL_SIZE,
                THREAD_POOL_EXECUTOR_MAXIMUM_POOL_SIZE,
                THREAD_POOL_EXECUTOR_KEEP_ALIVE_TIME,
                THREAD_POOL_EXECUTOR_UNIT,
                new LinkedBlockingQueue<>(THREAD_POOL_DELETE_EXECUTOR_THREAD_FACTORY),
                // 直接拒绝
                new ThreadPoolExecutor.DiscardPolicy()
        );
    }

    // JVM关闭时优雅关闭线程池
    @PreDestroy
    public void shutdownExecutors() {
        try {
            if (!ObjectUtils.isEmpty(deleteDataExecutor())) {
                deleteDataExecutor().shutdown();
                if (!deleteDataExecutor().awaitTermination(60, TimeUnit.SECONDS)) {
                    deleteDataExecutor().shutdownNow();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("线程池关闭被中断", e);
        }
    }
}
