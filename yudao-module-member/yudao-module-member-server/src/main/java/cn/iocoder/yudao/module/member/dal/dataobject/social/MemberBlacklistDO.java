package cn.iocoder.yudao.module.member.dal.dataobject.social;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 社交黑名单 DO
 *
 * @author sun
 */
@TableName("member_blacklist")
@KeySequence("member_blacklist_seq")
@Data
@EqualsAndHashCode(callSuper = true)
public class MemberBlacklistDO extends BaseDO {

    @TableId
    private Long id;
    /**
     * 用户编号
     */
    private Long userId;
    /**
     * 被拉黑的用户
     */
    private Long targetId;
    /**
     * 拉黑原因
     */
    private String reason;

}
