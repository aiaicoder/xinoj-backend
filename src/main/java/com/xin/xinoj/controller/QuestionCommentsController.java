package com.xin.xinoj.controller;

import com.xin.xinoj.annotation.AuthCheck;
import com.xin.xinoj.common.BaseResponse;
import com.xin.xinoj.common.ErrorCode;
import com.xin.xinoj.common.ResultUtils;
import com.xin.xinoj.constant.UserConstant;
import com.xin.xinoj.exception.BusinessException;
import com.xin.xinoj.model.dto.questioncomment.CommentAddRequest;
import com.xin.xinoj.model.dto.questioncomment.CommentDeleteRequest;
import com.xin.xinoj.model.dto.questioncomment.CommentUpdateRequest;
import com.xin.xinoj.model.entity.QuestionComment;
import com.xin.xinoj.model.entity.User;
import com.xin.xinoj.model.vo.QuestionCommentVO;
import com.xin.xinoj.service.QuestionCommentService;
import com.xin.xinoj.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 帖子接口
 *
 * @author <a href="https://github.com/liyupi">程序员小新</a>
 */
@RestController
@RequestMapping("/question_comment")
@Slf4j
public class QuestionCommentsController {


    @Resource
    private UserService userService;

    @Resource
    private QuestionCommentService questionCommentService;

    // region 增删改查

    /**
     * 获取该问题的所有评论
     *
     * @param id
     * @return
     */
    @GetMapping("/getCommentList")
    public BaseResponse<List<QuestionCommentVO>> getCommentList(long id) {
        return ResultUtils.success(questionCommentService.getAllCommentList(id));
    }


    @PostMapping("/addComment")
    public BaseResponse<Boolean> addQuestionComment(@RequestBody QuestionComment currentComment, @RequestBody(required = false) QuestionComment parent) {
        User loginUser = userService.getLoginUser();
        boolean b = questionCommentService.addComment(currentComment, parent, loginUser);
        return ResultUtils.success(b);
    }

    @PostMapping("wrap/addComment")
    public BaseResponse<Boolean> addQuestionCommentWrap(@RequestBody CommentAddRequest commentAddRequest) {
        User loginUser = userService.getLoginUser();
        QuestionComment currentComment = commentAddRequest.getCurrentComment();
        QuestionComment parent = commentAddRequest.getParentComment();
        boolean b = questionCommentService.addComment(currentComment, parent, loginUser);
        return ResultUtils.success(b);
    }


    /**
     * 删除
     *
     * @param currentComment
     * @return
     */
    @PostMapping("/deleteComment")
    public BaseResponse<Integer> deleteQuestion(@RequestBody QuestionComment currentComment) {
        if (currentComment == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不能删除空评论");
        }
        User loginUser = userService.getLoginUser();
        int updateCount = questionCommentService.deleteCommentById(currentComment, loginUser);
        return ResultUtils.success(updateCount);
    }

    /**
     * 更新（仅管理员）
     *
     * @param currentComment
     * @return
     */
    @PostMapping("/updateLikeCount")
    public BaseResponse<Boolean> updateQuestionComment(@RequestBody QuestionComment currentComment) {
        boolean updateLikeCount = questionCommentService.updateLikeCount(currentComment);
        return ResultUtils.success(updateLikeCount);
    }


}
