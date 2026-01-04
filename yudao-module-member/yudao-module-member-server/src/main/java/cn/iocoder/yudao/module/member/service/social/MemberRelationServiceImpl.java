package cn.iocoder.yudao.module.member.service.social;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.RandomUtil;
import cn.iocoder.yudao.module.member.dal.dataobject.social.MemberBlacklistDO;
import cn.iocoder.yudao.module.member.dal.dataobject.social.MemberContactStatDO;
import cn.iocoder.yudao.module.member.dal.dataobject.social.MemberFriendRelationDO;
import cn.iocoder.yudao.module.member.dal.mysql.social.MemberBlacklistMapper;
import cn.iocoder.yudao.module.member.dal.mysql.social.MemberContactStatMapper;
import cn.iocoder.yudao.module.member.dal.mysql.social.MemberFriendRelationMapper;
import cn.iocoder.yudao.module.member.dal.mysql.user.MemberUserMapper;
import cn.iocoder.yudao.module.member.framework.kafka.MemberBehaviorEventProducer;
import cn.iocoder.yudao.module.member.service.social.bo.MemberFriendRequestCreateBO;
import cn.iocoder.yudao.module.member.service.social.bo.MemberPotentialFriendBO;
import cn.iocoder.yudao.module.member.service.social.bo.MemberRelationSummary;
import cn.iocoder.yudao.module.member.service.social.bo.MemberSocialUserBO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.member.enums.ErrorCodeConstants.*;

/**
 * 简化实现的社交关系服务：
 * - 以单表 + 轻量逻辑支撑 App 层调用，避免接口报错
 * - 允许后续根据真实业务补充推荐/黑名单等能力
 *
 * @author sun
 */
@Service
public class MemberRelationServiceImpl implements MemberRelationService {

    private static final AtomicLong REQUEST_ID = new AtomicLong(10_0000L);

    @Resource
    private MemberFriendRelationMapper relationMapper;
    @Resource
    private MemberBlacklistMapper blacklistMapper;
    @Resource
    private MemberContactStatMapper contactStatMapper;
    @Resource
    private MemberUserMapper memberUserMapper;
    @Resource
    private MemberBehaviorEventProducer behaviorEventProducer;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createRelationRequest(MemberFriendRequestCreateBO createBO) {
        Long userId = createBO.getUserId();
        Long targetUserId = createBO.getTargetUserId();
        if (Objects.equals(userId, targetUserId)) {
            throw exception(RELATION_SELF_NOT_ALLOWED);
        }
        if (!userExists(targetUserId)) {
            throw exception(USER_NOT_EXISTS);
        }
        if (isBlacklisted(userId, targetUserId)) {
            throw exception(RELATION_TARGET_IN_BLACKLIST);
        }
        if (isBlacklisted(targetUserId, userId)) {
            throw exception(RELATION_BLOCKED_BY_TARGET);
        }
        int relationType = ObjUtil.defaultIfNull(createBO.getRelationType(), 0);
        boolean autoApprove = relationType == 0; // 0=关注：不需要对方同意
        int desiredState = autoApprove ? 1 : 0;
        MemberFriendRelationDO relation = relationMapper.selectByUserIdAndFriendId(userId, targetUserId);
        Integer previousState = relation != null ? relation.getState() : null;
        if (relation == null) {
            relation = new MemberFriendRelationDO();
            relation.setId(REQUEST_ID.incrementAndGet());
            relation.setUserId(userId);
            relation.setFriendId(targetUserId);
            relation.setRelationType(relationType);
            relation.setSource(ObjUtil.defaultIfNull(createBO.getSource(), 0));
            relation.setRequestMessage(createBO.getRequestMessage());
            relation.setState(desiredState);
            relation.setLastActionAt(LocalDateTime.now());
            relationMapper.insert(relation);
        } else {
            relation.setRelationType(relationType);
            relation.setSource(ObjUtil.defaultIfNull(createBO.getSource(), relation.getSource()));
            relation.setRequestMessage(createBO.getRequestMessage());
            relation.setState(desiredState);
            relation.setLastActionAt(LocalDateTime.now());
            relationMapper.updateById(relation);
        }

        // 关注：生成“新增关注”通知（仅在从未关注 -> 已关注时触发）
        if (autoApprove && (previousState == null || previousState != 1)) {
            behaviorEventProducer.sendFollowEvent(userId, targetUserId);
        }
        return relation.getId();
    }

