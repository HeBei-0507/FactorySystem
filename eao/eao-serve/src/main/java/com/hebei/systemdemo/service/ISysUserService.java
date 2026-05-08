package com.hebei.systemdemo.service;

import com.hebei.systemdemo.core.Result;
import com.hebei.systemdemo.domain.dto.user.LoginRequestDTO;
import com.hebei.systemdemo.domain.po.SysUser;

public interface ISysUserService {
    Result login(LoginRequestDTO loginRequestDTO);
    
    Result register(SysUser user);
    
    Result getUserInfo(Long id);
    
    Result updateUserInfo(SysUser user);
    
    Result logout();
}
