package cn.iocoder.yudao.module.content.service.support;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Temporary in-memory follow storage for both users and topics.
 */
@Component
public class LocalFollowStorage {

    private final ConcurrentMap<Long, Set<Long>> userFollowMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, Set<Long>> topicFollowMap = new ConcurrentHashMap<>();

    public boolean toggleTopicFollow(Long userId, Long topicId) {
        if (userId == null || topicId == null) {
            return false;
        }
        Set<Long> followings = topicFollowMap.computeIfAbsent(userId, key -> ConcurrentHashMap.newKeySet());
        if (followings.add(topicId)) {
            return true;
        }
        followings.remove(topicId);
        if (followings.isEmpty()) {
            topicFollowMap.remove(userId, followings);
        }
        return false;
    }

    public boolean isTopicFollowed(Long userId, Long topicId) {
        Set<Long> followings = topicFollowMap.get(userId);
        return followings != null && followings.contains(topicId);
    }

    public List<Long> getFollowedTopicIds(Long userId) {
        Set<Long> followings = topicFollowMap.get(userId);
        if (followings == null || followings.isEmpty()) {
            return Collections.emptyList();
        }
        return new ArrayList<>(followings);
    }

    public boolean toggleUserFollow(Long userId, Long targetUserId) {
        if (userId == null || targetUserId == null || userId.equals(targetUserId)) {
            return false;
        }
        Set<Long> followings = userFollowMap.computeIfAbsent(userId, key -> ConcurrentHashMap.newKeySet());
        if (followings.add(targetUserId)) {
            return true;
        }
        followings.remove(targetUserId);
        if (followings.isEmpty()) {
            userFollowMap.remove(userId, followings);
        }
        return false;
    }

    public void followUser(Long userId, Long targetUserId) {
        if (userId == null || targetUserId == null || userId.equals(targetUserId)) {
            return;
        }
        userFollowMap.computeIfAbsent(userId, key -> ConcurrentHashMap.newKeySet()).add(targetUserId);
    }

    public boolean isFollowingUser(Long userId, Long targetUserId) {
        Set<Long> followings = userFollowMap.get(userId);
        return followings != null && followings.contains(targetUserId);
    }

    public List<Long> getFollowingUserIds(Long userId) {
        Set<Long> followings = userFollowMap.get(userId);
        if (followings == null || followings.isEmpty()) {
            return Collections.emptyList();
        }
        return new ArrayList<>(followings);
    }
}

