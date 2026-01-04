package cn.iocoder.yudao.module.message.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 私信消息 DO
 *
 * @author xiaolvshu
 */
@TableName("message_private")
@Data
public class MessagePrivateDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long fromUserId;

    private Long toUserId;

    private Integer type;

    private String content;

    private String extraData;

    /**
     * 0-未读 1-已读 2-撤回
     */
    private Integer status;

    /**
     * 删除标记：0-未删除 1-发送方删除 2-接收方删除 3-双方删除
     */
    private Integer deleted;

    private LocalDateTime readTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
