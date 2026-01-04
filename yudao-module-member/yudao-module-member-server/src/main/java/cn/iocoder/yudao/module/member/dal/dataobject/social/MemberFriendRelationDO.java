package cn.iocoder.yudao.module.member.dal.dataobject.social;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 社交关系 DO
 *
 * 记录用户之间的关注 / 好友申请信息。
 *
 * @author sun
 */
@TableName("member_friend_relation")
@KeySequence("member_friend_relation_seq")
@Data
@EqualsAndHashCode(callSuper = true)
public class MemberFriendRelationDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 发起操作的用户
     */
    private Long userId;
    /**
     * 目标用户
     */
    private Long friendId;
    /**
     * 关系类型：0 = 关注、1 = 好友
     */
    private Integer relationType;
    /**
     * 状态：0 待审核、1 已通过、2 已拒绝
     */
    private Integer state;
    /**
     * 申请来源，如：0=搜索、1=推荐 等
     */
    private Integer source;
    /**
     * 请求附言
     */
    private String requestMessage;
    /**
     * 备注信息
     */
    private String remark;
    /**
     * 最近一次处理时间
     */
    private LocalDateTime lastActionAt;

}
