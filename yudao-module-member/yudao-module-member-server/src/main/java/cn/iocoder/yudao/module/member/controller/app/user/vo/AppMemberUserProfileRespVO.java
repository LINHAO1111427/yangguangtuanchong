package cn.iocoder.yudao.module.member.controller.app.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "用户 APP - 用户详情（含统计数据）Response VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppMemberUserProfileRespVO {

    @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "用户昵称", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋艿")
    private String nickname;

    @Schema(description = "用户头像", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.iocoder.cn/xxx.png")
    private String avatar;

    @Schema(description = "用户手机号", example = "156****1300")
    private String mobile;

    @Schema(description = "用户性别", example = "1")
    private Integer sex;

    @Schema(description = "生日（yyyy-MM-dd）", example = "2025-01-01")
    private String birthday;

    @Schema(description = "个人简介", example = "这个人很懒，什么都没留下")
    private String bio;

    @Schema(description = "背景图 URL", example = "https://example.com/bg.png")
    private String backgroundUrl;

    @Schema(description = "地区（展示用）", example = "上海市 上海市 浦东新区")
    private String region;

    @Schema(description = "职业", example = "程序员")
    private String occupation;

    @Schema(description = "学校", example = "XX大学")
    private String school;

    @Schema(description = "原创认证状态：0未认证 1认证中 2已认证 3拒绝", example = "0")
    private Integer originalVerifyStatus;

    @Schema(description = "用户自定义ID", example = "xiaolvshu_001")
    private String customId;

    @Schema(description = "是否VIP", example = "false")
    private Boolean isVip;

    @Schema(description = "积分", example = "10")
    private Integer point;

    @Schema(description = "经验值", example = "1024")
    private Integer experience;

    @Schema(description = "用户等级")
    private AppMemberUserInfoRespVO.Level level;

    @Schema(description = "统计数据")
    private Stats stats;

    @Schema(description = "用户 App - 统计数据")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Stats {

        @Schema(description = "获赞数", example = "100")
        private Long likesCount;

        @Schema(description = "好友数", example = "50")
        private Long friendsCount;

        @Schema(description = "关注数", example = "80")
        private Long followingCount;

        @Schema(description = "粉丝数", example = "200")
        private Long followersCount;

        @Schema(description = "作品数", example = "30")
        private Long worksCount;

        @Schema(description = "想要数", example = "15")
        private Long wishlistCount;

        @Schema(description = "足迹数", example = "120")
        private Long footprintCount;

        @Schema(description = "评论数", example = "30")
        private Long commentCount;

    }

}
