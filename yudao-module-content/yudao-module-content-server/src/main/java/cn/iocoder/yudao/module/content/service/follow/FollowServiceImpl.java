package cn.iocoder.yudao.module.content.service.follow;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.module.content.dal.dataobject.TopicFollowDO;
import cn.iocoder.yudao.module.content.dal.dataobject.UserFollowDO;
import cn.iocoder.yudao.module.content.dal.mysql.TopicFollowMapper;
import cn.iocoder.yudao.module.content.dal.mysql.UserFollowMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 关注/取关持久化实现。
 */
@Service
public class FollowServiceImpl implements FollowService {

    private static final short STATUS_ACTIVE = 0;
    private static final short STATUS_CANCEL = 1;

    @Resource
    private UserFollowMapper userFollowMapper;
    @Resource
    private TopicFollowMapper topicFollowMapper;

    @Override
    public boolean followUser(Long followerId, Long targetId, String remark) {
        validateUserIds(followerId, targetId);
        UserFollowDO existing = userFollowMapper.selectOne(followerId, targetId);
        if (existing != null && Objects.equals(existing.getStatus(), STATUS_ACTIVE) && Boolean.FALSE.equals(existing.getDeleted())) {
            return true;
        }
        if (existing == null) {
            UserFollowDO record = new UserFollowDO();
            record.setFollowerId(followerId);
            record.setTargetId(targetId);
            record.setStatus(STATUS_ACTIVE);
            record.setSource((short) 0);
            record.setRemark(remark);
            record.setDeleted(0);
            userFollowMapper.insert(record);
            return true;
        }
        existing.setStatus(STATUS_ACTIVE);
        existing.setDeleted(0);
        existing.setUpdateTime(LocalDateTime.now());
        existing.setRemark(remark);
        userFollowMapper.updateById(existing);
        return true;
    }

    @Override
    public boolean unfollowUser(Long followerId, Long targetId) {
        validateUserIds(followerId, targetId);
        UserFollowDO existing = userFollowMapper.selectOne(followerId, targetId);
        if (existing == null) {
            return false;
        }
        existing.setStatus(STATUS_CANCEL);
        existing.setDeleted(0);
        existing.setUpdateTime(LocalDateTime.now());
        userFollowMapper.updateById(existing);
        return true;
    }

    @Override
    public List<Long> getFollowingUserIds(Long userId) {
        if (userId == null) {
            return List.of();
        }
        List<UserFollowDO> list = userFollowMapper.selectList(
                UserFollowDO::getFollowerId, userId);
        if (CollUtil.isEmpty(list)) {
            return List.of();
        }
        return list.stream()
                .filter(r -> Boolean.FALSE.equals(r.getDeleted()) && ObjectUtil.equal(r.getStatus(), STATUS_ACTIVE))
                .map(UserFollowDO::getTargetId)
                .collect(Collectors.toList());
    }

    @Override
    public List<Long> getFansUserIds(Long userId) {
        if (userId == null) {
            return List.of();
        }
        List<UserFollowDO> list = userFollowMapper.selectList(
                UserFollowDO::getTargetId, userId);
        if (CollUtil.isEmpty(list)) {
            return List.of();
        }
        return list.stream()
                .filter(r -> Boolean.FALSE.equals(r.getDeleted()) && ObjectUtil.equal(r.getStatus(), STATUS_ACTIVE))
                .map(UserFollowDO::getFollowerId)
                .collect(Collectors.toList());
    }

    @Override
    public Long countFollowing(Long userId) {
        return userId == null ? 0L : userFollowMapper.countFollowing(userId);
    }

    @Override
    public Long countFans(Long userId) {
        return userId == null ? 0L : userFollowMapper.countFans(userId);
    }

    @Override
    public boolean isFollowingUser(Long followerId, Long targetId) {
        if (followerId == null || targetId == null || Objects.equals(followerId, targetId)) {
            return false;
        }
        UserFollowDO existing = userFollowMapper.selectOne(followerId, targetId);
        return existing != null && Boolean.FALSE.equals(existing.getDeleted()) && ObjectUtil.equal(existing.getStatus(), STATUS_ACTIVE);
    }

    @Override
    public boolean followTopic(Long userId, Long topicId) {
        Assert.notNull(userId, "userId不能为空");
        Assert.notNull(topicId, "topicId不能为空");
        TopicFollowDO existing = topicFollowMapper.selectOne(userId, topicId);
        if (existing != null && ObjectUtil.equal(existing.getStatus(), STATUS_ACTIVE) && Boolean.FALSE.equals(existing.getDeleted())) {
            return true;
        }
        if (existing == null) {
            TopicFollowDO record = new TopicFollowDO();
            record.setUserId(userId);
            record.setTopicId(topicId);
            record.setStatus(STATUS_ACTIVE);
            record.setDeleted(0);
            topicFollowMapper.insert(record);
            return true;
        }
        existing.setStatus(STATUS_ACTIVE);
        existing.setDeleted(0);
        existing.setUpdateTime(LocalDateTime.now());
        topicFollowMapper.updateById(existing);
        return true;
    }

    @Override
    public boolean unfollowTopic(Long userId, Long topicId) {
        if (userId == null || topicId == null) {
            return false;
        }
        TopicFollowDO existing = topicFollowMapper.selectOne(userId, topicId);
        if (existing == null) {
            return false;
        }
        existing.setStatus(STATUS_CANCEL);
        existing.setDeleted(0);
        existing.setUpdateTime(LocalDateTime.now());
        topicFollowMapper.updateById(existing);
        return true;
    }

    @Override
    public boolean isTopicFollowed(Long userId, Long topicId) {
        if (userId == null || topicId == null) {
            return false;
        }
        TopicFollowDO existing = topicFollowMapper.selectOne(userId, topicId);
        return existing != null && Boolean.FALSE.equals(existing.getDeleted()) && ObjectUtil.equal(existing.getStatus(), STATUS_ACTIVE);
    }

    @Override
    public List<Long> getFollowedTopicIds(Long userId) {
        if (userId == null) {
            return List.of();
        }
        List<TopicFollowDO> list = topicFollowMapper.selectByUser(userId, 0, Integer.MAX_VALUE);
        if (CollUtil.isEmpty(list)) {
            return new ArrayList<>();
        }
        return list.stream()
                .filter(r -> Boolean.FALSE.equals(r.getDeleted()) && ObjectUtil.equal(r.getStatus(), STATUS_ACTIVE))
                .map(TopicFollowDO::getTopicId)
                .collect(Collectors.toList());
    }

    private void validateUserIds(Long followerId, Long targetId) {
        Assert.notNull(followerId, "followerId不能为空");
        Assert.notNull(targetId, "targetId不能为空");
        if (Objects.equals(followerId, targetId)) {
            throw new IllegalArgumentException("不能关注自己");
        }
    }
}
