package cn.iocoder.yudao.module.message.constants;

/**
 * Message 模块常量定义
 */
public interface MessageConstants {

    /**
     * 私聊/群聊消息类型
     */
    interface ChatMessageType {
        int TEXT = 1;
        int IMAGE = 2;
        int VIDEO = 3;
        int VOICE = 4;
        int FILE = 5;
        int SHARE_CARD = 6;
        int GIF = 7;
        int SYSTEM = 8;
    }

    /**
     * 通知类型
     */
    interface NotificationType {
        int LIKE = 1;
        int COMMENT = 2;
        int FOLLOW = 3;
        int SYSTEM = 4;
        int AUDIT = 5;
    }

    /**
     * 行为事件类型（Kafka）
     */
    interface BehaviorType {
        String LIKE = "like";
        String COMMENT_LIKE = "comment_like";
        String COMMENT = "comment";
        String FOLLOW = "follow";
        String SHARE = "share";
        String COLLECT = "collect";
    }
}
