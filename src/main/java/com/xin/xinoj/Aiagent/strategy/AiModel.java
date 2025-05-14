package com.xin.xinoj.Aiagent.strategy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标识AI模型策略支持的模型类型
 * @author 15712
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AiModel {
    /**
     * 模型代码
     */
    String value();
    
    /**
     * 是否为默认策略
     */
    boolean isDefault() default false;
} 