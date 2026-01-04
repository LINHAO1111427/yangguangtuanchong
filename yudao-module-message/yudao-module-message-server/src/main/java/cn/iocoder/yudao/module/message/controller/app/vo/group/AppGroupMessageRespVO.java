package cn.iocoder.yudao.module.message.controller.app.vo.group;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "APP - 群聊消息 Response VO")
@Data
public class AppGroupMessageRespVO {

    @Schema(description = "消息 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "群组 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long groupId;

    @Schema(description = "发送者用户 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long fromUserId;

    @Schema(description = "发送者昵称", example = "小绿薯")
    private String senderNickname;

    @Schema(description = "发送者头像", example = "https://static.xiaolvshu.cn/avatar.png")
    private String senderAvatar;

    @Schema(description = "消息类型 1-文本 2-图片 3-视频 4-语音 5-文件 6-卡片 10-系统", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer type;

    @Schema(description = "消息内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "大家好")
    private String content;

    @Schema(description = "扩展数据(JSON)", example = "{\"mediaUrls\":[\"https://...\"],\"fileName\":\"a.png\"}")
    private String extraData;

    @Schema(description = "消息状态 0-正常 1-撤回 2-删除", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer status;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;
}

