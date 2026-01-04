package cn.iocoder.yudao.module.content.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.content.controller.app.vo.CommentCreateReqVO;
import cn.iocoder.yudao.module.content.controller.app.vo.CommentPageReqVO;
import cn.iocoder.yudao.module.content.controller.app.vo.CommentRespVO;
import cn.iocoder.yudao.module.content.dal.dataobject.ContentCommentDO;
import cn.iocoder.yudao.module.content.dal.dataobject.ContentDO;
import cn.iocoder.yudao.module.content.dal.mysql.ContentCommentMapper;
import cn.iocoder.yudao.module.content.dal.mysql.ContentMapper;
import cn.iocoder.yudao.module.content.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.content.framework.kafka.producer.ContentKafkaProducer;
import cn.iocoder.yudao.module.content.service.support.LocalCommentLikeStorage;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

@Service
public class CommentServiceImpl implements CommentService {

    private static final Logger log = LoggerFactory.getLogger(CommentServiceImpl.class);
    private static final int COMMENT_STATUS_NORMAL = 1;
    private static final int AUDIT_STATUS_APPROVED = 1;
    private static final int DEFAULT_REPLY_PREVIEW = 2;
    private static final int MAX_REPLY_PREVIEW = 5;

    @Resource
    private ContentCommentMapper commentMapper;
    @Resource
    private ContentMapper contentMapper;
    @Resource
    private MemberUserApi memberUserApi;
    @Resource
    private LocalCommentLikeStorage localCommentLikeStorage;
    @Resource
    private ContentKafkaProducer contentKafkaProducer;