    @Override
    public void approveRelation(Long requestId, Long operator) {
        MemberFriendRelationDO relation = relationMapper.selectById(requestId);
        if (relation == null) {
            throw exception(RELATION_REQUEST_NOT_EXISTS);
        }
        if (!Objects.equals(relation.getFriendId(), operator)) {
            throw exception(RELATION_REQUEST_NOT_EXISTS);
        }
        relation.setState(1);
        relation.setLastActionAt(LocalDateTime.now());
        relationMapper.updateById(relation);
    }

    @Override
    public void rejectRelation(Long requestId, Long operator, String reason) {
        MemberFriendRelationDO relation = relationMapper.selectById(requestId);
        if (relation == null) {
            throw exception(RELATION_REQUEST_NOT_EXISTS);
        }
        if (!Objects.equals(relation.getFriendId(), operator)) {
            throw exception(RELATION_REQUEST_NOT_EXISTS);
        }
        relation.setState(2);
        relation.setRemark(reason);
        relation.setLastActionAt(LocalDateTime.now());
        relationMapper.updateById(relation);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeRelation(Long userId, Long targetUserId) {
        if (relationMapper.deleteByUserIdAndFriendId(userId, targetUserId) == 0
                && relationMapper.deleteByUserIdAndFriendId(targetUserId, userId) == 0) {
            throw exception(RELATION_REQUEST_NOT_EXISTS);
        }
    }

    @Override
    public void addToBlacklist(Long userId, Long targetUserId, String reason) {
        if (Objects.equals(userId, targetUserId)) {
            throw exception(RELATION_SELF_NOT_ALLOWED);
        }
        MemberBlacklistDO exist = blacklistMapper.selectByUserIdAndTargetId(userId, targetUserId);
        if (exist != null) {
            exist.setReason(reason);
            blacklistMapper.updateById(exist);
            return;
        }
        MemberBlacklistDO record = new MemberBlacklistDO();
        record.setId(REQUEST_ID.incrementAndGet());
        record.setUserId(userId);
        record.setTargetId(targetUserId);
        record.setReason(reason);
        blacklistMapper.insert(record);
    }

    @Override
    public void removeFromBlacklist(Long userId, Long targetUserId) {
        blacklistMapper.deleteByUserIdAndTargetId(userId, targetUserId);
    }

    @Override
    public List<MemberSocialUserBO> searchUsers(Long userId, String keyword, Integer limit) {
        // 复用 MemberUserMapper 的搜索能力
        List<MemberSocialUserBO> list = memberUserMapper.searchSocialUsers(keyword, ObjUtil.defaultIfNull(limit, 20));
        Map<Long, MemberRelationSummary> summaries = getRelationSummary(userId,
                list.stream().map(MemberSocialUserBO::getUserId).toList());
        list.forEach(item -> item.setRelation(summaries.getOrDefault(item.getUserId(), new MemberRelationSummary())));
        return list;
    }

    @Override
    public List<MemberPotentialFriendBO> getPotentialFriends(Long userId, Integer limit) {
        int size = ObjUtil.defaultIfNull(limit, 20);
        List<MemberContactStatDO> stats = contactStatMapper.selectListByUserId(userId, size);
        if (CollUtil.isEmpty(stats)) {
            return Collections.emptyList();
        }
        return stats.stream().map(stat -> {
            MemberPotentialFriendBO bo = new MemberPotentialFriendBO();
            bo.setUserId(userId);
            bo.setTargetUserId(stat.getTargetUserId());
            bo.setSameCity(Boolean.TRUE.equals(stat.getSameCity()));
            bo.setSameDistrict(Boolean.TRUE.equals(stat.getSameDistrict()));
            bo.setNearDistanceCount(stat.getNearDistanceCount());
            bo.setSameWifiCount(stat.getSameWifiCount());
            bo.setSameIpCount(stat.getSameIpCount());
            bo.setScore(stat.getScore());
            return bo;
        }).collect(Collectors.toList());
    }

    @Override
    public Map<Long, MemberRelationSummary> getRelationSummary(Long userId, List<Long> targetUserIds) {
        if (CollUtil.isEmpty(targetUserIds)) {
            return Collections.emptyMap();
        }
        List<MemberFriendRelationDO> myRelations = relationMapper.selectListByUserIdAndFriendIds(userId, targetUserIds);
        List<MemberFriendRelationDO> friendRelations = relationMapper.selectListByFriendIdAndUserIds(userId, targetUserIds);
        List<MemberBlacklistDO> myBlacklist = blacklistMapper.selectListByUserIdAndTargetIds(userId, targetUserIds);
        List<MemberBlacklistDO> targetBlack = blacklistMapper.selectListByUserIdsAndTargetId(targetUserIds, userId);

        Map<Long, MemberRelationSummary> summaryMap = new HashMap<>(targetUserIds.size());
        for (Long targetId : targetUserIds) {
            MemberRelationSummary summary = new MemberRelationSummary();
            summary.setFollowState(2);
            summaryMap.put(targetId, summary);
        }
        for (MemberFriendRelationDO relation : myRelations) {
            MemberRelationSummary summary = summaryMap.get(relation.getFriendId());
            if (summary != null) {
                summary.setFollowing(relation.getState() == 1);
                summary.setFollowState(relation.getState());
                summary.setNeedApproval(relation.getState() == 0);
            }
        }
        for (MemberFriendRelationDO relation : friendRelations) {
            MemberRelationSummary summary = summaryMap.get(relation.getUserId());
            if (summary != null) {
                boolean follower = relation.getState() == 1;
                summary.setFollower(follower);
                summary.setMutualFollow(summary.isFollowing() && follower);
            }
        }
        myBlacklist.forEach(record -> {
            MemberRelationSummary summary = summaryMap.get(record.getTargetId());
            if (summary != null) {
                summary.setBlockedByMe(true);
            }
        });
        targetBlack.forEach(record -> {
            MemberRelationSummary summary = summaryMap.get(record.getUserId());
            if (summary != null) {
                summary.setBlockedMe(true);
            }
        });
        summaryMap.values().forEach(summary ->
                summary.setCanMessage(!summary.isBlockedByMe() && !summary.isBlockedMe()));
        return summaryMap;
    }

    @Override
    public MemberRelationSummary getRelation(Long userId, Long targetUserId) {
        return getRelationSummary(userId, List.of(targetUserId))
                .getOrDefault(targetUserId, new MemberRelationSummary());
    }

    private boolean userExists(Long userId) {
        return memberUserMapper.selectById(userId) != null;
    }

    private boolean isBlacklisted(Long userId, Long targetId) {
        return blacklistMapper.selectByUserIdAndTargetId(userId, targetId) != null;
    }

    @Override
    public List<MemberSocialUserBO> getFollowingList(Long userId, Integer pageNo, Integer pageSize) {
        int offset = (Math.max(1, ObjUtil.defaultIfNull(pageNo, 1)) - 1) * ObjUtil.defaultIfNull(pageSize, 20);
        int limit = ObjUtil.defaultIfNull(pageSize, 20);

        List<MemberFriendRelationDO> relations = relationMapper.selectFollowingList(userId, offset, limit);
        if (CollUtil.isEmpty(relations)) {
            return Collections.emptyList();
        }

        List<Long> friendIds = relations.stream().map(MemberFriendRelationDO::getFriendId).toList();
        Map<Long, MemberRelationSummary> relationMap = getRelationSummary(userId, friendIds);

        return memberUserMapper.selectBatchIds(friendIds).stream()
                .map(user -> {
                    MemberSocialUserBO bo = new MemberSocialUserBO();
                    bo.setUserId(user.getId());
                    bo.setNickname(user.getNickname());
                    bo.setAvatar(user.getAvatar());
                    bo.setRelation(relationMap.getOrDefault(user.getId(), new MemberRelationSummary()));
                    return bo;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<MemberSocialUserBO> getFollowersList(Long userId, Integer pageNo, Integer pageSize) {
        int offset = (Math.max(1, ObjUtil.defaultIfNull(pageNo, 1)) - 1) * ObjUtil.defaultIfNull(pageSize, 20);
        int limit = ObjUtil.defaultIfNull(pageSize, 20);

        List<MemberFriendRelationDO> relations = relationMapper.selectFollowersList(userId, offset, limit);
        if (CollUtil.isEmpty(relations)) {
            return Collections.emptyList();
        }

        List<Long> followerIds = relations.stream().map(MemberFriendRelationDO::getUserId).toList();
        Map<Long, MemberRelationSummary> relationMap = getRelationSummary(userId, followerIds);

        return memberUserMapper.selectBatchIds(followerIds).stream()
                .map(user -> {
                    MemberSocialUserBO bo = new MemberSocialUserBO();
                    bo.setUserId(user.getId());
                    bo.setNickname(user.getNickname());
                    bo.setAvatar(user.getAvatar());
                    bo.setRelation(relationMap.getOrDefault(user.getId(), new MemberRelationSummary()));
                    return bo;
                })
                .collect(Collectors.toList());
    }
}
