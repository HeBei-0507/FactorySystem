package com.hebei.systemdemo.core.aspect;

import com.hebei.systemdemo.core.Result;
import com.hebei.systemdemo.core.constant.ResultCode;
import com.hebei.systemdemo.core.UserContext;
import com.hebei.systemdemo.mapper.SysUserMapper;
import com.hebei.systemdemo.domain.po.SysUser;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class LoginAspect {
    private static final Logger log = LoggerFactory.getLogger(LoginAspect.class);

    @Autowired
    private SysUserMapper sysUserMapper;

    @Around("@annotation(com.hebei.systemdemo.core.annotation.RequireLogin)")
    public Object checkLogin(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return Result.fail(ResultCode.FAIL, "无法获取请求上下文");
        }

        HttpServletRequest request = attributes.getRequest();
        String authorization = request.getHeader("Authorization");

        if (authorization == null || authorization.isEmpty()) {
            return Result.fail(ResultCode.UNAUTHORIZED, "未登录，请先登录");
        }

        String token = extractToken(authorization);
        if (token == null || token.isEmpty()) {
            return Result.fail(ResultCode.UNAUTHORIZED, "无效的token格式");
        }

        Long userId = extractUserIdFromToken(token);
        if (userId == null) {
            return Result.fail(ResultCode.UNAUTHORIZED, "无效的token");
        }

        SysUser user = sysUserMapper.getById(userId);
        if (user == null) {
            return Result.fail(ResultCode.UNAUTHORIZED, "用户不存在");
        }

        if (user.getStatus() == 0) {
            return Result.fail(ResultCode.FORBIDDEN, "用户已被禁用");
        }

        if (user.getIsDeleted() == 1) {
            return Result.fail(ResultCode.FORBIDDEN, "用户已被删除");
        }

        UserContext.setUser(user);
        log.debug("用户上下文设置成功: {}, ID: {}", user.getUsername(), user.getId());

        try {
            return joinPoint.proceed();
        } finally {
            UserContext.clear();
            log.debug("用户上下文已清理");
        }
    }

    private String extractToken(String authorization) {
        if (authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return authorization;
    }

    private Long extractUserIdFromToken(String token) {
        try {
            String[] parts = token.split("_");
            if (parts.length == 2) {
                return Long.parseLong(parts[1]);
            }
        } catch (NumberFormatException e) {
            log.error("解析token失败: {}", token);
        }
        return null;
    }
}
