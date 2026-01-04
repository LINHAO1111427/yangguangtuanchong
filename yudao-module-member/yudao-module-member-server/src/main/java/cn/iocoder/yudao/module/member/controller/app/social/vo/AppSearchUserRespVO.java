package cn.iocoder.yudao.module.member.controller.app.social.vo;

import cn.iocoder.yudao.module.member.service.social.bo.MemberRelationSummary;
import cn.iocoder.yudao.module.member.service.social.bo.MemberSocialUserBO;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AppSearchUserRespVO {

    private Long userId;
    private Long customId;
    private String nickname;
    private String avatar;
    private String signature;
    private Boolean following;
    private Boolean follower;
    private Boolean mutualFollow;
    private Boolean blockedByMe;
    private Boolean blockedMe;
    private Integer followState;
    private Boolean needApproval;
    private Boolean canMessage;

    public static List<AppSearchUserRespVO> from(List<MemberSocialUserBO> list) {
        return list.stream().map(bo -> {
            AppSearchUserRespVO vo = new AppSearchUserRespVO();
            vo.setUserId(bo.getUserId());
            vo.setCustomId(bo.getCustomId());
            vo.setNickname(bo.getNickname());
            vo.setAvatar(bo.getAvatar());
            vo.setSignature(bo.getSignature());
            MemberRelationSummary relation = bo.getRelation();
            if (relation != null) {
                vo.setFollowing(relation.isFollowing());
                vo.setFollower(relation.isFollower());
                vo.setMutualFollow(relation.isMutualFollow());
                vo.setBlockedByMe(relation.isBlockedByMe());
                vo.setBlockedMe(relation.isBlockedMe());
                vo.setFollowState(relation.getFollowState());
                vo.setNeedApproval(relation.isNeedApproval());
                vo.setCanMessage(relation.isCanMessage());
            }
            return vo;
        }).toList();
    }
}
