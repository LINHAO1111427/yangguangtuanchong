package cn.iocoder.yudao.module.content.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.content.controller.admin.comment.vo.ContentCommentAuditReqVO;
import cn.iocoder.yudao.module.content.controller.admin.comment.vo.ContentCommentPageReqVO;
import cn.iocoder.yudao.module.content.controller.admin.comment.vo.ContentCommentRespVO;
import cn.iocoder.yudao.module.content.dal.dataobject.ContentCommentDO;
import cn.iocoder.yudao.module.content.dal.dataobject.ContentDO;
import cn.iocoder.yudao.module.content.dal.mysql.ContentCommentMapper;
import cn.iocoder.yudao.module.content.dal.mysql.ContentMapper;
import cn.iocoder.yudao.module.content.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

@Service
public class ContentCommentAdminService {

    @Resource
    private ContentCommentMapper commentMapper;
    @Resource
    private ContentMapper contentMapper;
    @Resource
    private MemberUserApi memberUserApi;

    public PageResult<ContentCommentRespVO> getCommentPage(ContentCommentPageReqVO reqVO) {
        LambdaQueryWrapperX<ContentCommentDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eqIfPresent(ContentCommentDO::getContentId, reqVO.getContentId())
                .eqIfPresent(ContentCommentDO::getUserId, reqVO.getUserId())
                .eqIfPresent(ContentCommentDO::getStatus, reqVO.getStatus())
                .eqIfPresent(ContentCommentDO::getAuditStatus, reqVO.getAuditStatus())
                .eq(ContentCommentDO::getDeleted, 0)
                .orderByDesc(ContentCommentDO::getCreateTime);
        if (reqVO.getCreateTime() != null && reqVO.getCreateTime().length == 2) {
            wrapper.between(ContentCommentDO::getCreateTime, reqVO.getCreateTime()[0], reqVO.getCreateTime()[1]);
        }
        PageResult<ContentCommentDO> page = commentMapper.selectPage(reqVO, wrapper);
        if (CollUtil.isEmpty(page.getList())) {
            return PageResult.empty(page.getTotal());
        }
        Map<Long, MemberUserRespDTO> userMap = loadUserMap(page.getList());
        List<ContentCommentRespVO> list = page.getList().stream()
                .map(comment -> convert(comment, userMap))
                .collect(Collectors.toList());
        return new PageResult<>(list, page.getTotal());
    }

    public void auditComment(ContentCommentAuditReqVO reqVO) {
        ContentCommentDO comment = commentMapper.selectById(reqVO.getId());
        if (comment == null || ObjectUtil.defaultIfNull(comment.getDeleted(), 0) == 1) {
            throw exception(ErrorCodeConstants.COMMENT_NOT_EXISTS);
        }
        comment.setAuditStatus(reqVO.getAuditStatus());
        comment.setAuditRemark(reqVO.getAuditRemark());
        comment.setAuditTime(LocalDateTime.now());
        comment.setUpdateTime(LocalDateTime.now());
        commentMapper.updateById(comment);
    }

    public void deleteComment(Long id) {
        ContentCommentDO comment = commentMapper.selectById(id);
        if (comment == null || ObjectUtil.defaultIfNull(comment.getDeleted(), 0) == 1) {
            throw exception(ErrorCodeConstants.COMMENT_NOT_EXISTS);
        }
        ContentDO content = contentMapper.selectById(comment.getContentId());
        if (content == null || ObjectUtil.defaultIfNull(content.getDeleted(), 0) == 1) {
            throw exception(ErrorCodeConstants.CONTENT_NOT_EXISTS);
        }

        List<ContentCommentDO> toDelete = new ArrayList<>();
        toDelete.add(comment);
        toDelete.addAll(loadDescendants(comment));

        LocalDateTime now = LocalDateTime.now();
        for (ContentCommentDO item : toDelete) {
            item.setDeleted(1);
            item.setStatus(0);
            item.setUpdateTime(now);
            commentMapper.updateById(item);
        }

        adjustContentCommentCount(comment.getContentId(), -toDelete.size());
        if (!isRootComment(comment)) {
            adjustReplyCount(comment.getRootId(), -toDelete.size());
        }
    }

    private ContentCommentRespVO convert(ContentCommentDO comment, Map<Long, MemberUserRespDTO> userMap) {
        ContentCommentRespVO vo = new ContentCommentRespVO();
        vo.setId(comment.getId());
        vo.setContentId(comment.getContentId());
        vo.setUserId(comment.getUserId());
        vo.setReplyUserId(comment.getReplyUserId());
        vo.setParentId(comment.getParentId());
        vo.setRootId(comment.getRootId());
        vo.setContent(comment.getContent());
        vo.setStatus(comment.getStatus());
        vo.setAuditStatus(comment.getAuditStatus());
        vo.setLikeCount(ObjectUtil.defaultIfNull(comment.getLikeCount(), 0));
        vo.setReplyCount(ObjectUtil.defaultIfNull(comment.getReplyCount(), 0));
        vo.setCreateTime(comment.getCreateTime());

        MemberUserRespDTO user = userMap.get(comment.getUserId());
        if (user != null) {
            vo.setUserName(user.getNickname());
        }
        if (comment.getReplyUserId() != null) {
            MemberUserRespDTO replyUser = userMap.get(comment.getReplyUserId());
            if (replyUser != null) {
                vo.setReplyUserName(replyUser.getNickname());
            }
        }
        return vo;
    }

    private Map<Long, MemberUserRespDTO> loadUserMap(List<ContentCommentDO> list) {
        Set<Long> userIds = new HashSet<>();
        for (ContentCommentDO comment : list) {
            if (comment.getUserId() != null) {
                userIds.add(comment.getUserId());
            }
            if (comment.getReplyUserId() != null) {
                userIds.add(comment.getReplyUserId());
            }
        }
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            return memberUserApi.getUserMap(new ArrayList<>(userIds));
        } catch (Exception ex) {
            return Collections.emptyMap();
        }
    }

    private List<ContentCommentDO> loadDescendants(ContentCommentDO comment) {
        List<ContentCommentDO> descendants = new ArrayList<>();
        List<Long> parents = new ArrayList<>();
        parents.add(comment.getId());
        while (!parents.isEmpty()) {
            List<ContentCommentDO> children = commentMapper.selectList(new LambdaQueryWrapperX<ContentCommentDO>()
                    .in(ContentCommentDO::getParentId, parents)
                    .eq(ContentCommentDO::getDeleted, 0));
            if (CollUtil.isEmpty(children)) {
                break;
            }
            descendants.addAll(children);
            parents = children.stream().map(ContentCommentDO::getId).collect(Collectors.toList());
        }
        return descendants;
    }

    private boolean isRootComment(ContentCommentDO comment) {
        return comment.getParentId() == null || comment.getParentId() == 0;
    }

    private void adjustReplyCount(Long commentId, int delta) {
        if (commentId == null || commentId <= 0) {
            return;
        }
        LambdaUpdateWrapper<ContentCommentDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ContentCommentDO::getId, commentId)
                .setSql("reply_count = GREATEST(COALESCE(reply_count,0) + (" + delta + "), 0)");
        commentMapper.update(null, updateWrapper);
    }

    private void adjustContentCommentCount(Long contentId, int delta) {
        if (contentId == null) {
            return;
        }
        LambdaUpdateWrapper<ContentDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ContentDO::getId, contentId)
                .setSql("comment_count = GREATEST(COALESCE(comment_count,0) + (" + delta + "), 0)");
        contentMapper.update(null, updateWrapper);
    }
}
