package com.xin.xinoj.constant;

/**
 * RedisKey类用于定义Redis键的前缀常量
 *
 * @author xin
 */
public interface RedisKeyConstant {
    /**
     * 提交限制的Redis键前缀
     */
    String LIMIT_KEY_PREFIX = "oj:commit:limit";

    /**
     * 获取免费接口数量的Redis键前缀
     */
    String USER_COUNT_KEY_PREFIX = "api:free:count:lock:";


    String USER_CHANGE_KEY_PREFIX = "api:change:lock:";


}
