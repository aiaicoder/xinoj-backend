package com.xin.xinoj.model.vo;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.xin.xinoj.model.entity.QuestionComment;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author 15712
 */
@Data
public class QuestionCommentVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long questionId;

    private Long userId;

    private String userName;

    private String userAvatar;

    private String content;

    private Long parentId;

    private Integer commentNum;

    private Integer likeCount;

    private Boolean isLike;

    private List<String> likeListId;

    private Boolean inputShow;

    private Long fromId;

    private String fromName;

    private Date gmtModified;

    private Date gmtCreate;

    private List<QuestionCommentVO> reply;

    /**
     * 包装类转对象
     *
     * @param questionCommentVO
     * @return
     */
    public static QuestionComment voToObj(QuestionCommentVO questionCommentVO) {
        if (questionCommentVO == null) {
            return null;
        }
        QuestionComment questionComment = new QuestionComment();
        BeanUtils.copyProperties(questionCommentVO, questionComment);
        List<String> likeListId = questionCommentVO.getLikeListId();
        if (likeListId != null) {
            String likeListIdStr = JSONUtil.toJsonStr(likeListId);
            questionComment.setLikeListId(likeListIdStr);
        }

        return questionComment;
    }

    /**
     * 对象转包装类
     *
     * @param questionComment
     * @return
     */
    public static QuestionCommentVO objToVo(QuestionComment questionComment) {
        if (questionComment == null) {
            return null;
        }
        QuestionCommentVO questionSubmitVO = new QuestionCommentVO();
        BeanUtils.copyProperties(questionComment, questionSubmitVO);
        String judgeInfoStr = questionComment.getLikeListId();
        if (StrUtil.isNotBlank(judgeInfoStr)) {
            questionSubmitVO.setLikeListId(JSONUtil.toList(judgeInfoStr, String.class));
        }
        return questionSubmitVO;
    }
}

