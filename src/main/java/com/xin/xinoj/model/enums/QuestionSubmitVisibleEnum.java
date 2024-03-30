package com.xin.xinoj.model.enums;


import cn.hutool.core.util.ObjectUtil;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 15712
 */

public enum QuestionSubmitVisibleEnum {

    // 0 自己可见，1 仅自己和好友可见，2 完全公开
    WAITING("仅自己可见", 0),
    RUNNING("好友可见", 1),
    SUCCESS("公开", 2);

    private final String text;

    private final Integer value;

    QuestionSubmitVisibleEnum(String text, Integer value) {
        this.text = text;
        this.value = value;
    }


    /**
     * 获取值列表
     *
     * @return
     */
    public static List<Integer> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value
     * @return
     */
    public static QuestionSubmitVisibleEnum getEnumByValue(Integer value) {
        if (ObjectUtil.isEmpty( value)) {
            return null;
        }
        for (QuestionSubmitVisibleEnum anEnum : QuestionSubmitVisibleEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }

    public Integer getValue() {
        return value;
    }

    public String getText() {
        return text;
    }
}
