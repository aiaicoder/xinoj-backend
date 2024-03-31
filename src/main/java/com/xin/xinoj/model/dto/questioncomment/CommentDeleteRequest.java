package com.xin.xinoj.model.dto.questioncomment;

import com.xin.xinoj.model.entity.QuestionComment;
import lombok.Data;

import java.io.Serializable;

/**
 * @author 15712
 */
@Data
public class CommentDeleteRequest implements Serializable {

    /**
     * 当前评论
     */
    private QuestionComment currentComment;


}
