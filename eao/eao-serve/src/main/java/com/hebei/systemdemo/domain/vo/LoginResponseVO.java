package com.hebei.systemdemo.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseVO {
    private Long userId;
    private String username;
    private String realName;
    private String token;
}
