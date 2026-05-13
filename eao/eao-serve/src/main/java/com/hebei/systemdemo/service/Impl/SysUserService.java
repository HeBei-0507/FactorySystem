package com.hebei.systemdemo.service.Impl;

import cn.hutool.crypto.digest.DigestUtil;
import com.hebei.systemdemo.core.Result;
import com.hebei.systemdemo.domain.dto.user.LoginRequestDTO;
import com.hebei.systemdemo.domain.vo.LoginResponseVO;
import com.hebei.systemdemo.core.UserContext;
import com.hebei.systemdemo.mapper.SysUserMapper;
import com.hebei.systemdemo.domain.po.SysUser;
import com.hebei.systemdemo.service.ISysUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class SysUserService implements ISysUserService {
    private static final Logger log = LoggerFactory.getLogger(SysUserService.class);

    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    public Result login(LoginRequestDTO loginRequestDTO) {
        if (!StringUtils.hasText(loginRequestDTO.getUsername()) || !StringUtils.hasText(loginRequestDTO.getPassword())) {
            return Result.fail("用户名和密码不能为空");
        }

        SysUser user = sysUserMapper.getByUsername(loginRequestDTO.getUsername());
        if (user == null) {
            return Result.fail("用户不存在");
        }

        if (user.getStatus() == 0) {
            return Result.fail("用户已被禁用");
        }

        String inputPasswordHash = DigestUtil.md5Hex(loginRequestDTO.getPassword());
        if (!inputPasswordHash.equals(user.getPasswordHash())) {
            return Result.fail("密码错误");
        }

        String token = generateToken(user.getId());
        
        UserContext.setUser(user);
        
        LoginResponseVO response = new LoginResponseVO();
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setRealName(user.getRealName());
        response.setToken(token);

        log.info("用户登录成功: {}", user.getUsername());
        return Result.ok(response);
    }

    @Override
    public Result register(SysUser user) {
        return Result.fail("考试环境已禁用注册功能");
    }

    @Override
    public Result getUserInfo(Long id) {
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) {
            return Result.fail("未登录，请先登录");
        }
        if (id == null) {
            return Result.fail("用户ID不能为空");
        }
        if (!currentUserId.equals(id)) {
            return Result.fail("仅允许查看当前登录用户信息");
        }

        SysUser user = sysUserMapper.getById(currentUserId);
        if (user == null) {
            return Result.fail("用户不存在");
        }

        user.setPasswordHash(null);
        return Result.ok(user);
    }

    @Override
    public Result updateUserInfo(SysUser user) {
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) {
            return Result.fail("未登录，请先登录");
        }
        if (user.getId() == null) {
            return Result.fail("用户ID不能为空");
        }
        if (!currentUserId.equals(user.getId())) {
            return Result.fail("仅允许修改当前登录用户信息");
        }

        SysUser existUser = sysUserMapper.getById(currentUserId);
        if (existUser == null) {
            return Result.fail("用户不存在");
        }

        if (StringUtils.hasText(user.getPasswordHash())) {
            user.setPasswordHash(DigestUtil.md5Hex(user.getPasswordHash()));
        } else {
            user.setPasswordHash(null);
        }

        user.setUpdatedAt(LocalDateTime.now());
        int rows = sysUserMapper.updateById(user);
        if (rows <= 0) {
            return Result.fail("更新用户信息失败");
        }

        log.info("用户信息更新成功, ID: {}", user.getId());
        return Result.ok();
    }

    @Override
    public Result logout() {
        UserContext.clear();
        log.info("用户退出登录");
        return Result.ok();
    }

    private String generateToken(Long userId) {
        return UUID.randomUUID().toString().replace("-", "") + "_" + userId;
    }
}
