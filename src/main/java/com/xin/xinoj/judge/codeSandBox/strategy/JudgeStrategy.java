package com.xin.xinoj.judge.codeSandBox.strategy;

import com.xin.xinoj.judge.codeSandBox.model.JudgeInfo;

public interface JudgeStrategy {
    JudgeInfo doJudge(JudgeContext judgeContext);
}
