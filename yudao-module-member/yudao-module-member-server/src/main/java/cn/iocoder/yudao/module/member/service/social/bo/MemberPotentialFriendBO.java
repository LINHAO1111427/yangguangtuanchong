package cn.iocoder.yudao.module.member.service.social.bo;

import lombok.Data;

/**
 * 可能认识的人 BO
 *
 * @author sun
 */
@Data
public class MemberPotentialFriendBO {

    private Long userId;
    private Long targetUserId;
    private boolean sameCity;
    private boolean sameDistrict;
    private Integer nearDistanceCount;
    private Integer sameWifiCount;
    private Integer sameIpCount;
    private Double score;

}
