package com.xin.xinoj.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xin.xinoj.RabbitMq.MyMessageProducer;
import com.xin.xinoj.common.ErrorCode;
import com.xin.xinoj.constant.CommonConstant;
import com.xin.xinoj.constant.MqConstant;
import com.xin.xinoj.exception.BusinessException;
import com.xin.xinoj.judge.JudgeService;
import com.xin.xinoj.mapper.QuestionSubmitMapper;
import com.xin.xinoj.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.xin.xinoj.model.dto.questionsubmit.QuestionSubmitQueryRequest;
import com.xin.xinoj.model.entity.Question;
import com.xin.xinoj.model.entity.QuestionSubmit;
import com.xin.xinoj.model.entity.User;
import com.xin.xinoj.model.enums.QuestionSubmitLanguageEnum;
import com.xin.xinoj.model.enums.QuestionSubmitStatusEnum;
import com.xin.xinoj.model.enums.QuestionSubmitVisibleEnum;
import com.xin.xinoj.model.vo.QuestionSubmitVO;
import com.xin.xinoj.model.vo.QuestionVO;
import com.xin.xinoj.service.QuestionService;
import com.xin.xinoj.service.QuestionSubmitService;
import com.xin.xinoj.utils.SqlUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 15712
 * @description 针对表【question_submit(题目提交)】的数据库操作Service实现
 * @createDate 2024-03-25 16:24:20
 */
@Service
public class QuestionSubmitServiceImpl extends ServiceImpl<QuestionSubmitMapper, QuestionSubmit>
        implements QuestionSubmitService {

    @Resource
    private QuestionService questionService;

    @Resource
    private UserServiceImpl userService;

//    @Resource
//    @Lazy
//    private JudgeService judgeService;


    @Resource
    private MyMessageProducer myMessageProducer;


    /**
     * 点赞
     *
     * @param questionSubmitAddRequest
     * @param loginUser
     * @return
     */
    @Override
    public Long doQuestionSubmit(QuestionSubmitAddRequest questionSubmitAddRequest, User loginUser) {
        // 校验编程语言是否合法
        String language = questionSubmitAddRequest.getLanguage();
        QuestionSubmitLanguageEnum languageEnum = QuestionSubmitLanguageEnum.getEnumByValue(language);
        if (languageEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "暂不支持该编程语言");
        }
        long questionId = questionSubmitAddRequest.getQuestionId();
        // 判断实体是否存在，根据类别获取实体
        Question question = questionService.getById(questionId);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 是否已提交题目
        long userId = loginUser.getId();
        // 每个用户串行提交题目
        QuestionSubmit questionSubmit = new QuestionSubmit();
        questionSubmit.setUserId(userId);
        questionSubmit.setQuestionId(questionId);
        questionSubmit.setCode(questionSubmitAddRequest.getCode());
        questionSubmit.setLanguage(language);
        // 设置初始状态
        questionSubmit.setStatus(QuestionSubmitStatusEnum.WAITING.getValue());
        questionSubmit.setJudgeInfo("{}");
        boolean save = this.save(questionSubmit);
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "数据插入失败");
        }
        // 设置问题的提交数
        Integer submitNum = question.getSubmitNum();
        Question updateQuestion = new Question();
        synchronized (question.getSubmitNum()) {
            submitNum = submitNum + 1;
            updateQuestion.setId(questionId);
            updateQuestion.setSubmitNum(submitNum);
            save = questionService.updateById(updateQuestion);
            if (!save) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "数据保存失败");
            }
        }
        Long questionSubmitId = questionSubmit.getId();
        // 异步调用判题服务
        myMessageProducer.sendMessage(MqConstant.DIRECT_EXCHANGE, "oj", String.valueOf(questionSubmitId));
        return questionSubmitId;
    }


    /**
     * 获取查询包装类（用户根据哪些字段查询，根据前端传来的请求对象，得到 mybatis 框架支持的查询 QueryWrapper 类）
     *
     * @param questionQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<QuestionSubmit> getQueryWrapper(QuestionSubmitQueryRequest questionQueryRequest) {
        QueryWrapper<QuestionSubmit> queryWrapper = new QueryWrapper<>();
        if (questionQueryRequest == null) {
            return queryWrapper;
        }
        String language = questionQueryRequest.getLanguage();
        Integer status = questionQueryRequest.getStatus();
        Long questionId = questionQueryRequest.getQuestionId();
        Long userId = questionQueryRequest.getUserId();
        Integer visible = questionQueryRequest.getVisible();
        String sortField = questionQueryRequest.getSortField();
        String sortOrder = questionQueryRequest.getSortOrder();
        queryWrapper.eq(ObjectUtils.isNotEmpty(language), "language", language);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionId), "questionId", questionId);
        queryWrapper.eq(QuestionSubmitVisibleEnum.getEnumByValue(visible) != null, "visible", visible);
        queryWrapper.eq(QuestionSubmitStatusEnum.getEnumByValue(status) != null, "status", status);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }


    @Override
    public QuestionSubmitVO getQuestionSubmitVO(QuestionSubmit questionSubmit, User loginUser) {
        QuestionSubmitVO questionSubmitVo = QuestionSubmitVO.objToVo(questionSubmit);
        Question question = questionService.getById(questionSubmit.getQuestionId());
        questionSubmitVo.setQuestionVO(QuestionVO.objToVo(question));
        questionSubmitVo.setUserVO(userService.getUserVO(loginUser));
        if (!questionSubmitVo.getUserId().equals(loginUser.getId()) && userService.isAdmin(loginUser)) {
            questionSubmitVo.setCode(null);
        }
        return questionSubmitVo;
    }


    @Override
    public Page<QuestionSubmitVO> getQuestionSubmitVOPage(Page<QuestionSubmit> questionSubmitPage, User loginUser) {
        List<QuestionSubmit> questionList = questionSubmitPage.getRecords();
        Page<QuestionSubmitVO> questionVoPage = new Page<>(questionSubmitPage.getCurrent(), questionSubmitPage.getSize(), questionSubmitPage.getTotal());
        if (CollUtil.isEmpty(questionList)) {
            return questionVoPage;
        }
        // 填充信息
        List<QuestionSubmitVO> questionVOList = questionList.stream().map(question -> {
            return this.getQuestionSubmitVO(question, loginUser);
        }).collect(Collectors.toList());
        questionVoPage.setRecords(questionVOList);
        return questionVoPage;
    }

    @Override
    public List<QuestionSubmitVO> getQuestionSubmitVOList(List<QuestionSubmit> questionSubmitList, User loginUser) {
        ArrayList<QuestionSubmitVO> questionSubmitListVo = new ArrayList<>();
        questionSubmitList.forEach(questionSubmit -> {
            QuestionSubmitVO questionSubmitVO = getQuestionSubmitVO(questionSubmit, loginUser);
            questionSubmitListVo.add(questionSubmitVO);
        });
        return questionSubmitListVo;
    }

}




