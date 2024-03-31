package com.xin.xinoj.judge;

import com.xin.xinoj.model.entity.QuestionSubmit;

/**判题服务
 * @author 15712
 */
public interface JudgeService {
    QuestionSubmit doJudge(long questionSubmitId);
}
