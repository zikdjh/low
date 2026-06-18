package com.back.exception;

import com.back.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.concurrent.RejectedExecutionException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 业务逻辑异常 — 返回真实错误消息
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("业务校验失败: {}", e.getMessage());
        return Result.error(e.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleIllegalStateException(IllegalStateException e) {
        log.warn("状态校验失败: {}", e.getMessage());
        return Result.error(e.getMessage());
    }

    // 统一异常处理（兜底）
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleException(Exception e) {
        log.info(e.getClass().getName());
        log.error(StringUtils.hasLength(e.getMessage()) ? e.getMessage() : "操作失败");
        return Result.error("操作失败,请检查信息");
    }

    // 访问路径异常
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result handleNoResourceException(NoResourceFoundException e) {
        log.error(StringUtils.hasLength(e.getMessage()) ? e.getMessage() : "无效路径");
        return Result.error("无效路径");
    }

    // 参数校验
    @ExceptionHandler(HandlerMethodValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleHandlerMethodValidationException(HandlerMethodValidationException e) {
        log.error(e.getClass().getName());
        return Result.error(e.getMessage());
    }

    // 缺少参数
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        log.error(StringUtils.hasLength(e.getMessage()) ? e.getMessage() : "参数错误");
        return Result.error("缺少必要参数");
    }

    // 超出线程池等待队列
    @ExceptionHandler(RejectedExecutionException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleRejectedExecutionException(RejectedExecutionException e) {
        log.error(StringUtils.hasLength(e.getMessage()) ? e.getMessage() : "任务被拒");
        return Result.error("服务繁忙，请稍后重试");
    }

    // TODO 处理请求方法不支持异常，仅做调试使用，正式部署注释掉本异常
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public Result handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.error(StringUtils.hasLength(e.getMessage()) ? e.getMessage() : "请求方法错误");
        return Result.error("请求方法错误");
    }
}