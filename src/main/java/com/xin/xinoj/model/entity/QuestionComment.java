package com.xin.xinoj.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@ApiModel(value = "OjComment对象", description = "评论")
@TableName(value = "question_comment")
public class QuestionComment implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键id")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @ApiModelProperty(value = "问题id")
    private Long questionId;

    @ApiModelProperty(value = "用户id")
    private Long userId;

    @ApiModelProperty(value = "用户昵称")
    private String userName;

    @ApiModelProperty(value = "用户头像")
    private String userAvatar;

    @ApiModelProperty(value = "评论内容")
    private String content;

    @ApiModelProperty(value = "父级评论id")
    private Long parentId;

    @ApiModelProperty(value = "回复条数")
    private Integer  commentNum;

    @ApiModelProperty(value = "点赞数量")
    private Integer likeCount;

    @ApiModelProperty(value = "是否点赞")
    private Boolean isLike;

    @ApiModelProperty(value = "点赞id列表")
    private String likeListId;

    @ApiModelProperty(value = "是否显示输入框")
    private Boolean inputShow;

    @ApiModelProperty(value = "被回复的记录id")
    private Long fromId;

    @ApiModelProperty(value = "回复人名称")
    private String fromName;

    @ApiModelProperty(value = "更新时间")
    private Date gmtModified;

    @ApiModelProperty(value = "创建时间")
    private Date gmtCreate;

    @TableLogic
    @ApiModelProperty(value = "逻辑删除 1（true）已删除， 0（false）未删除")
    private Boolean isDeleted;


}
