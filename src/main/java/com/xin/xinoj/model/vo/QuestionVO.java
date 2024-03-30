package com.xin.xinoj.model.vo;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.xin.xinoj.model.dto.question.JudgeCase;
import com.xin.xinoj.model.dto.question.JudgeConfig;
import com.xin.xinoj.model.entity.Question;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.Date;
import java.util.List;

/**
 * @author 15712
 */
@Data
public class QuestionVO {
    /**
     * id
     */
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 标签列表
     */
    private List<String> tags;

    /**
     * 题目提交数
     */
    private Integer submitNum;

    /**
     * 题目通过数
     */
    private Integer acceptedNum;

    /**
     * 判题配置（json 对象）
     */
    private JudgeConfig judgeConfig;

    /**
     * 判题用例（json 数组）
     */
    private List<JudgeCase> judgeCase;

    /**
     * 点赞数
     */
    private Integer thumbNum;

    /**
     * 收藏数
     */
    private Integer favourNum;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建题目人的信息
     */
    private UserVO userVO;

    /**
     * 题目答案
     */
    private String answer;


    /**
     * 包装类转对象
     *
     * @param questionVO
     * @return
     */
    public static Question voToObj(QuestionVO questionVO) {
        if (questionVO == null) {
            return null;
        }
        Question question = new Question();
        BeanUtils.copyProperties(questionVO, question);
        List<String> tagList = questionVO.getTags();
        if (tagList != null) {
            question.setTags(JSONUtil.toJsonStr(tagList));
        }
        JudgeConfig judgeConfig = questionVO.getJudgeConfig();
        if (judgeConfig != null) {
            question.setJudgeConfig(JSONUtil.toJsonStr(judgeConfig));
        }
        return question;
    }

    /**
     * 对象转包装类
     *
     * @param question
     * @return
     */
    public static QuestionVO objToVo(Question question) {
        if (question == null) {
            return null;
        }
        QuestionVO questionVO = new QuestionVO();
        BeanUtils.copyProperties(question, questionVO);
        String tags = question.getTags();
        String judgeConfig = question.getJudgeConfig();
        String judgeCase = question.getJudgeCase();
        if (StrUtil.isNotBlank(tags)) {
            questionVO.setTags(JSONUtil.toList(tags, String.class));
        }
        if (StrUtil.isNotBlank(judgeConfig)) {
            questionVO.setJudgeConfig(JSONUtil.toBean(question.getJudgeConfig(), JudgeConfig.class));
        }
        if (StrUtil.isNotBlank(judgeCase)) {
            questionVO.setJudgeCase(JSONUtil.toList(question.getJudgeCase(), JudgeCase.class));
        }
        return questionVO;
    }

}
