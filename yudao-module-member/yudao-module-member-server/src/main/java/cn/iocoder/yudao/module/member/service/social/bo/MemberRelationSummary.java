package cn.iocoder.yudao.module.member.service.social.bo;

import lombok.Data;

/**
 * 关系概况
 *
 * @author sun
 */
@Data
public class MemberRelationSummary {

    /**
     * 我是否已关注对方
     */
    private boolean following;
    /**
     * 对方是否关注我
     */
    private boolean follower;
    /**
     * 是否互相关注
     */
    private boolean mutualFollow;
    /**
     * 我是否已拉黑对方
     */
    private boolean blockedByMe;
    /**
     * 对方是否拉黑我
     */
    private boolean blockedMe;
    /**
     * 当前关注状态（0待审核、1通过、2拒绝）
     */
    private int followState;
    /**
     * 是否仍处于待审批
     */
    private boolean needApproval;
    /**
     * 是否允许发起沟通（双方未互相拉黑）
     */
    private boolean canMessage;

}
