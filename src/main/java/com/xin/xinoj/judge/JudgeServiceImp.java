package com.xin.xinoj.judge;

import com.xin.xinoj.judge.codeSandBox.strategy.JudgeContext;
import com.xin.xinoj.judge.codeSandBox.strategy.JudgeManager;
import com.xin.xinoj.judge.codeSandBox.model.JudgeInfo;

import java.util.List;
import java.util.stream.Collectors;

import cn.hutool.json.JSONUtil;
import com.xin.xinoj.common.ErrorCode;
import com.xin.xinoj.exception.BusinessException;
import com.xin.xinoj.judge.codeSandBox.CodeSandBox;
import com.xin.xinoj.judge.codeSandBox.CodeSandBoxFactory;
import com.xin.xinoj.judge.codeSandBox.CodeSandBoxProxy;
import com.xin.xinoj.judge.codeSandBox.model.ExecuteCodeRequest;
import com.xin.xinoj.judge.codeSandBox.model.ExecuteCodeResponse;
import com.xin.xinoj.model.dto.question.JudgeCase;
import com.xin.xinoj.model.entity.Question;
import com.xin.xinoj.model.entity.QuestionSubmit;
import com.xin.xinoj.model.enums.QuestionSubmitStatusEnum;
import com.xin.xinoj.service.QuestionService;
import com.xin.xinoj.service.QuestionSubmitService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;


/**
 * 判题服务实现
 *
 * @author 15712
 */
@Service
public class JudgeServiceImp implements JudgeService {
    @Value("${codeSandBox.type:example}")
    private String type;
    @Resource
    private QuestionService questionService;

    @Resource
    private QuestionSubmitService questionSubmitService;

    @Resource
    private JudgeManager judgeManager;


    @Override
    public QuestionSubmit doJudge(long questionSubmitId) {
        //先获取题目提交信息
        QuestionSubmit questionSubmit = questionSubmitService.getById(questionSubmitId);
        if (questionSubmit == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "提交信息不存在");
        }
        //根据题目提交信息获取到题目id，获取题目信息
        Long questionId = questionSubmit.getQuestionId();
        Question question = questionService.getById(questionId);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "题目不存在");
        }
        // 2）如果题目提交状态不为等待中，就不用重复执行了
        if (!questionSubmit.getStatus().equals(QuestionSubmitStatusEnum.WAITING.getValue())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "题目正在判题中");
        }
        //修改题目的判题状态
        QuestionSubmit questionSubmitUpdate = new QuestionSubmit();
        questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.RUNNING.getValue());
        questionSubmitUpdate.setId(questionSubmitId);
        questionSubmitUpdate.setQuestionId(questionSubmitId);
        boolean update = questionSubmitService.updateById(questionSubmitUpdate);
        if (!update) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "题目状态更新失败");
        }
        //拿到题目提交的所有信息
        String language = questionSubmit.getLanguage();
        String code = questionSubmit.getCode();
        CodeSandBox codeSandbox = CodeSandBoxFactory.newInstance(type);
        codeSandbox = new CodeSandBoxProxy(codeSandbox);
        // 获取输入用例
        String judgeCaseStr = question.getJudgeCase();
        List<JudgeCase> judgeCaseList = JSONUtil.toList(judgeCaseStr, JudgeCase.class);
        List<String> inputList = judgeCaseList.stream().map(JudgeCase::getInput).collect(Collectors.toList());
        ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder()
                .code(code)
                .language(language)
                .inputList(inputList)
                .build();
        ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);
        List<String> outputList = executeCodeResponse.getOutputList();
        JudgeContext judgeContext = new JudgeContext();
        judgeContext.setJudgeInfo(executeCodeResponse.getJudgeInfo());
        judgeContext.setInputList(inputList);
        judgeContext.setOutputList(outputList);
        judgeContext.setJudgeCaseList(judgeCaseList);
        judgeContext.setQuestion(question);
        judgeContext.setQuestionSubmit(questionSubmit);
        JudgeInfo judgeInfo = judgeManager.doJudge(judgeContext);
        //更新题目状态
        questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.SUCCESS.getValue());
        questionSubmitUpdate.setId(questionSubmitId);
        questionSubmitUpdate.setQuestionId(questionSubmitId);
        questionSubmitUpdate.setJudgeInfo(JSONUtil.toJsonStr(judgeInfo));
        update = questionSubmitService.updateById(questionSubmitUpdate);
        if (!update) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "题目状态更新失败");
        }
        return questionSubmit;

    }
}
