package cn.iocoder.yudao.module.member.service.social.bo;

import lombok.Data;

/**
 * 关注 / 好友申请 BO
 *
 * @author sun
 */
@Data
public class MemberFriendRequestCreateBO {

    private Long userId;
    private Long targetUserId;
    private Integer relationType;
    private Integer source;
    private String requestMessage;

}
