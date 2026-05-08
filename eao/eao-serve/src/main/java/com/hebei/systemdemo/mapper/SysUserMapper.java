package com.hebei.systemdemo.mapper;

import com.hebei.systemdemo.domain.po.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysUserMapper {
    SysUser getByUsername(@Param("username") String username);
    
    SysUser getById(@Param("id") Long id);
    
    int insert(SysUser user);
    
    int updateById(SysUser user);
    
    int deleteById(@Param("id") Long id);
}
