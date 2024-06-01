package com.xin.xinoj.judge.codeSandBox.impl;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.excel.util.StringUtils;
import com.xin.xinoj.common.ErrorCode;
import com.xin.xinoj.exception.BusinessException;
import com.xin.xinoj.judge.codeSandBox.CodeSandBox;
import com.xin.xinoj.judge.codeSandBox.model.ExecuteCodeRequest;
import com.xin.xinoj.judge.codeSandBox.model.ExecuteCodeResponse;

import static com.xin.xinoj.constant.AuthConstant.AUTHREQUESTHEADER;
import static com.xin.xinoj.constant.AuthConstant.AUTHREQUESTSECRET;

/**
 * @author 15712
 * 远程调用代码接口
 */
public class RemoteCodeSandBox implements CodeSandBox {
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        System.out.println("远程代码沙箱");
        String url = "http://localhost:8123/executeCode";
        String json = JSONUtil.toJsonStr(executeCodeRequest);
        String responseStr = HttpUtil.createPost(url)
                .header(AUTHREQUESTHEADER , AUTHREQUESTSECRET)
                .body(json)
                .execute()
                .body();
        if (StringUtils.isBlank(responseStr)) {
            throw new BusinessException(ErrorCode.API_REQUEST_ERROR, "executeCode remoteSandbox error, message = " + responseStr);
        }
        return JSONUtil.toBean(responseStr, ExecuteCodeResponse.class);
    }

}
