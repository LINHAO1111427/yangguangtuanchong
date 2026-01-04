package cn.iocoder.yudao.module.content.framework.kafka.message;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 内容互动消息（点赞、评论、收藏、分享）
 *
 * @author 阳光团宠
 */
@Data
public class ContentInteractionMessage implements Serializable {

    /**
     * 内容ID
     */
    private Long postId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 互动类型: like/comment/collect/share
     */
    private String interactionType;

    /**
     * 操作动作: add/cancel
     */
    private String action;

    /**
     * 关联ID（如评论ID）
     */
    private Long relatedId;

    /**
     * 事件时间
     */
    private LocalDateTime eventTime;
}
