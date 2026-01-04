package cn.iocoder.yudao.module.content.service.support;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Simple in-memory storage for comment like relations.
 */
@Component
public class LocalCommentLikeStorage {

    private final ConcurrentMap<Long, Set<Long>> commentLikeUsers = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, Set<Long>> userLikeComments = new ConcurrentHashMap<>();

    public boolean toggleLike(Long commentId, Long userId) {
        if (commentId == null || userId == null) {
            return false;
        }
        Set<Long> users = commentLikeUsers.computeIfAbsent(commentId, key -> ConcurrentHashMap.newKeySet());
        Set<Long> comments = userLikeComments.computeIfAbsent(userId, key -> ConcurrentHashMap.newKeySet());
        if (users.add(userId)) {
            comments.add(commentId);
            return true;
        }
        users.remove(userId);
        if (users.isEmpty()) {
            commentLikeUsers.remove(commentId, users);
        }
        comments.remove(commentId);
        if (comments.isEmpty()) {
            userLikeComments.remove(userId, comments);
        }
        return false;
    }

    public boolean isLiked(Long commentId, Long userId) {
        if (commentId == null || userId == null) {
            return false;
        }
        Set<Long> users = commentLikeUsers.get(commentId);
        return users != null && users.contains(userId);
    }

    public Map<Long, Boolean> batchLiked(Collection<Long> commentIds, Long userId) {
        if (userId == null || commentIds == null || commentIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Set<Long> liked = userLikeComments.get(userId);
        if (liked == null || liked.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, Boolean> result = new HashMap<>(commentIds.size());
        for (Long id : commentIds) {
            result.put(id, liked.contains(id));
        }
        return result;
    }
}
