package cn.iocoder.yudao.module.member.controller.app.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Schema(description = "用户 App - 会员用户更新 Request VO")
@Data
public class AppMemberUserUpdateReqVO {

    @Schema(description = "用户昵称", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    private String nickname;

    @Schema(description = "头像", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.iocoder.cn/x.png")
    @URL(message = "头像必须是 URL 格式")
    private String avatar;

    @Schema(description = "性别", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer sex;

    @Schema(description = "个人简介（对应 member_user.mark）", example = "这个人很懒，什么都没留下")
    private String mark;

    @Schema(description = "生日（yyyy-MM-dd）", example = "2025-01-01")
    private String birthday;

    @Schema(description = "背景图 URL", example = "https://example.com/bg.png")
    @URL(message = "背景图必须是 URL 格式")
    private String backgroundUrl;

    @Schema(description = "地区（展示用）", example = "上海市 上海市 浦东新区")
    private String region;

    @Schema(description = "职业", example = "程序员")
    private String occupation;

    @Schema(description = "学校", example = "XX大学")
    private String school;

}
