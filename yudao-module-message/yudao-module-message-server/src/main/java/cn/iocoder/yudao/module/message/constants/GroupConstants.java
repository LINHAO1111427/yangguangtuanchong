package cn.iocoder.yudao.module.message.constants;

/**
 * 群组相关常量
 *
 * @author xiaolvshu
 */
public class GroupConstants {

    // ========== 群组默认配置 ==========
    /**
     * 默认最大成员数
     */
    public static final Integer DEFAULT_MAX_MEMBER_COUNT = 500;

    /**
     * 默认加入方式：0-自由加入
     */
    public static final Integer DEFAULT_JOIN_TYPE_FREE = 0;

    /**
     * 群组状态：0-正常
     */
    public static final Integer GROUP_STATUS_ACTIVE = 0;

    /**
     * 群组状态：1-已解散
     */
    public static final Integer GROUP_STATUS_DISSOLVED = 1;

    /**
     * 全员禁言：0-关闭
     */
    public static final Integer MUTE_ALL_DISABLED = 0;

    /**
     * 全员禁言：1-开启
     */
    public static final Integer MUTE_ALL_ENABLED = 1;

    // ========== 成员角色 ==========
    /**
     * 角色：1-群主
     */
    public static final Integer ROLE_OWNER = 1;

    /**
     * 角色：2-管理员
     */
    public static final Integer ROLE_ADMIN = 2;

    /**
     * 角色：3-普通成员
     */
    public static final Integer ROLE_MEMBER = 3;

    // ========== 成员状态 ==========
    /**
     * 成员状态：0-正常
     */
    public static final Integer MEMBER_STATUS_ACTIVE = 0;

    /**
     * 成员状态：1-已退出
     */
    public static final Integer MEMBER_STATUS_QUIT = 1;

    /**
     * 成员状态：2-被踢出
     */
    public static final Integer MEMBER_STATUS_KICKED = 2;

    /**
     * 禁言状态：0-未禁言
     */
    public static final Integer MUTE_STATUS_NORMAL = 0;

    /**
     * 禁言状态：1-已禁言
     */
    public static final Integer MUTE_STATUS_MUTED = 1;

    // ========== 消息类型 ==========
    /**
     * 消息类型：1-文本
     */
    public static final Integer MESSAGE_TYPE_TEXT = 1;

    /**
     * 消息类型：10-系统消息
     */
    public static final Integer MESSAGE_TYPE_SYSTEM = 10;

    /**
     * 消息状态：0-正常
     */
    public static final Integer MESSAGE_STATUS_NORMAL = 0;

    /**
     * 消息状态：1-已撤回
     */
    public static final Integer MESSAGE_STATUS_RECALLED = 1;

    /**
     * 消息删除状态：0-未删除
     */
    public static final Integer MESSAGE_NOT_DELETED = 0;

    // ========== 业务规则 ==========
    /**
     * 消息撤回时间限制（分钟）
     */
    public static final Integer MESSAGE_RECALL_TIMEOUT_MINUTES = 2;

    /**
     * 默认消息查询数量
     */
    public static final Integer DEFAULT_MESSAGE_LIMIT = 20;

    /**
     * 角色判断：管理员及以上
     */
    public static final Integer ROLE_ADMIN_OR_ABOVE = 2;

}
