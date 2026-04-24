package com.hebei.systemdemo.pojo;

import lombok.Data;

import java.util.List;

@Data
public class PageBean <T>{
    private Long total; // 总记录数
    private Long current;
    private Long size;
    private List<T> rows; // 当前页结果
}
