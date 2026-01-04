package cn.iocoder.yudao.module.member.dal.dataobject.task;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDate;

/**
 * 会员任务完成记录 DO
 *
 * @author xiaolvshu
 */
@TableName("member_task_record")
@KeySequence("member_task_record_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberTaskRecordDO extends BaseDO {

    /**
     * 记录ID
     */
    @TableId
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 任务类型
     * 1-观看短视频 2-观看图文 3-观看广告
     */
    private Integer taskType;

    /**
     * 完成进度
     * 当前已完成数量
     */
    private Integer progress;

    /**
     * 任务要求数量
     */
    private Integer requiredCount;

    /**
     * 任务状态
     * 0-进行中 1-已完成 2-已领取奖励
     */
    private Integer status;

    /**
     * 奖励积分数量
     */
    private Integer rewardPoint;

    /**
     * 任务日期
     * 用于每日任务重置
     */
    private LocalDate taskDate;

}
