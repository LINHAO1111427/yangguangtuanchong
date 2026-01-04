package cn.iocoder.yudao.module.content.dal.mysql;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.content.dal.dataobject.ChannelVisitStatsDO;
import cn.iocoder.yudao.module.content.dal.dataobject.ContentInteractionDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.List;

@Mapper
public interface ContentInteractionMapper extends BaseMapperX<ContentInteractionDO> {

    default List<ContentInteractionDO> selectByContentId(Long contentId) {
        return selectList(ContentInteractionDO::getContentId, contentId);
    }

    default ContentInteractionDO selectOne(Long contentId, Long userId, Integer type) {
        return selectOne(new LambdaQueryWrapperX<ContentInteractionDO>()
                .eq(ContentInteractionDO::getContentId, contentId)
                .eq(ContentInteractionDO::getUserId, userId)
                .eq(ContentInteractionDO::getInteractionType, type)
                .last("LIMIT 1"));
    }

    default List<ContentInteractionDO> selectRecent(Long userId, Integer type, int limit) {
        LambdaQueryWrapperX<ContentInteractionDO> wrapper = new LambdaQueryWrapperX<ContentInteractionDO>()
                .eq(ContentInteractionDO::getUserId, userId)
                .orderByDesc(ContentInteractionDO::getUpdateTime);
        if (type != null) {
            wrapper.eq(ContentInteractionDO::getInteractionType, type);
        }
        wrapper.last("LIMIT " + Math.max(limit, 1));
        return selectList(wrapper);
    }

    default List<ContentInteractionDO> selectByUserAndContentIds(Long userId,
                                                                Collection<Long> contentIds,
                                                                Integer type) {
        if (userId == null || CollUtil.isEmpty(contentIds)) {
            return List.of();
        }
        LambdaQueryWrapperX<ContentInteractionDO> wrapper = new LambdaQueryWrapperX<ContentInteractionDO>()
                .eq(ContentInteractionDO::getUserId, userId)
                .in(ContentInteractionDO::getContentId, contentIds);
        if (type != null) {
            wrapper.eq(ContentInteractionDO::getInteractionType, type);
        }
        return selectList(wrapper);
    }

    /**
     * 分页查询用户的交互记录
     */
    default List<ContentInteractionDO> selectPageByUserAndType(Long userId, Integer type, Integer offset, Integer limit) {
        int safeOffset = offset == null ? 0 : Math.max(offset, 0);
        int safeLimit = limit == null ? 1 : Math.max(limit, 1);
        return selectList(new LambdaQueryWrapperX<ContentInteractionDO>()
                .eq(ContentInteractionDO::getUserId, userId)
                .eq(ContentInteractionDO::getInteractionType, type)
                .eq(ContentInteractionDO::getDeleted, (short) 0)
                .orderByDesc(ContentInteractionDO::getUpdateTime)
                .last("LIMIT " + safeLimit + " OFFSET " + safeOffset));
    }

    /**
     * 统计用户某类型的交互数量
     */
    default Long countByUserAndType(Long userId, Integer type) {
        return selectCount(new LambdaQueryWrapperX<ContentInteractionDO>()
                .eq(ContentInteractionDO::getUserId, userId)
                .eq(ContentInteractionDO::getInteractionType, type)
                .eq(ContentInteractionDO::getDeleted, (short) 0));
    }

    /**
     * 按用户和交互类型批量软删
     */
    default int softDeleteByUserAndType(Long userId, Integer type) {
        LambdaQueryWrapperX<ContentInteractionDO> wrapper = new LambdaQueryWrapperX<ContentInteractionDO>()
                .eq(ContentInteractionDO::getUserId, userId)
                .eq(ContentInteractionDO::getInteractionType, type)
                .eq(ContentInteractionDO::getDeleted, (short) 0);
        ContentInteractionDO update = new ContentInteractionDO();
        update.setDeleted((short) 1);
        return update(update, wrapper);
    }

    /**
     * 统计用户最近访问的频道情况
     */
    @Select("SELECT cp.channel_id AS channelId, " +
            "       cp.channel_name AS channelName, " +
            "       COUNT(1) AS visitCount, " +
            "       MAX(ci.update_time) AS lastVisitTime " +
            "FROM content_interaction ci " +
            "JOIN content_post cp ON ci.content_id = cp.id " +
            "WHERE ci.user_id = #{userId} " +
            "  AND ci.deleted = 0 " +
            "  AND ci.interaction_type = #{interactionType} " +
            "  AND cp.deleted = 0 " +
            "  AND cp.channel_id IS NOT NULL " +
            "GROUP BY cp.channel_id, cp.channel_name " +
            "ORDER BY visitCount DESC, lastVisitTime DESC " +
            "LIMIT #{limit}")
    List<ChannelVisitStatsDO> selectChannelVisitStats(@Param("userId") Long userId,
                                                      @Param("interactionType") Integer interactionType,
                                                      @Param("limit") Integer limit);
}
