package com.hebei.systemdemo.core;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OperateLog {
    private Long id;
    private String module;
    private String operation;
    private String method;
    private String requestMethod;
    private String requestUrl;
    private String ipAddress;
    private Long userId;
    private String username;
    private String params;
    private String result;
    private Integer status;
    private String errorMsg;
    private Long executeTime;
    private LocalDateTime operateTime;
}
