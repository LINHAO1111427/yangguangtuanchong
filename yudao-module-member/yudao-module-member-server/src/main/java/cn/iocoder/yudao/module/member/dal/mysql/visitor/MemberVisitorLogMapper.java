package cn.iocoder.yudao.module.member.dal.mysql.visitor;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.member.dal.dataobject.visitor.MemberVisitorLogDO;
import com.baomidou.mybatisplus.core.metadata.IPage;
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

    @Select({
            "<script>",
            "SELECT id, user_id, visitor_id, visit_type, target_id, is_paid, pay_amount, tenant_id,",
            "       create_time::timestamp AS create_time,",
            "       update_time::timestamp AS update_time,",
            "       creator, updater, deleted",
            "FROM member_visitor_log",
            "WHERE deleted = 0",
            "  AND visitor_id = #{visitorId}",
            "  <if test='visitType != null'> AND visit_type = #{visitType} </if>",
            "ORDER BY create_time DESC",
            "</script>"
    })
    IPage<MemberVisitorLogDO> selectPageByVisitor(IPage<?> page,
                                                  @Param("visitorId") Long visitorId,
                                                  @Param("visitType") Integer visitType);

    @Select({
            "<script>",
            "SELECT id, user_id, visitor_id, visit_type, target_id, is_paid, pay_amount, tenant_id,",
            "       create_time::timestamp AS create_time,",
            "       update_time::timestamp AS update_time,",
            "       creator, updater, deleted",
            "FROM member_visitor_log",
            "WHERE deleted = 0",
            "  AND user_id = #{userId}",
            "  <if test='visitType != null'> AND visit_type = #{visitType} </if>",
            "ORDER BY create_time DESC",
            "</script>"
    })
    IPage<MemberVisitorLogDO> selectPageByUser(IPage<?> page,
                                               @Param("userId") Long userId,
                                               @Param("visitType") Integer visitType);

    class MemberVisitorDayRow {
        private String day;
        private Long cnt;

        public String getDay() {
            return day;
        }

        public void setDay(String day) {
            this.day = day;
        }

        public Long getCnt() {
            return cnt;
        }

        public void setCnt(Long cnt) {
            this.cnt = cnt;
        }
    }
}
