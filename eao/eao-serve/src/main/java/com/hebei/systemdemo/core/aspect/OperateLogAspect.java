package com.hebei.systemdemo.core.aspect;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.Header;
import cn.hutool.json.JSONUtil;
import com.hebei.systemdemo.core.UserContext;
import com.hebei.systemdemo.core.annotation.OperateLog;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
@Aspect
@Component
public class OperateLogAspect {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Around("@annotation(com.hebei.systemdemo.core.annotation.OperateLog)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        OperateLog operateLog = method.getAnnotation(OperateLog.class);
        
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;
        
        String module = operateLog.module();
        String operation = operateLog.operation();
        String className = joinPoint.getTarget().getClass().getName();
        String methodName = method.getName();
        String fullMethodName = className + "." + methodName;
        
        String requestMethod = request != null ? request.getMethod() : "UNKNOWN";
        String requestUrl = request != null ? request.getRequestURI() : "UNKNOWN";
        String ipAddress = getIpAddress(request);
        
        Long userId = UserContext.getUserId();
        String username = UserContext.getUsername();
        
        String params = "";
        if (operateLog.saveParams()) {
            Object[] args = joinPoint.getArgs();
            params = Arrays.stream(args)
                    .map(arg -> arg != null ? JSONUtil.toJsonStr(arg) : "null")
                    .collect(Collectors.joining(", "));
        }
        
        Object result = null;
        Integer status = 1;
        String errorMsg = "";
        
        try {
            result = joinPoint.proceed();
            
            long executeTime = System.currentTimeMillis() - startTime;
            
            String resultStr = "";
            if (operateLog.saveResult() && result != null) {
                resultStr = JSONUtil.toJsonStr(result);
            }
            
            String logMessage = buildLogMessage(module, operation, fullMethodName, requestMethod, 
                    requestUrl, ipAddress, userId, username, params, resultStr, 
                    executeTime, status, errorMsg);
            
            log.info("\n{}", logMessage);
            
            return result;
            
        } catch (Throwable e) {
            long executeTime = System.currentTimeMillis() - startTime;
            status = 0;
            errorMsg = e.getMessage();
            
            String logMessage = buildLogMessage(module, operation, fullMethodName, requestMethod, 
                    requestUrl, ipAddress, userId, username, params, "", 
                    executeTime, status, errorMsg);
            
            log.error("\n{}", logMessage, e);
            
            throw e;
        }
    }
    
    private String buildLogMessage(String module, String operation, String method, 
                                   String requestMethod, String requestUrl, String ipAddress,
                                   Long userId, String username, String params, String result,
                                   Long executeTime, Integer status, String errorMsg) {
        StringBuilder sb = new StringBuilder();
        String operateTime = LocalDateTime.now().format(FORMATTER);
        
        sb.append("【操作日志】").append(operateTime).append("\n");
        sb.append("├─ 操作模块: ").append(module).append("\n");
        sb.append("├─ 操作描述: ").append(operation).append("\n");
        sb.append("├─ 请求方法: ").append(method).append("\n");
        sb.append("├─ 请求方式: ").append(requestMethod).append("\n");
        sb.append("├─ 请求URL: ").append(requestUrl).append("\n");
        sb.append("├─ IP地址: ").append(ipAddress).append("\n");
        sb.append("├─ 操作用户: ").append(username).append("(ID:").append(userId).append(")").append("\n");
        sb.append("├─ 请求参数: ").append(StrUtil.subPre(params, 2000)).append("\n");
        
        if (StrUtil.isNotBlank(result)) {
            sb.append("├─ 返回结果: ").append(StrUtil.subPre(result, 2000)).append("\n");
        }
        
        sb.append("├─ 执行耗时: ").append(executeTime).append("ms").append("\n");
        sb.append("├─ 操作状态: ").append(status == 1 ? "成功" : "失败").append("\n");
        
        if (status == 0 && StrUtil.isNotBlank(errorMsg)) {
            sb.append("└─ 异常信息: ").append(errorMsg);
        } else {
            sb.append("└─ --------------------");
        }
        
        return sb.toString();
    }
    
    private String getIpAddress(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }

        String ip = request.getHeader("X-Forwarded-For");

        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        return StrUtil.isNotBlank(ip) ? ip : "unknown";
    }
}
