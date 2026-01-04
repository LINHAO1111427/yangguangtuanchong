package cn.iocoder.yudao.module.member.service.social;

import cn.iocoder.yudao.module.member.service.social.bo.MemberFriendRequestCreateBO;
import cn.iocoder.yudao.module.member.service.social.bo.MemberPotentialFriendBO;
import cn.iocoder.yudao.module.member.service.social.bo.MemberRelationSummary;
import cn.iocoder.yudao.module.member.service.social.bo.MemberSocialUserBO;

import java.util.List;
import java.util.Map;

/**
 * 社交关系服务
 *
 * @author sun
 */
public interface MemberRelationService {

    Long createRelationRequest(MemberFriendRequestCreateBO createBO);

    void approveRelation(Long requestId, Long operator);

    void rejectRelation(Long requestId, Long operator, String reason);

    void removeRelation(Long userId, Long targetUserId);

    void addToBlacklist(Long userId, Long targetUserId, String reason);

    void removeFromBlacklist(Long userId, Long targetUserId);

    List<MemberSocialUserBO> searchUsers(Long userId, String keyword, Integer limit);

    List<MemberPotentialFriendBO> getPotentialFriends(Long userId, Integer limit);

    Map<Long, MemberRelationSummary> getRelationSummary(Long userId, List<Long> targetUserIds);

    MemberRelationSummary getRelation(Long userId, Long targetUserId);

    /**
     * 获取关注列表
     *
     * @param userId 用户ID
     * @param pageNo 页码
     * @param pageSize 每页数量
     * @return 关注的用户列表
     */
    List<MemberSocialUserBO> getFollowingList(Long userId, Integer pageNo, Integer pageSize);

    /**
     * 获取粉丝列表
     *
     * @param userId 用户ID
     * @param pageNo 页码
     * @param pageSize 每页数量
     * @return 粉丝用户列表
     */
    List<MemberSocialUserBO> getFollowersList(Long userId, Integer pageNo, Integer pageSize);

}
