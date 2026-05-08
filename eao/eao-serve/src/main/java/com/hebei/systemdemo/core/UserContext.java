package com.hebei.systemdemo.core;

import com.hebei.systemdemo.domain.po.SysUser;

public class UserContext {
    private static final ThreadLocal<SysUser> userHolder = new ThreadLocal<>();

    public static void setUser(SysUser user) {
        userHolder.set(user);
    }

    public static SysUser getUser() {
        return userHolder.get();
    }

    public static Long getUserId() {
        SysUser user = userHolder.get();
        return user != null ? user.getId() : null;
    }

    public static String getUsername() {
        SysUser user = userHolder.get();
        return user != null ? user.getUsername() : null;
    }

    public static void clear() {
        userHolder.remove();
    }
}
