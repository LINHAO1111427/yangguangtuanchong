package cn.iocoder.yudao.module.content.framework.kafka.message;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户行为消息
 *
 * @author 阳光团宠
 */
@Data
public class UserBehaviorMessage implements Serializable {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 内容ID
     */
    private Long postId;

    /**
     * 行为类型: view/like/collect/share/comment
     */
    private String behaviorType;

    /**
     * 操作动作: add/cancel
     */
    private String action;

    /**
     * 来源: home/search/topic/profile/recommend
     */
    private String source;

    /**
     * 停留时长（秒）- 仅用于 view 行为
     */
    private Integer duration;

    /**
     * 事件时间
     */
    private LocalDateTime eventTime;

    /**
     * 扩展数据（JSON格式）
     */
    private String extra;
}
