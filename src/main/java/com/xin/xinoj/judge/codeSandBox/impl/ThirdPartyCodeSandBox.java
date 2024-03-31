package com.xin.xinoj.judge.codeSandBox.impl;

import com.xin.xinoj.judge.codeSandBox.CodeSandBox;
import com.xin.xinoj.judge.codeSandBox.model.ExecuteCodeRequest;
import com.xin.xinoj.judge.codeSandBox.model.ExecuteCodeResponse;

/**
 * @author 15712
 * 调用第三方的判题服务
 */
public class ThirdPartyCodeSandBox implements CodeSandBox {
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        System.out.println("第三方调用");
        return null;
    }
}
