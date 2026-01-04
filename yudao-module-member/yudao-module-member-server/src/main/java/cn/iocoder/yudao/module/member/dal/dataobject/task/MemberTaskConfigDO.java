package cn.iocoder.yudao.module.member.dal.dataobject.task;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 会员任务配置 DO
 *
 * @author xiaolvshu
 */
@TableName("member_task_config")
@KeySequence("member_task_config_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberTaskConfigDO extends BaseDO {

    /**
     * 任务ID
     */
    @TableId
    private Long id;

    /**
     * 任务类型
     * 1-观看短视频 2-观看图文 3-观看广告
     */
    private Integer taskType;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 任务描述
     */
    private String taskDesc;

    /**
     * 任务要求数量
     * 例如：观看3条短视频，则此值为3
     */
    private Integer requiredCount;

    /**
     * 奖励积分数量
     */
    private Integer rewardPoint;

    /**
     * 任务状态
     * 0-禁用 1-启用
     */
    private Integer status;

    /**
     * 排序
     */
    private Integer sort;

}