    @Override
    public PageResult<CommentRespVO> getCommentPage(CommentPageReqVO pageReqVO, Long currentUserId) {
        if (Boolean.TRUE.equals(pageReqVO.getMine()) && currentUserId == null) {
            return PageResult.empty();
        }
        LambdaQueryWrapperX<ContentCommentDO> wrapper = buildQueryWrapper(pageReqVO, currentUserId);
        if (wrapper == null) {
            return PageResult.empty();
        }
        PageResult<ContentCommentDO> page = commentMapper.selectPage(pageReqVO, wrapper);
        if (CollUtil.isEmpty(page.getList())) {
            return PageResult.empty(page.getTotal());
        }

        Long contentAuthorId = resolveContentAuthor(pageReqVO);
        Map<Long, List<ContentCommentDO>> replyPreview = loadReplyPreview(pageReqVO, page.getList());

        Set<Long> userIds = collectUserIds(page.getList(), replyPreview.values());
        Map<Long, MemberUserRespDTO> userMap = loadUserMap(userIds);

        List<CommentRespVO> data = page.getList().stream()
                .map(comment -> convert(comment,
                        replyPreview.getOrDefault(comment.getId(), Collections.emptyList()),
                        userMap, contentAuthorId, currentUserId))
                .collect(Collectors.toList());
        return new PageResult<>(data, page.getTotal());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommentRespVO createComment(CommentCreateReqVO createReqVO, Long userId, String ipAddress, String userAgent) {
        if (userId == null) {
            throw exception(ErrorCodeConstants.COMMENT_ACCESS_DENIED);
        }
        ContentDO content = validateContent(createReqVO.getContentId());
        ensureCommentAllowed(content);

        ContentCommentDO comment = new ContentCommentDO();
        comment.setContentId(content.getId());
        comment.setUserId(userId);
        comment.setContent(StrUtil.trim(createReqVO.getContent()));
        comment.setImages(normalizeImages(createReqVO.getImages()));
        comment.setIsAnonymous(Boolean.TRUE.equals(createReqVO.getAnonymous()) ? 1 : 0);
        comment.setStatus(COMMENT_STATUS_NORMAL);
        comment.setAuditStatus(AUDIT_STATUS_APPROVED);
        comment.setIpAddress(StrUtil.blankToDefault(ipAddress, ""));
        comment.setUserAgent(StrUtil.blankToDefault(userAgent, ""));

        ContentCommentDO parent = null;
        Long parentId = createReqVO.getParentId();
        if (parentId != null && parentId > 0) {
            parent = validateParentComment(parentId, content.getId());
            comment.setParentId(parent.getId());
            comment.setRootId(resolveRootId(parent));
            comment.setReplyUserId(parent.getUserId());
        } else {
            comment.setParentId(0L);
            comment.setRootId(0L);
        }
        if (createReqVO.getReplyUserId() != null) {
            comment.setReplyUserId(createReqVO.getReplyUserId());
        }

        commentMapper.insert(comment);

        if (comment.getRootId() == null || comment.getRootId() <= 0) {
            comment.setRootId(comment.getId());
            commentMapper.updateById(comment);
        } else if (parent != null) {
            adjustReplyCount(comment.getRootId(), 1);
        }

        adjustContentCommentCount(content.getId(), 1);

        // 通知：评论/回复
        Long notifyTargetUserId = parent != null ? parent.getUserId() : content.getUserId();
        if (notifyTargetUserId != null && !Objects.equals(notifyTargetUserId, userId)) {
            Map<String, Object> event = new HashMap<>();
            event.put("behaviorType", "comment");
            event.put("action", "add");
            event.put("actorUserId", userId);
            event.put("targetUserId", notifyTargetUserId);
            event.put("contentId", content.getId());
            event.put("commentId", comment.getId());
            event.put("parentCommentId", parent != null ? parent.getId() : null);
            event.put("commentText", comment.getContent());
            event.put("eventTime", LocalDateTime.now().toString());
            contentKafkaProducer.sendBehaviorEvent(event);
        }

        Map<Long, MemberUserRespDTO> userMap = loadUserMap(Collections.singleton(userId));
        return convert(comment, Collections.emptyList(), userMap, content.getUserId(), userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long commentId, Long operatorUserId) {
        ContentCommentDO comment = commentMapper.selectById(commentId);
        if (comment == null || Boolean.TRUE.equals(comment.getDeleted())) {
            throw exception(ErrorCodeConstants.COMMENT_NOT_EXISTS);
        }
        ContentDO content = contentMapper.selectById(comment.getContentId());
        if (content == null || Boolean.TRUE.equals(content.getDeleted())) {
            throw exception(ErrorCodeConstants.CONTENT_NOT_EXISTS);
        }
        boolean isOwner = operatorUserId != null && Objects.equals(operatorUserId, comment.getUserId());
        boolean isContentAuthor = operatorUserId != null && Objects.equals(operatorUserId, content.getUserId());
        if (!isOwner && !isContentAuthor) {
            throw exception(ErrorCodeConstants.COMMENT_ACCESS_DENIED);
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

    private LambdaQueryWrapperX<ContentCommentDO> buildQueryWrapper(CommentPageReqVO reqVO, Long currentUserId) {
        LambdaQueryWrapperX<ContentCommentDO> wrapper = new LambdaQueryWrapperX<>();
        if (Boolean.TRUE.equals(reqVO.getMine())) {
            wrapper.eq(ContentCommentDO::getUserId, currentUserId);
        } else if (reqVO.getContentId() != null) {
            wrapper.eq(ContentCommentDO::getContentId, reqVO.getContentId());
            if (reqVO.getRootId() != null && reqVO.getRootId() > 0) {
                wrapper.eq(ContentCommentDO::getRootId, reqVO.getRootId())
                        .ne(ContentCommentDO::getId, reqVO.getRootId());
                wrapper.orderByAsc(ContentCommentDO::getCreateTime);
            } else {
                wrapper.and(q -> q.eq(ContentCommentDO::getParentId, 0L).or().isNull(ContentCommentDO::getParentId));
                if ("hot".equalsIgnoreCase(reqVO.getSort())) {
                    wrapper.orderByDesc(ContentCommentDO::getLikeCount)
                            .orderByDesc(ContentCommentDO::getReplyCount)
                            .orderByDesc(ContentCommentDO::getId);
                } else {
                    wrapper.orderByDesc(ContentCommentDO::getCreateTime);
                }
            }
        } else {
            return null;
        }

        wrapper.eq(ContentCommentDO::getStatus, COMMENT_STATUS_NORMAL)
                .eq(ContentCommentDO::getAuditStatus, AUDIT_STATUS_APPROVED)
                .eq(ContentCommentDO::getDeleted, 0);
        return wrapper;
    }

    private Map<Long, List<ContentCommentDO>> loadReplyPreview(CommentPageReqVO reqVO,
                                                               List<ContentCommentDO> roots) {
        if (reqVO.getRootId() != null || CollUtil.isEmpty(roots)) {
            return Collections.emptyMap();
        }
        boolean withReplies = reqVO.getWithReplies() == null || Boolean.TRUE.equals(reqVO.getWithReplies());
        if (!withReplies) {
            return Collections.emptyMap();
        }
        int previewSize = resolvePreviewSize(reqVO.getReplyPreviewSize());
        Map<Long, List<ContentCommentDO>> preview = new HashMap<>();
        for (ContentCommentDO root : roots) {
            Long rootId = resolveRootId(root);
            List<ContentCommentDO> replies = commentMapper.selectList(new LambdaQueryWrapperX<ContentCommentDO>()
                    .eq(ContentCommentDO::getRootId, rootId)
                    .ne(ContentCommentDO::getId, rootId)
                    .eq(ContentCommentDO::getDeleted, 0)
                    .eq(ContentCommentDO::getStatus, COMMENT_STATUS_NORMAL)
                    .eq(ContentCommentDO::getAuditStatus, AUDIT_STATUS_APPROVED)
                    .orderByAsc(ContentCommentDO::getCreateTime)
                    .last("LIMIT " + previewSize));
            preview.put(root.getId(), replies);
        }
        return preview;
    }

    private Set<Long> collectUserIds(List<ContentCommentDO> comments,
                                     Collection<List<ContentCommentDO>> replies) {
        Set<Long> ids = new HashSet<>();
        comments.forEach(comment -> collectUserIds(comment, ids));
        replies.forEach(list -> list.forEach(reply -> collectUserIds(reply, ids)));
        return ids;
    }

    private void collectUserIds(ContentCommentDO comment, Set<Long> ids) {
        if (comment.getUserId() != null) {
            ids.add(comment.getUserId());
        }
        if (comment.getReplyUserId() != null) {
            ids.add(comment.getReplyUserId());
        }
    }

    private Map<Long, MemberUserRespDTO> loadUserMap(Collection<Long> userIds) {
        if (CollUtil.isEmpty(userIds)) {
            return Collections.emptyMap();
        }
        try {
            return memberUserApi.getUserMap(userIds);
        } catch (Exception ex) {
            log.warn("Failed to load member users {}", userIds, ex);
            return Collections.emptyMap();
        }
    }

    private CommentRespVO convert(ContentCommentDO comment,
                                  List<ContentCommentDO> replies,
                                  Map<Long, MemberUserRespDTO> userMap,
                                  Long contentAuthorId,
                                  Long currentUserId) {
        CommentRespVO vo = buildSingleComment(comment, userMap, contentAuthorId, currentUserId);
        if (CollUtil.isNotEmpty(replies)) {
            List<CommentRespVO> replyVos = replies.stream()
                    .map(reply -> buildSingleComment(reply, userMap, contentAuthorId, currentUserId))
                    .collect(Collectors.toList());
            vo.setReplies(replyVos);
        } else {
            vo.setReplies(Collections.emptyList());
        }
        return vo;
    }

    private CommentRespVO buildSingleComment(ContentCommentDO comment,
                                             Map<Long, MemberUserRespDTO> userMap,
                                             Long contentAuthorId,
                                             Long currentUserId) {
        CommentRespVO vo = new CommentRespVO();
        vo.setId(comment.getId());
        vo.setContentId(comment.getContentId());
        vo.setUserId(comment.getUserId());
        vo.setContent(comment.getContent());
        vo.setImages(comment.getImages());
        vo.setParentId(comment.getParentId());
        vo.setRootId(comment.getRootId());
        vo.setReplyUserId(comment.getReplyUserId());
        vo.setAnonymous(comment.getIsAnonymous() != null && comment.getIsAnonymous() == 1);
        vo.setLikeCount(ObjectUtil.defaultIfNull(comment.getLikeCount(), 0));
        vo.setReplyCount(ObjectUtil.defaultIfNull(comment.getReplyCount(), 0));
        vo.setCreateTime(comment.getCreateTime());
        vo.setMine(currentUserId != null && Objects.equals(currentUserId, comment.getUserId()));
        vo.setAuthor(contentAuthorId != null && Objects.equals(contentAuthorId, comment.getUserId()));
        vo.setLiked(localCommentLikeStorage.isLiked(comment.getId(), currentUserId));

        if (!vo.getAnonymous()) {
            MemberUserRespDTO user = userMap.get(comment.getUserId());
            if (user != null) {
                vo.setUserName(StrUtil.blankToDefault(user.getNickname(), "用户" + user.getId()));
                vo.setUserAvatar(user.getAvatar());
            } else {
                vo.setUserName("用户" + comment.getUserId());
            }
        } else {
            vo.setUserName("匿名用户");
        }

        if (comment.getReplyUserId() != null) {
            MemberUserRespDTO replyUser = userMap.get(comment.getReplyUserId());
            if (replyUser != null) {
                vo.setReplyUserName(StrUtil.blankToDefault(replyUser.getNickname(), "用户" + replyUser.getId()));
                vo.setReplyUserAvatar(replyUser.getAvatar());
            }
        }
        vo.setReplies(Collections.emptyList());
        return vo;
    }

    private ContentDO validateContent(Long contentId) {
        if (contentId == null) {
            throw exception(ErrorCodeConstants.CONTENT_NOT_EXISTS);
        }
        ContentDO content = contentMapper.selectById(contentId);
        if (content == null || Boolean.TRUE.equals(content.getDeleted())
                || Objects.equals(content.getStatus(), ContentDO.StatusEnum.DELETED.getStatus())) {
            throw exception(ErrorCodeConstants.CONTENT_NOT_EXISTS);
        }
        return content;
    }

    private void ensureCommentAllowed(ContentDO content) {
        if (content.getAllowComment() != null && content.getAllowComment() == 0) {
            throw exception(ErrorCodeConstants.COMMENT_NOT_ALLOWED);
        }
    }

    private ContentCommentDO validateParentComment(Long parentId, Long contentId) {
        ContentCommentDO parent = commentMapper.selectById(parentId);
        if (parent == null || Boolean.TRUE.equals(parent.getDeleted())) {
            throw exception(ErrorCodeConstants.COMMENT_NOT_EXISTS);
        }
        if (!Objects.equals(parent.getContentId(), contentId)) {
            throw exception(ErrorCodeConstants.COMMENT_ACCESS_DENIED);
        }
        return parent;
    }

    private List<String> normalizeImages(List<String> images) {
        if (CollUtil.isEmpty(images)) {
            return null;
        }
        List<String> list = images.stream()
                .filter(StrUtil::isNotBlank)
                .map(String::trim)
                .limit(9)
                .collect(Collectors.toList());
        return list.isEmpty() ? null : list;
    }

    private Long resolveContentAuthor(CommentPageReqVO reqVO) {
        if (reqVO.getContentId() == null) {
            return null;
        }
        ContentDO content = contentMapper.selectById(reqVO.getContentId());
        if (content == null) {
            return null;
        }
        return content.getUserId();
    }

    private Long resolveRootId(ContentCommentDO comment) {
        if (comment.getRootId() != null && comment.getRootId() > 0) {
            return comment.getRootId();
        }
        if (comment.getParentId() != null && comment.getParentId() > 0) {
            return comment.getParentId();
        }
        return comment.getId();
    }

    private boolean isRootComment(ContentCommentDO comment) {
        return comment.getParentId() == null || comment.getParentId() == 0;
    }

    private List<ContentCommentDO> loadDescendants(ContentCommentDO comment) {
        List<ContentCommentDO> descendants = new ArrayList<>();
        List<Long> parents = new ArrayList<>();
        parents.add(comment.getId());
        while (!parents.isEmpty()) {
            List<ContentCommentDO> children = commentMapper.selectList(new LambdaQueryWrapperX<ContentCommentDO>()
                    .in(ContentCommentDO::getParentId, parents)
                    .eq(ContentCommentDO::getDeleted, Boolean.FALSE));
            if (CollUtil.isEmpty(children)) {
                break;
            }
            descendants.addAll(children);
            parents = children.stream().map(ContentCommentDO::getId).collect(Collectors.toList());
        }
        return descendants;
    }

    private int resolvePreviewSize(Integer previewSize) {
        if (previewSize == null || previewSize <= 0) {
            return DEFAULT_REPLY_PREVIEW;
        }
        return Math.min(previewSize, MAX_REPLY_PREVIEW);
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
