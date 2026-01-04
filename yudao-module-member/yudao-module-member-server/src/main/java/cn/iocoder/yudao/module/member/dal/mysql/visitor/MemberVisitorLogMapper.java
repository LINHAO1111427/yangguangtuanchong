package cn.iocoder.yudao.module.member.dal.mysql.visitor;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.member.dal.dataobject.visitor.MemberVisitorLogDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface MemberVisitorLogMapper extends BaseMapperX<MemberVisitorLogDO> {

    @Select("""
            SELECT COALESCE(COUNT(DISTINCT visitor_id), 0)
            FROM member_visitor_log
            WHERE deleted = 0
              AND user_id = #{userId}
              AND create_time >= #{beginTime}
              AND create_time < #{endTime}
            """)
    long selectDistinctVisitorCount(@Param("userId") Long userId,
                                   @Param("beginTime") LocalDateTime beginTime,
                                   @Param("endTime") LocalDateTime endTime);

    @Select("""
            SELECT DATE(create_time) AS day, COALESCE(COUNT(DISTINCT visitor_id), 0) AS cnt
            FROM member_visitor_log
            WHERE deleted = 0
              AND user_id = #{userId}
              AND create_time >= #{beginTime}
              AND create_time < #{endTime}
            GROUP BY DATE(create_time)
            ORDER BY day ASC
            """)
    List<MemberVisitorDayRow> selectDailyDistinctVisitorCount(@Param("userId") Long userId,
                                                             @Param("beginTime") LocalDateTime beginTime,
                                                             @Param("endTime") LocalDateTime endTime);

    interface MemberVisitorDayRow {
        String getDay();

        Long getCnt();
    }
}

