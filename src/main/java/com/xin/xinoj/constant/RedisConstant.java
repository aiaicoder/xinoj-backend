package com.xin.xinoj.constant;

/**
 * @author 15712
 * redis常量
 */
public interface RedisConstant {

    /**
     * 缓存空值时间
     */
    Long CACHE_NULL_TTL = 2L;

    /**
     * 缓存题目分页时间
     */
    Long CACHE_QUESTION_PAGE_TTL = 1L;


    Long CACHE_QUESTION_TTL = 1L;
}
