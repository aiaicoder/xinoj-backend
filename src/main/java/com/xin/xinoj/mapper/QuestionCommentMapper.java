package com.xin.xinoj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xin.xinoj.model.entity.QuestionComment;
import io.lettuce.core.dynamic.annotation.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author 15712
 */
public interface QuestionCommentMapper extends BaseMapper<QuestionComment> {

}
