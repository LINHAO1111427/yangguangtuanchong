package cn.iocoder.yudao.module.member.dal.dataobject.social;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户接触统计 DO
 *
 * 记录“可能认识的人”相关的统计指标。
 *
 * @author sun
 */
@TableName("member_contact_stat")
@KeySequence("member_contact_stat_seq")
@Data
@EqualsAndHashCode(callSuper = true)
public class MemberContactStatDO extends BaseDO {

    @TableId
    private Long id;
    /**
     * 当前用户
     */
    private Long userId;
    /**
     * 可能认识的目标用户
     */
    private Long targetUserId;
    /**
     * 是否同城
     */
    private Boolean sameCity;
    /**
     * 是否同区县
     */
    private Boolean sameDistrict;
    /**
     * 近距离接触次数
     */
    private Integer nearDistanceCount;
    /**
     * 相同 Wi-Fi 出现次数
     */
    private Integer sameWifiCount;
    /**
     * 相同 IP 出现次数
     */
    private Integer sameIpCount;
    /**
     * 最近一次出现时间
     */
    private LocalDateTime lastSeenTime;
    /**
     * 综合得分
     */
    private Double score;

}
