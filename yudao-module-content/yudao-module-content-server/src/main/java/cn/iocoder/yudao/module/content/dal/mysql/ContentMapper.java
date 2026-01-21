package cn.iocoder.yudao.module.content.dal.mysql;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.content.controller.admin.content.vo.ContentPostPageReqVO;
import cn.iocoder.yudao.module.content.controller.app.vo.ContentPageReqVO;
import cn.iocoder.yudao.module.content.dal.dataobject.ContentDO;
import cn.iocoder.yudao.module.content.service.vo.ContentAuthorStats;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
@Mapper
public interface ContentMapper extends BaseMapperX<ContentDO> {

    default PageResult<ContentDO> selectPage(ContentPageReqVO reqVO) {
        return selectPage(reqVO, buildPageWrapper(reqVO));
    }

    default PageResult<ContentDO> selectAdminPage(ContentPostPageReqVO reqVO) {
        return selectPage(reqVO, buildAdminPageWrapper(reqVO));
    }

    default List<ContentDO> selectListByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<ContentDO>()
                .in(ContentDO::getId, ids)
                .eq(ContentDO::getDeleted, 0));
    }

    default List<ContentDO> selectRecentPublic(int limit) {
        return selectList(new LambdaQueryWrapperX<ContentDO>()
                .eq(ContentDO::getDeleted, 0)
                .eq(ContentDO::getStatus, ContentDO.StatusEnum.PUBLISHED.getStatus())
                .orderByDesc(ContentDO::getPublishTime)
                .orderByDesc(ContentDO::getCreateTime)
                .last("LIMIT " + Math.max(limit, 1)));
    }

    default List<ContentDO> selectLatestByUserIds(Collection<Long> userIds, int limitPerUser) {
        if (CollUtil.isEmpty(userIds) || limitPerUser <= 0) {
            return Collections.emptyList();
        }
        List<ContentDO> result = new ArrayList<>();
        for (Long userId : userIds) {
            LambdaQueryWrapperX<ContentDO> wrapper = new LambdaQueryWrapperX<ContentDO>()
                    .eq(ContentDO::getUserId, userId)
                    .eq(ContentDO::getDeleted, 0)
                    .eq(ContentDO::getStatus, ContentDO.StatusEnum.PUBLISHED.getStatus())
                    .orderByDesc(ContentDO::getPublishTime)
                    .last("LIMIT " + limitPerUser);
            result.addAll(selectList(wrapper));
        }
        return result;
    }

    default List<ContentDO> selectListByUserId(Long userId, boolean includeDeleted) {
        if (userId == null) {
            return Collections.emptyList();
        }
        LambdaQueryWrapperX<ContentDO> wrapper = new LambdaQueryWrapperX<ContentDO>()
                .eq(ContentDO::getUserId, userId);
        if (!includeDeleted) {
            wrapper.ne(ContentDO::getStatus, ContentDO.StatusEnum.DELETED.getStatus())
                    .eq(ContentDO::getDeleted, 0);
        }
        wrapper.orderByDesc(ContentDO::getPublishTime)
                .orderByDesc(ContentDO::getCreateTime);
        return selectList(wrapper);
    }

    default List<ContentDO> selectLatestPublishedByChannelId(Long channelId, int limit) {
        if (channelId == null) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<ContentDO>()
                .eq(ContentDO::getChannelId, channelId)
                .eq(ContentDO::getDeleted, 0)
                .eq(ContentDO::getStatus, ContentDO.StatusEnum.PUBLISHED.getStatus())
                .orderByDesc(ContentDO::getPublishTime)
                .last("LIMIT " + Math.max(limit, 1)));
    }

    default List<ContentDO> selectDraftListByUserId(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<ContentDO>()
                .eq(ContentDO::getUserId, userId)
                .eq(ContentDO::getStatus, ContentDO.StatusEnum.DRAFT.getStatus())
                .eq(ContentDO::getDeleted, 0)
                .orderByDesc(ContentDO::getUpdateTime));
    }

    @Select("""
            SELECT #{userId} AS userId,
                   COUNT(1) AS workCount,
                   COALESCE(SUM(like_count), 0) AS totalLikeCount,
                   COALESCE(SUM(comment_count), 0) AS totalCommentCount,
                   COALESCE(SUM(collect_count), 0) AS totalCollectCount,
                   COALESCE(SUM(view_count), 0) AS totalViewCount
            FROM content_post
            WHERE user_id = #{userId}
              AND deleted = 0
              AND status = #{status}
            """)
    ContentAuthorStats selectAuthorStats(@Param("userId") Long userId,
                                         @Param("status") Integer publishedStatus);

    @Update("""
            UPDATE content_post
               SET like_count = GREATEST(COALESCE(like_count, 0) + #{delta}, 0),
                   update_time = NOW()
             WHERE id = #{contentId}
               AND deleted = 0
            """)
    int updateLikeCount(@Param("contentId") Long contentId, @Param("delta") int delta);

    @Update("""
            UPDATE content_post
               SET collect_count = GREATEST(COALESCE(collect_count, 0) + #{delta}, 0),
                   update_time = NOW()
             WHERE id = #{contentId}
               AND deleted = 0
            """)
    int updateCollectCount(@Param("contentId") Long contentId, @Param("delta") int delta);

    default LambdaQueryWrapperX<ContentDO> buildPageWrapper(ContentPageReqVO reqVO) {
        LambdaQueryWrapperX<ContentDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(ContentDO::getDeleted, 0)
                .ne(ContentDO::getStatus, ContentDO.StatusEnum.DELETED.getStatus());

        if (reqVO.getUserId() != null) {
            wrapper.eq(ContentDO::getUserId, reqVO.getUserId());
        }
        if (reqVO.getPublishTopicId() != null) {
            wrapper.eq(ContentDO::getPublishTopicId, reqVO.getPublishTopicId());
        }
        if (reqVO.getChannelId() != null) {
            wrapper.eq(ContentDO::getChannelId, reqVO.getChannelId());
        }
        if (reqVO.getContentType() != null) {
            wrapper.eq(ContentDO::getContentType, reqVO.getContentType());
        }
        if (reqVO.getAuditStatus() != null) {
            wrapper.eq(ContentDO::getAuditStatus, reqVO.getAuditStatus());
        }
        if (reqVO.getStatus() != null) {
            wrapper.eq(ContentDO::getStatus, reqVO.getStatus());
        }
        if (reqVO.getIsPublic() != null) {
            wrapper.eq(ContentDO::getIsPublic, reqVO.getIsPublic());
        }
        if (reqVO.getCreateTimeStart() != null || reqVO.getCreateTimeEnd() != null) {
            wrapper.betweenIfPresent(ContentDO::getCreateTime,
                    reqVO.getCreateTimeStart(), reqVO.getCreateTimeEnd());
        }

        if (StrUtil.isNotBlank(reqVO.getTitle())) {
            wrapper.like(ContentDO::getTitle, reqVO.getTitle());
        }
        if (StrUtil.isNotBlank(reqVO.getContent())) {
            wrapper.like(ContentDO::getContent, reqVO.getContent());
        }
        if (StrUtil.isNotBlank(reqVO.getKeyword())) {
            String keyword = reqVO.getKeyword();
            wrapper.and(w -> {
                w.like(ContentDO::getTitle, keyword);
                w.or();
                w.like(ContentDO::getContent, keyword);
            });
        }

        return wrapper;
    }

    default LambdaQueryWrapperX<ContentDO> buildAdminPageWrapper(ContentPostPageReqVO reqVO) {
        LambdaQueryWrapperX<ContentDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(ContentDO::getDeleted, 0);

        if (reqVO.getUserId() != null) {
            wrapper.eq(ContentDO::getUserId, reqVO.getUserId());
        }
        if (reqVO.getPublishTopicId() != null) {
            wrapper.eq(ContentDO::getPublishTopicId, reqVO.getPublishTopicId());
        }
        if (reqVO.getChannelId() != null) {
            wrapper.eq(ContentDO::getChannelId, reqVO.getChannelId());
        }
        if (reqVO.getContentType() != null) {
            wrapper.eq(ContentDO::getContentType, reqVO.getContentType());
        }
        if (reqVO.getAuditStatus() != null) {
            wrapper.eq(ContentDO::getAuditStatus, reqVO.getAuditStatus());
        }
        if (reqVO.getStatus() != null) {
            wrapper.eq(ContentDO::getStatus, reqVO.getStatus());
        }
        if (reqVO.getIsPublic() != null) {
            wrapper.eq(ContentDO::getIsPublic, reqVO.getIsPublic());
        }
        if (reqVO.getIsTop() != null) {
            wrapper.eq(ContentDO::getIsTop, reqVO.getIsTop());
        }
        if (reqVO.getIsHot() != null) {
            wrapper.eq(ContentDO::getIsHot, reqVO.getIsHot());
        }
        if (reqVO.getIsRecommend() != null) {
            wrapper.eq(ContentDO::getIsRecommend, reqVO.getIsRecommend());
        }
        if (reqVO.getCreateTime() != null && reqVO.getCreateTime().length == 2) {
            wrapper.between(ContentDO::getCreateTime, reqVO.getCreateTime()[0], reqVO.getCreateTime()[1]);
        }

        if (StrUtil.isNotBlank(reqVO.getTitle())) {
            wrapper.like(ContentDO::getTitle, reqVO.getTitle());
        }
        if (StrUtil.isNotBlank(reqVO.getKeyword())) {
            String keyword = reqVO.getKeyword();
            wrapper.and(w -> {
                w.like(ContentDO::getTitle, keyword);
                w.or();
                w.like(ContentDO::getContent, keyword);
            });
        }
        wrapper.orderByDesc(ContentDO::getCreateTime);
        return wrapper;
    }
}
