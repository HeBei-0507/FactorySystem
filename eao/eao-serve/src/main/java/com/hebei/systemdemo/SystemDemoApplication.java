package com.hebei.systemdemo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@MapperScan("com.hebei.systemdemo.mapper")
@EnableAspectJAutoProxy
public class SystemDemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(SystemDemoApplication.class, args);
    }

}
