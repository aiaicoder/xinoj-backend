package com.xin.xinoj.judge.codeSandBox.impl;

import java.util.List;

import com.xin.xinoj.judge.codeSandBox.CodeSandBox;
import com.xin.xinoj.judge.codeSandBox.model.ExecuteCodeRequest;
import com.xin.xinoj.judge.codeSandBox.model.ExecuteCodeResponse;
import com.xin.xinoj.judge.codeSandBox.model.JudgeInfo;

/**
 * @author 15712
 * 代码测试
 */
public class ExampleCodeSandBox implements CodeSandBox {
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        String code = executeCodeRequest.getCode();
        List<String> inputList = executeCodeRequest.getInputList();
        String language = executeCodeRequest.getLanguage();
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setMessage("测试成功");
        judgeInfo.setMemory(1000L);
        judgeInfo.setTime(1000L);

        return ExecuteCodeResponse.builder().
                outputList(inputList).
                judgeInfo(judgeInfo).
                status(0).
                message("测试成功").
                build();
    }
}
