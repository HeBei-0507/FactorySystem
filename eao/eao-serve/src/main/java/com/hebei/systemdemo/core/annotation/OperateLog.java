package com.hebei.systemdemo.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperateLog {
    String module() default "";
    
    String operation() default "";
    
    boolean saveParams() default true;
    
    boolean saveResult() default false;
}
