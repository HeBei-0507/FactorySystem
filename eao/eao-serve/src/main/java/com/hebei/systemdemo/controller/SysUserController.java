package com.hebei.systemdemo.controller;

import com.hebei.systemdemo.core.Result;
import com.hebei.systemdemo.domain.dto.user.LoginRequestDTO;
import com.hebei.systemdemo.core.annotation.OperateLog;
import com.hebei.systemdemo.core.annotation.RequireLogin;
import com.hebei.systemdemo.domain.po.SysUser;
import com.hebei.systemdemo.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class SysUserController {
    
    @Autowired
    private ISysUserService sysUserService;

    @OperateLog(module = "用户管理", operation = "用户登录", saveParams = true, saveResult = false)
    @PostMapping("/login")
    public Result login(@RequestBody LoginRequestDTO loginRequestDTO) {
        return sysUserService.login(loginRequestDTO);
    }

    @OperateLog(module = "用户管理", operation = "用户注册", saveParams = true, saveResult = false)
    @PostMapping("/register")
    public Result register(@RequestBody SysUser user) {
        return sysUserService.register(user);
    }

    @RequireLogin
    @OperateLog(module = "用户管理", operation = "查询用户信息", saveParams = false, saveResult = false)
    @GetMapping("/info/{id}")
    public Result getUserInfo(@PathVariable Long id) {
        return sysUserService.getUserInfo(id);
    }

    @RequireLogin
    @OperateLog(module = "用户管理", operation = "更新用户信息", saveParams = true, saveResult = false)
    @PutMapping("/update")
    public Result updateUserInfo(@RequestBody SysUser user) {
        return sysUserService.updateUserInfo(user);
    }

    @RequireLogin
    @OperateLog(module = "用户管理", operation = "用户登出", saveParams = false, saveResult = false)
    @PostMapping("/logout")
    public Result logout() {
        return sysUserService.logout();
    }
}
