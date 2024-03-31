package com.xin.xinoj.judge.codeSandBox;

import com.xin.xinoj.judge.codeSandBox.model.ExecuteCodeRequest;
import com.xin.xinoj.judge.codeSandBox.model.ExecuteCodeResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 代码沙箱代理类，实现增强
 *
 * @author 15712
 */
@Slf4j
public class CodeSandBoxProxy implements CodeSandBox {


    private final CodeSandBox codeSandBox;


    public CodeSandBoxProxy(CodeSandBox codeSandbox) {
        this.codeSandBox = codeSandbox;
    }

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        log.info("准备开始判题{}", executeCodeRequest);
        ExecuteCodeResponse executeCodeResponse = codeSandBox.executeCode(executeCodeRequest);
        log.info("判题结束,{}", executeCodeRequest);
        return executeCodeResponse;
    }


}
