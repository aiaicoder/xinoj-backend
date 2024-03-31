package com.xin.xinoj.judge.codeSandBox.strategy;

import com.xin.xinoj.model.dto.question.JudgeCase;
import com.xin.xinoj.judge.codeSandBox.model.JudgeInfo;
import com.xin.xinoj.model.entity.Question;
import com.xin.xinoj.model.entity.QuestionSubmit;
import lombok.Data;

import java.util.List;

/**
 * @author 15712
 */
@Data
public class JudgeContext {
    private JudgeInfo judgeInfo;

    private List<String> inputList;

    private List<String> outputList;

    private List<JudgeCase> judgeCaseList;

    private Question question;

    private QuestionSubmit questionSubmit;


}
