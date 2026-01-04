package cn.iocoder.yudao.module.message.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会话 DO
 *
 * @author xiaolvshu
 */
@TableName("message_conversation")
@Data
public class ConversationDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long targetId;

    /**
     * 1-私信 2-群聊
     */
    private Integer type;

    private Long lastMessageId;

    private String lastMessageContent;

    private LocalDateTime lastMessageTime;

    private Integer unreadCount;

    /**
     * 0-否 1-是
     */
    private Integer isTop;

    /**
     * 0-否 1-是
     */
    private Integer isMute;

    /**
     * 0-否 1-是
     */
    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
