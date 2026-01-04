package cn.iocoder.yudao.module.message.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 群成员 DO
 *
 * @author xiaolvshu
 */
@TableName("group_member")
@Data
public class GroupMemberDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long groupId;

    private Long userId;

    /**
     * 群内昵称
     */
    private String nickname;

    /**
     * 1-群主 2-管理员 3-普通成员
     */
    private Integer role;

    /**
     * 0-正常 1-已退出 2-被移除
     */
    private Integer status;

    /**
     * 0-正常 1-禁言
     */
    private Integer muted;

    private LocalDateTime muteEndTime;

    private LocalDateTime joinTime;

    private LocalDateTime quitTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
