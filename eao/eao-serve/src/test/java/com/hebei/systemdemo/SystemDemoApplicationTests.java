package com.hebei.systemdemo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import cn.hutool.crypto.digest.DigestUtil;

@SpringBootTest
class SystemDemoApplicationTests {

    @Test
    void contextLoads() {
        System.out.println(DigestUtil.md5Hex("123456"));
        System.out.println(DigestUtil.md5Hex("577184"));
        System.out.println(DigestUtil.md5Hex("655302"));
        System.out.println(DigestUtil.md5Hex("434667"));
        System.out.println(DigestUtil.md5Hex("041128"));

    }

}
