package cn.iocoder.yudao.module.content.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.content.controller.admin.ad.vo.ContentAdStatRespVO;
import cn.iocoder.yudao.module.content.controller.admin.ad.vo.ContentAdStatSummaryRespVO;
import cn.iocoder.yudao.module.content.dal.dataobject.ContentAdEventDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ContentAdEventMapper extends BaseMapperX<ContentAdEventDO> {

    @Select("""
            <script>
            SELECT e.ad_id AS adId,
                   a.title AS title,
                   a.advertiser_name AS advertiserName,
                   a.display_scene AS displayScene,
                   a.status AS status,
                   SUM(CASE WHEN e.event_type = 1 THEN 1 ELSE 0 END) AS impressionCount,
                   SUM(CASE WHEN e.event_type = 2 THEN 1 ELSE 0 END) AS clickCount,
                   COUNT(DISTINCT CASE WHEN e.event_type = 1 THEN e.user_id END) AS uniqueImpressionCount,
                   COUNT(DISTINCT CASE WHEN e.event_type = 2 THEN e.user_id END) AS uniqueClickCount,
                   MIN(e.create_time) AS firstEventTime,
                   MAX(e.create_time) AS lastEventTime
            FROM content_ad_event e
            LEFT JOIN content_ad a ON a.id = e.ad_id
            WHERE e.deleted = 0
            <if test="adId != null"> AND e.ad_id = #{adId}</if>
            <if test="scene != null"> AND e.scene = #{scene}</if>
            <if test="startTime != null"> AND e.create_time &gt;= #{startTime}</if>
            <if test="endTime != null"> AND e.create_time &lt;= #{endTime}</if>
            GROUP BY e.ad_id, a.title, a.advertiser_name, a.display_scene, a.status
            ORDER BY lastEventTime DESC
            LIMIT #{limit} OFFSET #{offset}
            </script>
            """)
    List<ContentAdStatRespVO> selectStatPage(@Param("adId") Long adId,
                                             @Param("scene") Integer scene,
                                             @Param("startTime") LocalDateTime startTime,
                                             @Param("endTime") LocalDateTime endTime,
                                             @Param("limit") Integer limit,
                                             @Param("offset") Integer offset);

    @Select("""
            <script>
            SELECT COUNT(DISTINCT e.ad_id)
            FROM content_ad_event e
            WHERE e.deleted = 0
            <if test="adId != null"> AND e.ad_id = #{adId}</if>
            <if test="scene != null"> AND e.scene = #{scene}</if>
            <if test="startTime != null"> AND e.create_time &gt;= #{startTime}</if>
            <if test="endTime != null"> AND e.create_time &lt;= #{endTime}</if>
            </script>
            """)
    Long selectStatCount(@Param("adId") Long adId,
                         @Param("scene") Integer scene,
                         @Param("startTime") LocalDateTime startTime,
                         @Param("endTime") LocalDateTime endTime);

    @Select("""
            <script>
            SELECT SUM(CASE WHEN e.event_type = 1 THEN 1 ELSE 0 END) AS impressionCount,
                   SUM(CASE WHEN e.event_type = 2 THEN 1 ELSE 0 END) AS clickCount,
                   COUNT(DISTINCT CASE WHEN e.event_type = 1 THEN e.user_id END) AS uniqueImpressionCount,
                   COUNT(DISTINCT CASE WHEN e.event_type = 2 THEN e.user_id END) AS uniqueClickCount
            FROM content_ad_event e
            WHERE e.deleted = 0
            <if test="adId != null"> AND e.ad_id = #{adId}</if>
            <if test="scene != null"> AND e.scene = #{scene}</if>
            <if test="startTime != null"> AND e.create_time &gt;= #{startTime}</if>
            <if test="endTime != null"> AND e.create_time &lt;= #{endTime}</if>
            </script>
            """)
    ContentAdStatSummaryRespVO selectStatSummary(@Param("adId") Long adId,
                                                 @Param("scene") Integer scene,
                                                 @Param("startTime") LocalDateTime startTime,
                                                 @Param("endTime") LocalDateTime endTime);
}
