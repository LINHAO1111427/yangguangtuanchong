package cn.iocoder.yudao.module.message.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * Message 错误码常量
 * <p>
 * message 模块，使用 14-000-000 错误码段
 *
 * @author xiaolvshu
 */
public interface ErrorCodeConstants {

    // ========== 群组管理错误码 14-001-000 ~ 14-001-099 ==========
    ErrorCode GROUP_NOT_EXISTS = new ErrorCode(14001000, "群组不存在或已解散");
    ErrorCode GROUP_NAME_REQUIRED = new ErrorCode(14001001, "群名称不能为空");
    ErrorCode GROUP_ONLY_OWNER_CAN_DISSOLVE = new ErrorCode(14001002, "只有群主可以解散群组");
    ErrorCode GROUP_MEMBER_FULL = new ErrorCode(14001003, "群成员已满，无法添加");
    ErrorCode GROUP_MEMBER_NOT_EXISTS = new ErrorCode(14001004, "该用户不是群成员");
    ErrorCode GROUP_OWNER_CANNOT_QUIT = new ErrorCode(14001005, "群主不能退出群组，请先转让群主或解散群组");
    ErrorCode GROUP_UPDATE_PERMISSION_DENIED = new ErrorCode(14001006, "只有群主或管理员可以修改群信息");
    ErrorCode GROUP_INVITE_FORBIDDEN = new ErrorCode(14001007, "您不是群成员，无法邀请");

    // ========== 群成员管理错误码 14-001-100 ~ 14-001-199 ==========
    ErrorCode GROUP_CANNOT_REMOVE_OWNER = new ErrorCode(14001100, "不能移除群主");
    ErrorCode GROUP_ADMIN_REMOVE_DENIED = new ErrorCode(14001101, "管理员不能移除其他管理员");
    ErrorCode GROUP_REMOVE_PERMISSION_DENIED = new ErrorCode(14001102, "只有群主或管理员可以移除成员");
    ErrorCode GROUP_SET_ROLE_ONLY_OWNER = new ErrorCode(14001103, "只有群主可以设置成员角色");
    ErrorCode GROUP_ROLE_OWNER_FORBIDDEN = new ErrorCode(14001104, "不能修改群主角色");
    ErrorCode GROUP_ROLE_INVALID = new ErrorCode(14001105, "角色值无效，只能设置为管理员或普通成员");
    ErrorCode GROUP_TRANSFER_ONLY_OWNER = new ErrorCode(14001106, "只有群主可以转让群主");
    ErrorCode GROUP_TRANSFER_TARGET_NOT_MEMBER = new ErrorCode(14001107, "目标用户不是群成员");

    // ========== 群成员禁言错误码 14-001-200 ~ 14-001-299 ==========
    ErrorCode GROUP_CANNOT_MUTE_OWNER = new ErrorCode(14001200, "不能禁言群主");
    ErrorCode GROUP_ADMIN_MUTE_DENIED = new ErrorCode(14001201, "管理员不能禁言其他管理员");
    ErrorCode GROUP_MUTE_PERMISSION_DENIED = new ErrorCode(14001202, "只有群主或管理员可以禁言成员");
    ErrorCode GROUP_UNMUTE_PERMISSION_DENIED = new ErrorCode(14001203, "只有群主或管理员可以取消禁言");
    ErrorCode GROUP_MUTEALL_ONLY_OWNER = new ErrorCode(14001204, "只有群主可以设置全员禁言");

    // ========== 群消息错误码 14-002-000 ~ 14-002-099 ==========
    ErrorCode GROUP_SEND_NOT_MEMBER = new ErrorCode(14002000, "您不是群成员，无法发送消息");
    ErrorCode GROUP_SEND_MUTED = new ErrorCode(14002001, "您已被禁言，无法发送消息");
    ErrorCode GROUP_SEND_ALL_MUTED = new ErrorCode(14002002, "群组已开启全员禁言");
    ErrorCode MESSAGE_NOT_EXISTS = new ErrorCode(14002003, "消息不存在");
    ErrorCode MESSAGE_RECALL_ONLY_SELF = new ErrorCode(14002004, "只能撤回自己的消息");
    ErrorCode MESSAGE_RECALL_TIMEOUT = new ErrorCode(14002005, "只能撤回2分钟内的消息");

    // ========== 私信错误码 14-003-000 ~ 14-003-099 ==========
    ErrorCode PRIVATE_MESSAGE_NOT_EXISTS = new ErrorCode(14003000, "私信不存在");
    ErrorCode PRIVATE_MESSAGE_BLOCKED = new ErrorCode(14003001, "对方已拉黑您，无法发送消息");
    ErrorCode PRIVATE_MESSAGE_SEND_TOO_FREQUENT = new ErrorCode(14003002, "消息发送过于频繁，请稍后再试");
    ErrorCode MESSAGE_SEND_NOT_ALLOWED = new ErrorCode(14003003, "无法向对方发送消息，请检查隐私设置");
    ErrorCode PRIVATE_MESSAGE_CONTENT_REQUIRED = new ErrorCode(14003004, "消息内容不能为空");
    ErrorCode PRIVATE_MESSAGE_MEDIA_REQUIRED = new ErrorCode(14003005, "媒体内容不能为空");
    ErrorCode PRIVATE_MESSAGE_CARD_REQUIRED = new ErrorCode(14003006, "分享卡片内容不能为空");
    ErrorCode PRIVATE_MESSAGE_TYPE_INVALID = new ErrorCode(14003007, "消息类型无效");
    ErrorCode CONVERSATION_NOT_EXISTS = new ErrorCode(14003010, "会话不存在");
    ErrorCode CONVERSATION_PERMISSION_DENIED = new ErrorCode(14003011, "您无权操作该会话");
    ErrorCode CONVERSATION_TOP_INVALID = new ErrorCode(14003012, "置顶参数无效");
    ErrorCode CONVERSATION_MUTE_INVALID = new ErrorCode(14003013, "免打扰参数无效");

    // ========== 系统通知错误码 14-004-000 ~ 14-004-099 ==========
    ErrorCode NOTIFICATION_NOT_EXISTS = new ErrorCode(14004000, "通知不存在");
    ErrorCode NOTIFICATION_ALREADY_READ = new ErrorCode(14004001, "通知已读");
    ErrorCode NOTIFICATION_TYPE_INVALID = new ErrorCode(14004002, "通知类型无效");
    ErrorCode NOTIFICATION_USER_REQUIRED = new ErrorCode(14004003, "通知用户不能为空");
    ErrorCode NOTIFICATION_CONTENT_REQUIRED = new ErrorCode(14004004, "通知内容不能为空");
    ErrorCode NOTIFICATION_PERMISSION_DENIED = new ErrorCode(14004005, "您无权操作该通知");

}
