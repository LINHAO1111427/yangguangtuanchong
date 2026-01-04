package cn.iocoder.yudao.module.content.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * Content 错误码枚举类
 * 
 * content 系统，使用 1-005-000-000 段
 */
public interface ErrorCodeConstants {

    // ========== 内容相关 1-005-001-000 ==========
    ErrorCode CONTENT_NOT_EXISTS = new ErrorCode(1_005_001_000, "内容不存在");
    ErrorCode CONTENT_ALREADY_DELETED = new ErrorCode(1_005_001_001, "内容已被删除");
    ErrorCode CONTENT_ACCESS_DENIED = new ErrorCode(1_005_001_002, "无权限访问该内容");
    ErrorCode CONTENT_AUDIT_FAILED = new ErrorCode(1_005_001_003, "内容审核未通过");
    ErrorCode CONTENT_TITLE_TOO_LONG = new ErrorCode(1_005_001_004, "标题长度不能超过100个字符");
    ErrorCode CONTENT_TEXT_TOO_LONG = new ErrorCode(1_005_001_005, "内容长度不能超过2000个字符");
    ErrorCode CONTENT_IMAGES_TOO_MANY = new ErrorCode(1_005_001_006, "图片数量不能超过9张");
    ErrorCode CONTENT_PUBLISH_LIMIT = new ErrorCode(1_005_001_007, "今日发布次数已达上限");
    ErrorCode CONTENT_DUPLICATE_SUBMIT = new ErrorCode(1_005_001_008, "请勿重复提交");
    ErrorCode CONTENT_AUTHOR_NOT_EXISTS = new ErrorCode(1_005_001_009, "作者不存在");

    // ========== 话题相关 1-005-002-000 ==========
    ErrorCode TOPIC_NOT_EXISTS = new ErrorCode(1_005_002_000, "话题不存在");
    ErrorCode TOPIC_NAME_EXISTS = new ErrorCode(1_005_002_001, "话题名称已存在");
    ErrorCode TOPIC_DISABLED = new ErrorCode(1_005_002_002, "话题已被禁用");
    ErrorCode TOPIC_NAME_TOO_LONG = new ErrorCode(1_005_002_003, "话题名称长度不能超过50个字符");

    // ========== 互动相关 1-005-003-000 ==========
    ErrorCode INTERACTION_DUPLICATE = new ErrorCode(1_005_003_000, "重复操作");
    ErrorCode INTERACTION_SELF_NOT_ALLOWED = new ErrorCode(1_005_003_001, "不能对自己的内容进行此操作");
    ErrorCode LIKE_FREQUENCY_LIMIT = new ErrorCode(1_005_003_002, "点赞操作过于频繁，请稍后再试");
    ErrorCode SHARE_FREQUENCY_LIMIT = new ErrorCode(1_005_003_003, "分享操作过于频繁，请稍后再试");

    // ========== 评论相关 1-005-004-000 ==========
    ErrorCode COMMENT_NOT_EXISTS = new ErrorCode(1_005_004_000, "评论不存在");
    ErrorCode COMMENT_ALREADY_DELETED = new ErrorCode(1_005_004_001, "评论已被删除");
    ErrorCode COMMENT_ACCESS_DENIED = new ErrorCode(1_005_004_002, "无权限操作该评论");
    ErrorCode COMMENT_CONTENT_EMPTY = new ErrorCode(1_005_004_003, "评论内容不能为空");
    ErrorCode COMMENT_CONTENT_TOO_LONG = new ErrorCode(1_005_004_004, "评论内容长度不能超过500个字符");
    ErrorCode COMMENT_NOT_ALLOWED = new ErrorCode(1_005_004_005, "该内容不允许评论");
    ErrorCode COMMENT_AUDIT_FAILED = new ErrorCode(1_005_004_006, "评论审核未通过");

    // ========== 举报相关 1-005-005-000 ==========
    ErrorCode REPORT_DUPLICATE = new ErrorCode(1_005_005_000, "您已举报过该内容");
    ErrorCode REPORT_REASON_INVALID = new ErrorCode(1_005_005_001, "举报原因不正确");
    ErrorCode REPORT_SELF_NOT_ALLOWED = new ErrorCode(1_005_005_002, "不能举报自己的内容");

    // ========== 搜索相关 1-005-006-000 ==========
    ErrorCode SEARCH_KEYWORD_EMPTY = new ErrorCode(1_005_006_000, "搜索关键词不能为空");
    ErrorCode SEARCH_KEYWORD_TOO_LONG = new ErrorCode(1_005_006_001, "搜索关键词长度不能超过50个字符");
    ErrorCode SEARCH_FREQUENCY_LIMIT = new ErrorCode(1_005_006_002, "搜索过于频繁，请稍后再试");

    // ========== 文件上传相关 1-005-007-000 ==========
    ErrorCode FILE_UPLOAD_FAILED = new ErrorCode(1_005_007_000, "文件上传失败");
    ErrorCode FILE_TYPE_NOT_SUPPORTED = new ErrorCode(1_005_007_001, "不支持的文件类型");
    ErrorCode FILE_SIZE_TOO_LARGE = new ErrorCode(1_005_007_002, "文件大小超出限制");
    ErrorCode IMAGE_FORMAT_ERROR = new ErrorCode(1_005_007_003, "图片格式错误");
    ErrorCode VIDEO_FORMAT_ERROR = new ErrorCode(1_005_007_004, "视频格式错误");
    ErrorCode AUDIO_FORMAT_ERROR = new ErrorCode(1_005_007_005, "音频格式错误");

    // ========== 热推相关 1-005-008-000 ==========
    ErrorCode BOOST_CONFIG_NOT_EXISTS = new ErrorCode(1_005_008_000, "推热配置不存在");
    ErrorCode BOOST_RECORD_NOT_EXISTS = new ErrorCode(1_005_008_001, "推热记录不存在");
    ErrorCode BOOST_RECORD_NOT_OWNER = new ErrorCode(1_005_008_002, "无权限操作该推热记录");
    ErrorCode BOOST_RECORD_NOT_ACTIVE = new ErrorCode(1_005_008_003, "推热记录不是生效状态");
    ErrorCode CONTENT_BOOST_ALREADY_ACTIVE = new ErrorCode(1_005_008_004, "该作品已有生效中的推热");
    ErrorCode CONTENT_NOT_OWNER = new ErrorCode(1_005_008_005, "不是作品所有者");
    ErrorCode BOOST_COIN_INSUFFICIENT = new ErrorCode(1_005_008_006, "币余额不足");
    ErrorCode BOOST_LEVEL_INVALID = new ErrorCode(1_005_008_007, "推热等级无效");
    // ========== 收藏相关 1-005-009-000 ==========
    ErrorCode FAVORITE_GROUP_NOT_EXISTS = new ErrorCode(1_005_009_000, "收藏分组不存在");

    // ========== 广告相关 1-005-010-000 ==========
    ErrorCode AD_NOT_EXISTS = new ErrorCode(1_005_010_000, "广告不存在");
    ErrorCode AD_ACCESS_DENIED = new ErrorCode(1_005_010_001, "无权限操作该广告");

}
