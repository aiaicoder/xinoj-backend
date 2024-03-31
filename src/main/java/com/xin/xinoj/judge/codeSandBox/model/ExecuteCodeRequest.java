package com.xin.xinoj.judge.codeSandBox.model;

import com.xin.xinoj.model.dto.question.JudgeCase;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExecuteCodeRequest {
    /**
     * 判题代码
     */
    private String code;

    /**
     * 判题配置
     */
    private List<String> inputList;

    /**
     * 判题使用的语言
     */
    private String language;
}
