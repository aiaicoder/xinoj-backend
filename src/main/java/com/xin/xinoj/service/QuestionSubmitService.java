package com.xin.xinoj.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xin.xinoj.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.xin.xinoj.model.dto.questionsubmit.QuestionSubmitQueryRequest;
import com.xin.xinoj.model.entity.QuestionSubmit;
import com.xin.xinoj.model.entity.User;
import com.xin.xinoj.model.vo.QuestionSubmitVO;

import java.util.List;

/**
* @author 15712
* @description 针对表【question_submit(题目提交)】的数据库操作Service
* @createDate 2024-03-25 16:24:20
*/
public interface QuestionSubmitService extends IService<QuestionSubmit> {

    /**
     * 提交
     *
     * @param questionSubmitAddRequest
     * @param loginUser
     * @return
     */
    Long doQuestionSubmit(QuestionSubmitAddRequest questionSubmitAddRequest, User loginUser);


    /**
     * 获取查询条件
     *
     * @param questionSubmitQueryRequest
     * @return
     */
    QueryWrapper<QuestionSubmit> getQueryWrapper(QuestionSubmitQueryRequest questionSubmitQueryRequest);

    /**
     * 获取题目封装
     *
     * @param questionSubmit
     * @param loginUser
     * @return
     */
    QuestionSubmitVO getQuestionSubmitVO(QuestionSubmit questionSubmit, User loginUser);

    /**
     * 分页获取题目封装
     *
     * @param questionSubmitPage
     * @param loginUser
     * @return
     */
    Page<QuestionSubmitVO> getQuestionSubmitVOPage(Page<QuestionSubmit> questionSubmitPage, User loginUser);

    /**
     * 获取提交列表封装
     * @param questionSubmitList
     * @param loginUser
     * @return
     */
    List<QuestionSubmitVO> getQuestionSubmitVOList(List<QuestionSubmit> questionSubmitList, User loginUser);
}
