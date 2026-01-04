package cn.iocoder.yudao.module.message.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统通知 DO
 *
 * @author xiaolvshu
 */
@TableName("message_notification")
@Data
public class NotificationDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Integer type;

    private String title;

    private String content;

    private String relatedData;

    private String link;
    private Integer isRead;

    private LocalDateTime readTime;

    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
