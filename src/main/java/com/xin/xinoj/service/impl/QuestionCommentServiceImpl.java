package com.xin.xinoj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xin.xinoj.common.ErrorCode;
import com.xin.xinoj.exception.BusinessException;
import com.xin.xinoj.mapper.QuestionCommentMapper;
import com.xin.xinoj.model.entity.QuestionComment;
import com.xin.xinoj.model.entity.User;
import com.xin.xinoj.model.vo.QuestionCommentVO;
import com.xin.xinoj.service.QuestionCommentService;
import com.xin.xinoj.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 帖子收藏服务实现
 *
 * @author <a href="https://github.com/liyupi">程序员小新</a>
 */
@Service
public class QuestionCommentServiceImpl extends ServiceImpl<QuestionCommentMapper, QuestionComment> implements QuestionCommentService {

    @Resource
    private UserService userService;

    @Resource
    private QuestionCommentMapper questionCommentMapper;


    @Override
    public List<QuestionCommentVO> getAllCommentList(long questionId) {
        QueryWrapper<QuestionComment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("questionId", questionId);
        queryWrapper.eq("parentId", -1);
        List<QuestionComment> questionCommentList = this.list(queryWrapper);
        List<QuestionCommentVO> questionCommentVos = new ArrayList<>();
        questionCommentList.forEach(questionComment -> {
            QuestionCommentVO questionCommentVO = QuestionCommentVO.objToVo(questionComment);
            questionCommentVos.add(questionCommentVO);
        });
        questionCommentVos.forEach(questionCommentVO -> {
            //查询子评论
            QueryWrapper<QuestionComment> childCommentQueryWrapper = new QueryWrapper<>();
            childCommentQueryWrapper.eq("parentId", questionCommentVO.getId());
            List<QuestionComment> childCommentList = this.list(childCommentQueryWrapper);
            List<QuestionCommentVO> childCommentVos = new ArrayList<>();
            childCommentList.forEach(childComment -> {
                QuestionCommentVO childCommentVO = QuestionCommentVO.objToVo(childComment);
                childCommentVos.add(childCommentVO);
            });
            questionCommentVO.setReply(childCommentVos);
        });
        return questionCommentVos;
    }

    @Override
    @Transactional
    public int deleteCommentById(QuestionComment questionComment, User loginUser) {
        //只有自己的评论和管理员才可以删除
        if (!questionComment.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "你无权删除该评论");
        }
        int deleteCount = 1;
        try {
            //先查询该评论是不是顶级评论，有可能有回复有可能没有回复
            QueryWrapper<QuestionComment> isParentWrapper = new QueryWrapper<>();
            isParentWrapper.eq("id", questionComment.getId());
            isParentWrapper.eq("parentId", -1);

            long count = this.count(isParentWrapper);
            // 如果count大于0说明该评论是一条顶级评论，先删除他的子级评论
            if (count > 0) {
                //先删除子评论
                QueryWrapper<QuestionComment> wrapper = new QueryWrapper<>();
                wrapper.eq("parentId", questionComment.getId());
                //删除子评论
                questionCommentMapper.delete(wrapper);
            }
            //最后删除父级评论
            QueryWrapper<QuestionComment> wrapper = new QueryWrapper<>();
            wrapper.eq("userId", questionComment.getUserId());
            wrapper.eq("id", questionComment.getId());
            questionCommentMapper.delete(wrapper);

            // 找到该记录的顶级评论让他的评论数-1,因为有可能他的还有父级
            Long parentId = questionComment.getParentId();
            Long fromId = questionComment.getFromId();
            if (parentId != null && parentId.equals(fromId)) {
                QuestionComment parentquestionComment = this.getById(parentId);
                if (parentquestionComment != null) {
                    parentquestionComment.setCommentNum(parentquestionComment.getCommentNum() - 1);
                    this.updateLikeCount(parentquestionComment);
                }
            }

            // 考虑到不是顶级记录的直接子记录的情况 fromId:表示该记录回复的是那一条记录
            if (parentId != null && parentId.equals(fromId)) {
                // 更新他的直接父级
                QuestionComment father = this.getById(fromId);
                if (father != null) {
                    father.setCommentNum(father.getCommentNum() - 1);
                    this.updateLikeCount(father);
                }
                // 更新他的跟节点评论数量
                QuestionComment root = this.getById(parentId);
                if (root != null) {
                    root.setCommentNum(root.getCommentNum() - 1);
                    this.updateLikeCount(root);
                }
            }
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除评论失败");
        }
        return deleteCount;
    }

    @Override
    public boolean addComment(QuestionComment current, QuestionComment parent, User loginUser) {
        Long userId = loginUser.getId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        if (current == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "评论内容不能为空");
        }
        Long parentId = current.getParentId();
        // 是一条顶级评论，直接进行添加操作 如果他的parentId=-1那就是顶级评论[发表评论]
        if (current.getId() != null && parentId == -1) {
            // 如果从token中解析出来的memberId等于提交数据中MemberId就评论成功，否则失败
            if (userId.equals(current.getUserId())) {
                return this.save(current);
            }
        }
        // 如果能直接到这里，说明是一条回复评论
        if (parent != null && (parent.getId() != null && parent.getParentId() != null)) {
            // 修改当前被回复的记录的总回复条数+1 [前端传递过来的时候已经+1，直接按照id修改即可]
            this.updateLikeCount(parent);
            // 根据parentId查询一条记录
            QuestionComment root = this.getById(parent.getParentId());
            if (root != null && root.getParentId() == -1) {
                // 根据当前被回复的记录找到顶级记录，将顶级记录也+1
                root.setCommentNum(root.getCommentNum() + 1);
                this.updateLikeCount(root);
            }
        }
        // 如果userId不等于提交数据中的userId就评论失败，否则成功
        if (userId.equals(current.getUserId())) {
            return this.save(current);
        } else {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "评论失败");
        }
    }

    @Override
    public boolean updateLikeCount(QuestionComment questionComment) {
        return this.updateById(questionComment);
    }
}




