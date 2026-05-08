package com.hebei.systemdemo.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result {
    private Integer code;
    private String message;
    private Object data;
    private Long total;

    public static Result ok(){
        return new Result(200, "操作成功", null, null);
    }
    
    public static Result ok(Object data){
        return new Result(200, "操作成功", data,null);
    }

    public static Result ok(String message, Object data){
        return new Result(200, message, data,null);
    }
    
    public static Result fail(String message){
        return new Result(500, message, null,null);
    }
    
    public static Result fail(Integer code, String message){
        return new Result(code, message, null,null);
    }
    
    public static Result fail(Integer code, String message, Object data){
        return new Result(code, message, data,null);
    }
    public static Result ok(List<?> data, Long total){
        return new Result(200, null, data, total);
    }
}