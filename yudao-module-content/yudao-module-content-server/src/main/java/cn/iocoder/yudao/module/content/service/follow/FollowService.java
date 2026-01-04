package cn.iocoder.yudao.module.content.service.follow;

import java.util.List;

/**
 * 关注/取关服务。
 */
public interface FollowService {

    boolean followUser(Long followerId, Long targetId, String remark);

    boolean unfollowUser(Long followerId, Long targetId);

    List<Long> getFollowingUserIds(Long userId);

    List<Long> getFansUserIds(Long userId);

    Long countFollowing(Long userId);

    Long countFans(Long userId);

    boolean isFollowingUser(Long followerId, Long targetId);

    boolean followTopic(Long userId, Long topicId);

    boolean unfollowTopic(Long userId, Long topicId);

    boolean isTopicFollowed(Long userId, Long topicId);

    List<Long> getFollowedTopicIds(Long userId);
}
