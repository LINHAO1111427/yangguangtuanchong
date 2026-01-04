package cn.iocoder.yudao.module.member.controller.app.social.vo;

import cn.iocoder.yudao.module.member.service.social.bo.MemberSocialUserBO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Schema(description = "用户 APP - 社交用户信息 Response VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppSocialUserRespVO {

    @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "用户昵称", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋艿")
    private String nickname;

    @Schema(description = "用户头像", example = "https://www.iocoder.cn/xxx.png")
    private String avatar;

    @Schema(description = "个人简介", example = "这个人很懒，什么都没留下")
    private String bio;

    @Schema(description = "用户自定义ID", example = "xiaolvshu_001")
    private String customId;

    @Schema(description = "是否VIP", example = "false")
    private Boolean isVip;

    @Schema(description = "是否已关注", example = "true")
    private Boolean isFollowing;

    @Schema(description = "是否为好友", example = "false")
    private Boolean isFriend;

    @Schema(description = "关注时间", example = "2024-01-01 12:00:00")
    private LocalDateTime followTime;

    public static List<AppSocialUserRespVO> fromBOList(List<MemberSocialUserBO> boList) {
        if (boList == null) {
            return List.of();
        }
        return boList.stream().map(bo -> {
            AppSocialUserRespVO vo = new AppSocialUserRespVO();
            vo.setId(bo.getUserId());
            vo.setNickname(bo.getNickname());
            vo.setAvatar(bo.getAvatar());
            vo.setBio(bo.getSignature());
            vo.setCustomId(bo.getCustomId() != null ? String.valueOf(bo.getCustomId()) : null);
            if (bo.getRelation() != null) {
                vo.setIsFollowing(bo.getRelation().isFollowing());
                vo.setIsFriend(bo.getRelation().isMutualFollow());
            }
            return vo;
        }).collect(Collectors.toList());
    }

}
