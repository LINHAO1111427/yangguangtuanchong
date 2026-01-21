package cn.iocoder.yudao.module.content.dal.mysql;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.content.dal.dataobject.ChannelVisitStatsDO;
import cn.iocoder.yudao.module.content.dal.dataobject.ContentInteractionDO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

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

    @Select("SELECT * FROM content_interaction " +
            "WHERE content_id = #{contentId} AND user_id = #{userId} AND interaction_type = #{type} " +
            "ORDER BY id DESC LIMIT 1")
    ContentInteractionDO selectOneIncludeDeleted(@Param("contentId") Long contentId,
                                                 @Param("userId") Long userId,
                                                 @Param("type") Integer type);

    @Update("""
            UPDATE content_interaction
               SET ip_address = #{ipAddress},
                   user_agent = #{userAgent},
                   deleted = #{deleted},
                   update_time = #{updateTime},
                   updater = #{updater}
             WHERE id = #{id}
            """)
    int updateByIdIncludeDeleted(ContentInteractionDO interaction);

    default List<ContentInteractionDO> selectRecent(Long userId, Integer type, int limit) {
        LambdaQueryWrapperX<ContentInteractionDO> wrapper = new LambdaQueryWrapperX<ContentInteractionDO>()
                .eq(ContentInteractionDO::getUserId, userId)
                .eq(ContentInteractionDO::getDeleted, (short) 0)
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
        if (userId == null || type == null) {
            return 0;
        }
        return softDeleteByUserAndTypeDirect(userId, type);
    }

    /**
     * 按用户、交互类型与内容 ID 批量软删
     */
    default int softDeleteByUserAndTypeAndContentIds(Long userId, Integer type, Collection<Long> contentIds) {
        if (userId == null || CollUtil.isEmpty(contentIds)) {
            return 0;
        }
        return softDeleteByUserAndTypeAndContentIdsDirect(userId, type, contentIds);
    }

    @Update("UPDATE content_interaction SET deleted = 1, update_time = NOW() " +
            "WHERE user_id = #{userId} AND interaction_type = #{type}")
    int softDeleteByUserAndTypeDirect(@Param("userId") Long userId,
                                      @Param("type") Integer type);

    @Update({
            "<script>",
            "UPDATE content_interaction",
            " SET deleted = 1, update_time = NOW()",
            " WHERE user_id = #{userId}",
            " AND interaction_type = #{type}",
            " AND content_id IN",
            " <foreach collection='contentIds' item='id' open='(' separator=',' close=')'>",
            "   #{id}",
            " </foreach>",
            "</script>"
    })
    int softDeleteByUserAndTypeAndContentIdsDirect(@Param("userId") Long userId,
                                                   @Param("type") Integer type,
                                                   @Param("contentIds") Collection<Long> contentIds);

    @Delete("DELETE FROM content_interaction WHERE user_id = #{userId} AND interaction_type = #{type}")
    int hardDeleteByUserAndType(@Param("userId") Long userId,
                                @Param("type") Integer type);

    @Delete({
            "<script>",
            "DELETE FROM content_interaction",
            " WHERE user_id = #{userId}",
            " AND interaction_type = #{type}",
            " AND content_id IN",
            " <foreach collection='contentIds' item='id' open='(' separator=',' close=')'>",
            "   #{id}",
            " </foreach>",
            "</script>"
    })
    int hardDeleteByUserAndTypeAndContentIds(@Param("userId") Long userId,
                                             @Param("type") Integer type,
                                             @Param("contentIds") Collection<Long> contentIds);

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
