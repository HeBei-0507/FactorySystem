package com.hebei.systemdemo.exception;

import com.hebei.systemdemo.DTO.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器类
 * 用于统一处理系统中未被捕获的异常
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 全局异常处理方法
     * 捕获所有Exception类型的异常并进行统一处理
     *
     * @param e 捕获到的异常对象
     * @return 返回封装的错误结果信息
     */
    @ExceptionHandler(Exception.class)
    public Result exceptionHandler(Exception e) {
        log.error("全局异常捕获", e);
        String message = StringUtils.hasLength(e.getMessage()) ? e.getMessage() : e.getClass().getSimpleName();
        return Result.fail(message);
    }
}
