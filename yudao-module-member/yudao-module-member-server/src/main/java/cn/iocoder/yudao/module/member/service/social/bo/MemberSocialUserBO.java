package cn.iocoder.yudao.module.member.service.social.bo;

import lombok.Data;

/**
 * 社交场景下的用户信息
 *
 * @author sun
 */
@Data
public class MemberSocialUserBO {

    private Long userId;
    private Long customId;
    private String nickname;
    private String avatar;
    private String signature;
    private MemberRelationSummary relation;

}
