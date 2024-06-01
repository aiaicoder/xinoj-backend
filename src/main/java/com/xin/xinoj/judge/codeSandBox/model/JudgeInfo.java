package com.xin.xinoj.judge.codeSandBox.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class JudgeInfo {
    /**
     * 题目执行信息
     */
    private String message;
    /**
     * 题目消耗内存（kb）
     */
    private Long memory;

    /**
     * 题目消耗时间（ms）
     */
    private Long time;

    /**
     * 通过的案例数
     */
    private Integer passNum;

    /**
     * 总案例数
     */
    private Integer totalNum;
}
