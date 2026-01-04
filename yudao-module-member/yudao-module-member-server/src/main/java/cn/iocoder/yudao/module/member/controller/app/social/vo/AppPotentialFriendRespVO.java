package cn.iocoder.yudao.module.member.controller.app.social.vo;

import cn.iocoder.yudao.module.member.dal.dataobject.user.MemberUserDO;
import cn.iocoder.yudao.module.member.service.social.bo.MemberPotentialFriendBO;
import cn.iocoder.yudao.module.member.service.social.bo.MemberRelationSummary;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class AppPotentialFriendRespVO {

    private Long userId;
    private Long targetUserId;
    private String nickname;
    private String avatar;
    private Double score;
    private Boolean sameCity;
    private Boolean sameDistrict;
    private Integer nearDistanceCount;
    private Integer sameWifiCount;
    private Integer sameIpCount;
    private Boolean following;
    private Boolean follower;
    private Boolean mutualFollow;

    public static List<AppPotentialFriendRespVO> from(List<MemberPotentialFriendBO> list,
                                                     List<MemberUserDO> users,
                                                     Map<Long, MemberRelationSummary> relationMap) {
        Map<Long, MemberUserDO> userMap = users.stream()
                .collect(Collectors.toMap(MemberUserDO::getId, u -> u, (a, b) -> a));
        return list.stream().map(bo -> {
            AppPotentialFriendRespVO vo = new AppPotentialFriendRespVO();
            vo.setUserId(bo.getUserId());
            vo.setTargetUserId(bo.getTargetUserId());
            vo.setScore(bo.getScore());
            vo.setSameCity(bo.isSameCity());
            vo.setSameDistrict(bo.isSameDistrict());
            vo.setNearDistanceCount(bo.getNearDistanceCount());
            vo.setSameWifiCount(bo.getSameWifiCount());
            vo.setSameIpCount(bo.getSameIpCount());
            MemberUserDO target = userMap.get(bo.getTargetUserId());
            if (target != null) {
                vo.setNickname(target.getNickname());
                vo.setAvatar(target.getAvatar());
            }
            MemberRelationSummary summary = relationMap.get(bo.getTargetUserId());
            if (summary != null) {
                vo.setFollowing(summary.isFollowing());
                vo.setFollower(summary.isFollower());
                vo.setMutualFollow(summary.isMutualFollow());
            }
            return vo;
        }).toList();
    }
}
