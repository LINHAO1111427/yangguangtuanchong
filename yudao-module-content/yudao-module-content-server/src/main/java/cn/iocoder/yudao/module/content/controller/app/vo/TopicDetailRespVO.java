package cn.iocoder.yudao.module.content.controller.app.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "用户 APP - 话题详情 Response VO")
@Data
public class TopicDetailRespVO {

    @Schema(description = "话题ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "话题名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "美食")
    private String name;

    @Schema(description = "话题描述", example = "分享美食心得，记录生活美好")
    private String description;

    @Schema(description = "话题图标", example = "https://example.com/icon.png")
    private String icon;

    @Schema(description = "话题颜色", example = "#FF6B6B")
    private String color;

    @Schema(description = "话题类型：1-官方话题，2-用户话题", example = "1")
    private Integer type;

    @Schema(description = "状态：0-禁用，1-启用", example = "1")
    private Integer status;

    @Schema(description = "是否推荐：0-不推荐，1-推荐", example = "1")
    private Integer isRecommend;

    @Schema(description = "排序值", example = "100")
    private Integer sort;

    @Schema(description = "内容数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1500")
    private Integer contentCount;

    @Schema(description = "今日新增内容数", example = "50")
    private Integer todayContentCount;

    @Schema(description = "参与用户数", requiredMode = Schema.RequiredMode.REQUIRED, example = "800")
    private Integer participantCount;

    @Schema(description = "热度分数", example = "85.5")
    private Double hotScore;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime createTime;

    @Schema(description = "是否已关注", example = "false")
    private Boolean isFollowed;

    @Schema(description = "话题标签", example = "热门")
    private String tag;

    @Schema(description = "话题规则")
    private String rules;

    @Schema(description = "相关话题列表")
    private List<TopicRespVO> relatedTopics;

    @Schema(description = "热门内容列表（前几条）")
    private List<ContentListRespVO> hotContents;

    @Schema(description = "近期参与用户（头像列表）")
    private List<UserAvatar> recentParticipants;

    @Schema(description = "用户头像信息")
    @Data
    public static class UserAvatar {
        @Schema(description = "用户ID", example = "1024")
        private Long userId;

        @Schema(description = "头像", example = "https://example.com/avatar.jpg")
        private String avatar;

        @Schema(description = "用户名", example = "阳光团宠用户")
        private String nickname;
    }
}