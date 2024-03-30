package com.xin.xinoj.model.dto.questionsubmit;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.xin.xinoj.model.dto.question.JudgeCase;
import com.xin.xinoj.model.dto.question.JudgeConfig;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author 15712
 */
@Data
public class QuestionSubmitAddRequest {
    /**
     * 编程语言
     */
    private String language;

    /**
     * 用户代码
     */
    private String code;

    /**
     * 题目 id
     */
    private Long questionId;

    /**
     * 是否允许可见
     */
    private Integer isVisible;


    private static final long serialVersionUID = 1L;
}
