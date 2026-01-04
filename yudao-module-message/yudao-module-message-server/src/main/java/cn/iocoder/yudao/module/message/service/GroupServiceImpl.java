package cn.iocoder.yudao.module.message.service;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import cn.iocoder.yudao.module.message.constants.GroupConstants;
import cn.iocoder.yudao.module.message.controller.app.vo.group.*;
import cn.iocoder.yudao.module.message.dal.dataobject.GroupInfoDO;
import cn.iocoder.yudao.module.message.dal.dataobject.GroupMemberDO;
import cn.iocoder.yudao.module.message.dal.dataobject.GroupMessageDO;
import cn.iocoder.yudao.module.message.dal.mapper.GroupInfoMapper;
import cn.iocoder.yudao.module.message.dal.mapper.GroupMemberMapper;
import cn.iocoder.yudao.module.message.dal.mapper.GroupMessageMapper;
import cn.iocoder.yudao.module.message.kafka.MessageProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.module.message.constants.GroupConstants.*;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.UNAUTHORIZED;
import static cn.iocoder.yudao.module.message.enums.ErrorCodeConstants.*;


/**
 * 群组Service实现
 *
 * @author xiaolvshu
 */
@Slf4j
@Service
public class GroupServiceImpl implements GroupService {

    @Resource
    private GroupInfoMapper groupInfoMapper;

    @Resource
    private GroupMemberMapper groupMemberMapper;

    @Resource
    private GroupMessageMapper groupMessageMapper;

    @Resource
    private MessageProducer messageProducer;

    @Resource
    private MemberUserApi memberUserApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createGroup(GroupCreateReqVO reqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        if (userId == null) {
            throw ServiceExceptionUtil.exception(UNAUTHORIZED);
        }

        // 验证群名�?
        if (StrUtil.isBlank(reqVO.getGroupName())) {
            throw ServiceExceptionUtil.exception(GROUP_NAME_REQUIRED);
        }

        // 创建群组
        GroupInfoDO group = new GroupInfoDO();
        group.setGroupName(reqVO.getGroupName());
        group.setAvatar(reqVO.getAvatar());
        group.setOwnerUserId(userId);
        group.setDescription(reqVO.getDescription());
        group.setMemberCount(1); // 初始成员数为1(群主)
        group.setMaxMemberCount(reqVO.getMaxMemberCount() != null ? reqVO.getMaxMemberCount() : DEFAULT_MAX_MEMBER_COUNT);
        group.setJoinType(reqVO.getJoinType() != null ? reqVO.getJoinType() : DEFAULT_JOIN_TYPE_FREE);
        group.setStatus(GROUP_STATUS_ACTIVE);
        group.setMuteAll(MUTE_ALL_DISABLED);
        group.setCreateTime(LocalDateTime.now());
        group.setUpdateTime(LocalDateTime.now());

        groupInfoMapper.insert(group);

        // 添加群主为成�?
        GroupMemberDO owner = new GroupMemberDO();
        owner.setGroupId(group.getId());
        owner.setUserId(userId);
        owner.setNickname(resolveNickname(userId));
        owner.setRole(ROLE_OWNER);
        owner.setStatus(MEMBER_STATUS_ACTIVE);
        owner.setMuted(MUTE_STATUS_NORMAL);
        owner.setJoinTime(LocalDateTime.now());
        owner.setCreateTime(LocalDateTime.now());
        owner.setUpdateTime(LocalDateTime.now());

        groupMemberMapper.insert(owner);

        // 如果有初始成�?添加他们
        if (reqVO.getMemberIds() != null && !reqVO.getMemberIds().isEmpty()) {
            for (Long memberId : reqVO.getMemberIds()) {
                if (memberId.equals(userId)) {
                    continue; // 跳过群主
                }
                addMemberInternal(group.getId(), memberId, userId);
            }
        }

        log.info("创建群组成功: groupId={}, groupName={}, owner={}",
                group.getId(), reqVO.getGroupName(), userId);

        // 发送群创建系统消息
        sendSystemMessage(group.getId(), userId, "创建了群组");

        return group.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void dissolveGroup(Long groupId, Long userId) {
        // 检查群组是否存�?
        GroupInfoDO group = groupInfoMapper.selectActiveGroup(groupId);
        if (group == null) {
            throw ServiceExceptionUtil.exception(GROUP_NOT_EXISTS);
        }

        // 检查是否是群主
        if (!group.getOwnerUserId().equals(userId)) {
            throw ServiceExceptionUtil.exception(GROUP_ONLY_OWNER_CAN_DISSOLVE);
        }

        // 解散群组
        groupInfoMapper.dissolveGroup(groupId);

        // 移除所有成�?
        List<GroupMemberDO> members = groupMemberMapper.selectListByGroupId(groupId);
        for (GroupMemberDO member : members) {
            groupMemberMapper.removeMember(groupId, member.getUserId(), MEMBER_STATUS_QUIT);
        }

        log.info("解散群组成功: groupId={}, owner={}", groupId, userId);

        // 发送群解散通知
        sendSystemMessage(groupId, userId, "解散了群组");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addMembers(GroupAddMemberReqVO reqVO) {
        Long operatorId = SecurityFrameworkUtils.getLoginUserId();
        if (operatorId == null) {
            throw ServiceExceptionUtil.exception(UNAUTHORIZED);
        }

        // 检查群�?
        GroupInfoDO group = groupInfoMapper.selectActiveGroup(reqVO.getGroupId());
        if (group == null) {
            throw ServiceExceptionUtil.exception(GROUP_NOT_EXISTS);
        }

        // 检查操作者是否是群成�?
        GroupMemberDO operator = groupMemberMapper.selectByGroupAndUser(reqVO.getGroupId(), operatorId);
        if (operator == null) {
            throw ServiceExceptionUtil.exception(GROUP_INVITE_FORBIDDEN);
        }

        // 添加成员
        for (Long userId : reqVO.getUserIds()) {
            try {
                addMemberInternal(reqVO.getGroupId(), userId, operatorId);
            } catch (Exception e) {
                log.warn("添加群成员失�? groupId={}, userId={}, error={}",
                        reqVO.getGroupId(), userId, e.getMessage());
            }
        }

        log.info("批量添加群成员成�? groupId={}, count={}, operator={}",
                reqVO.getGroupId(), reqVO.getUserIds().size(), operatorId);
    }

    /**
     * 内部方法:添加单个成员
     */
    private void addMemberInternal(Long groupId, Long userId, Long inviterId) {
        // 检查是否已经是成员
        GroupMemberDO existingMember = groupMemberMapper.selectByGroupAndUser(groupId, userId);
        if (existingMember != null) {
            log.warn("用户已经是群成员: groupId={}, userId={}", groupId, userId);
            return;
        }

        // 检查群成员数是否已�?
        GroupInfoDO group = groupInfoMapper.selectById(groupId);
        if (group.getMemberCount() >= group.getMaxMemberCount()) {
            throw ServiceExceptionUtil.exception(GROUP_MEMBER_FULL);
        }

        // 添加成员
        GroupMemberDO member = new GroupMemberDO();
        member.setGroupId(groupId);
        member.setUserId(userId);
        member.setNickname(resolveNickname(userId));
        member.setRole(ROLE_MEMBER);
        member.setStatus(MEMBER_STATUS_ACTIVE);
        member.setMuted(MUTE_STATUS_NORMAL);
        member.setJoinTime(LocalDateTime.now());
        member.setCreateTime(LocalDateTime.now());
        member.setUpdateTime(LocalDateTime.now());

        groupMemberMapper.insert(member);

        // 更新群成员数
        groupInfoMapper.updateMemberCount(groupId, 1);

        // 发送入群系统消�?
        sendSystemMessage(groupId, userId, "加入了群组");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeMember(Long groupId, Long userId, Long operatorId) {
        // 检查群�?
        GroupInfoDO group = groupInfoMapper.selectActiveGroup(groupId);
        if (group == null) {
            throw ServiceExceptionUtil.exception(GROUP_NOT_EXISTS);
        }

        // 检查操作者权�?群主或管理员)
        GroupMemberDO operator = groupMemberMapper.selectByGroupAndUser(groupId, operatorId);
        if (operator == null || operator.getRole() > ROLE_ADMIN_OR_ABOVE) {
            throw ServiceExceptionUtil.exception(GROUP_REMOVE_PERMISSION_DENIED);
        }

        // 不能移除群主
        if (userId.equals(group.getOwnerUserId())) {
            throw ServiceExceptionUtil.exception(GROUP_CANNOT_REMOVE_OWNER);
        }

        // 检查目标用户是否是成员
        GroupMemberDO member = groupMemberMapper.selectByGroupAndUser(groupId, userId);
        if (member == null) {
            throw ServiceExceptionUtil.exception(GROUP_MEMBER_NOT_EXISTS);
        }

        // 管理员不能移除管理员
        if (operator.getRole() == ROLE_ADMIN && member.getRole() <= ROLE_ADMIN_OR_ABOVE) {
            throw ServiceExceptionUtil.exception(GROUP_ADMIN_REMOVE_DENIED);
        }

        // 移除成员
        groupMemberMapper.removeMember(groupId, userId, MEMBER_STATUS_KICKED);

        // 更新群成员数
        groupInfoMapper.updateMemberCount(groupId, -1);

        log.info("移除群成员成�? groupId={}, userId={}, operator={}", groupId, userId, operatorId);

        // 发送移除通知
        sendSystemMessage(groupId, userId, "被移出群组");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void quitGroup(Long groupId, Long userId) {
        // 检查群�?
        GroupInfoDO group = groupInfoMapper.selectActiveGroup(groupId);
        if (group == null) {
            throw ServiceExceptionUtil.exception(GROUP_NOT_EXISTS);
        }

        // 群主不能退�?只能解散
        if (userId.equals(group.getOwnerUserId())) {
            throw ServiceExceptionUtil.exception(GROUP_OWNER_CANNOT_QUIT);
        }

        // 检查是否是成员
        GroupMemberDO member = groupMemberMapper.selectByGroupAndUser(groupId, userId);
        if (member == null) {
            throw ServiceExceptionUtil.exception(GROUP_MEMBER_NOT_EXISTS);
        }

        // 退出群�?
        groupMemberMapper.removeMember(groupId, userId, MEMBER_STATUS_QUIT);

        // 更新群成员数
        groupInfoMapper.updateMemberCount(groupId, -1);

        log.info("退出群组成�? groupId={}, userId={}", groupId, userId);

        // 发送退出通知
        sendSystemMessage(groupId, userId, "退出了群聊");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long sendGroupMessage(GroupMessageSendReqVO reqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        if (userId == null) {
            throw ServiceExceptionUtil.exception(UNAUTHORIZED);
        }

        // 检查群�?
        GroupInfoDO group = groupInfoMapper.selectActiveGroup(reqVO.getGroupId());
        if (group == null) {
            throw ServiceExceptionUtil.exception(GROUP_NOT_EXISTS);
        }

        // 检查是否是群成�?
        GroupMemberDO member = groupMemberMapper.selectByGroupAndUser(reqVO.getGroupId(), userId);
        if (member == null) {
            throw ServiceExceptionUtil.exception(GROUP_SEND_NOT_MEMBER);
        }

        // 检查是否被禁言
        if (member.getMuted() == MUTE_STATUS_MUTED) {
            // 检查禁言是否过期
            if (member.getMuteEndTime() != null && member.getMuteEndTime().isAfter(LocalDateTime.now())) {
                throw ServiceExceptionUtil.exception(GROUP_SEND_MUTED);
            }
            // 禁言已过�?自动解除
            groupMemberMapper.updateMuteStatus(reqVO.getGroupId(), userId, MUTE_STATUS_NORMAL, null);
        }

        // 检查是否全员禁言(群主和管理员不受限制)
        if (group.getMuteAll() == MUTE_ALL_ENABLED && member.getRole() > ROLE_ADMIN_OR_ABOVE) {
            throw ServiceExceptionUtil.exception(GROUP_SEND_ALL_MUTED);
        }

        // 创建消息
        GroupMessageDO message = new GroupMessageDO();
        message.setGroupId(reqVO.getGroupId());
        message.setFromUserId(userId);
        message.setType(reqVO.getType());
        message.setContent(reqVO.getContent());
        message.setExtraData(reqVO.getExtraData());
        message.setStatus(MESSAGE_STATUS_NORMAL);
        message.setDeleted(MESSAGE_NOT_DELETED);
        message.setCreateTime(LocalDateTime.now());
        message.setUpdateTime(LocalDateTime.now());

        groupMessageMapper.insert(message);

        log.info("发送群消息成功: messageId={}, groupId={}, from={}",
                message.getId(), reqVO.getGroupId(), userId);

        // 发送到Kafka
        sendGroupMessageToKafka(message, group, member);

        return message.getId();
    }

    /**
     * 发送群消息到Kafka
     */
    private void sendGroupMessageToKafka(GroupMessageDO message, GroupInfoDO group, GroupMemberDO sender) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("deleted", message.getDeleted());
            data.put("messageId", message.getId());
            data.put("groupId", message.getGroupId());
            data.put("groupName", group.getGroupName());
            data.put("fromUserId", message.getFromUserId());
            data.put("senderNickname", sender.getNickname() != null ? sender.getNickname() : "");
            data.put("type", message.getType());
            data.put("content", message.getContent());
            data.put("extraData", message.getExtraData());
            data.put("createTime", message.getCreateTime().toString());

            messageProducer.sendGroupMessage(data);
        } catch (Exception e) {
            log.error("发送群消息到Kafka失败: messageId={}", message.getId(), e);
        }
    }

    /**
     * 发送系统消�?
     */
    private void sendSystemMessage(Long groupId, Long userId, String action) {
        GroupMessageDO systemMessage = new GroupMessageDO();
        systemMessage.setGroupId(groupId);
        systemMessage.setFromUserId(userId);
        systemMessage.setType(MESSAGE_TYPE_SYSTEM);
        systemMessage.setContent(action);
        systemMessage.setStatus(MESSAGE_STATUS_NORMAL);
        systemMessage.setDeleted(MESSAGE_NOT_DELETED);
        systemMessage.setCreateTime(LocalDateTime.now());
        systemMessage.setUpdateTime(LocalDateTime.now());

        groupMessageMapper.insert(systemMessage);

        // 发送到Kafka
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("deleted", systemMessage.getDeleted());
            data.put("messageId", systemMessage.getId());
            data.put("groupId", groupId);
            data.put("fromUserId", userId);
            data.put("type", MESSAGE_TYPE_SYSTEM);
            data.put("content", action);
            data.put("createTime", systemMessage.getCreateTime().toString());

            messageProducer.sendGroupMessage(data);
        } catch (Exception e) {
            log.error("发送系统消息到Kafka失败: messageId={}", systemMessage.getId(), e);
        }
    }

    @Override
    public void recallMessage(Long messageId, Long userId) {
        GroupMessageDO message = groupMessageMapper.selectById(messageId);
        if (message == null) {
            throw ServiceExceptionUtil.exception(MESSAGE_NOT_EXISTS);
        }

        // 只能撤回自己发送的消息
        if (!message.getFromUserId().equals(userId)) {
            throw ServiceExceptionUtil.exception(MESSAGE_RECALL_ONLY_SELF);
        }

        // 只能撤回2分钟内的消息
        if (message.getCreateTime().plusMinutes(MESSAGE_RECALL_TIMEOUT_MINUTES).isBefore(LocalDateTime.now())) {
            throw ServiceExceptionUtil.exception(MESSAGE_RECALL_TIMEOUT);
        }

        groupMessageMapper.updateStatusToRecall(messageId, userId);

        log.info("撤回群消�? messageId={}, userId={}", messageId, userId);
    }

    @Override
    public GroupInfoDO getGroupInfo(Long groupId) {
        return groupInfoMapper.selectActiveGroup(groupId);
    }

    @Override
    public List<GroupMemberDO> getGroupMembers(Long groupId) {
        // 检查群组是否存�?
        GroupInfoDO group = groupInfoMapper.selectActiveGroup(groupId);
        if (group == null) {
            throw ServiceExceptionUtil.exception(GROUP_NOT_EXISTS);
        }

        List<GroupMemberDO> members = groupMemberMapper.selectListByGroupId(groupId);
        if (members.isEmpty()) {
            return members;
        }

        try {
            Set<Long> userIds = members.stream().map(GroupMemberDO::getUserId).collect(Collectors.toSet());
            Map<Long, MemberUserRespDTO> userMap = memberUserApi.getUserMap(userIds);
            for (GroupMemberDO member : members) {
                if (StrUtil.isBlank(member.getNickname())) {
                    MemberUserRespDTO user = userMap.get(member.getUserId());
                    if (user != null && StrUtil.isNotBlank(user.getNickname())) {
                        member.setNickname(user.getNickname());
                    }
                }
            }
        } catch (Exception ex) {
            log.warn("[getGroupMembers][groupId({}) load member nickname failed]", groupId, ex);
        }

        return members;
    }

    private String resolveNickname(Long userId) {
        if (userId == null) {
            return null;
        }
        try {
            MemberUserRespDTO user = memberUserApi.getUser(userId).getCheckedData();
            return user != null ? user.getNickname() : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    @Override
    public List<GroupInfoDO> getUserGroups(Long userId) {
        // 获取用户加入的所有群组ID
        List<Long> groupIds = groupMemberMapper.selectGroupIdsByUserId(userId);

        if (groupIds.isEmpty()) {
            return List.of();
        }

        // 批量查询群组信息
        return groupInfoMapper.selectBatchIds(groupIds);
    }

    @Override
    public List<GroupMessageDO> getGroupMessages(Long groupId, Integer limit) {
        if (limit == null || limit <= 0) {
            limit = DEFAULT_MESSAGE_LIMIT;
        }

        // 检查群组是否存�?
        GroupInfoDO group = groupInfoMapper.selectActiveGroup(groupId);
        if (group == null) {
            throw ServiceExceptionUtil.exception(GROUP_NOT_EXISTS);
        }

        return groupMessageMapper.selectGroupMessages(groupId, limit);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateGroupInfo(GroupUpdateReqVO reqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        if (userId == null) {
            throw ServiceExceptionUtil.exception(UNAUTHORIZED);
        }

        // 检查群�?
        GroupInfoDO group = groupInfoMapper.selectActiveGroup(reqVO.getGroupId());
        if (group == null) {
            throw ServiceExceptionUtil.exception(GROUP_NOT_EXISTS);
        }

        // 检查权�?群主或管理员)
        GroupMemberDO member = groupMemberMapper.selectByGroupAndUser(reqVO.getGroupId(), userId);
        if (member == null || member.getRole() > ROLE_ADMIN_OR_ABOVE) {
            throw ServiceExceptionUtil.exception(GROUP_UPDATE_PERMISSION_DENIED);
        }

        // 更新群信�?
        GroupInfoDO update = new GroupInfoDO();
        update.setId(reqVO.getGroupId());

        if (StrUtil.isNotBlank(reqVO.getGroupName())) {
            update.setGroupName(reqVO.getGroupName());
        }
        if (StrUtil.isNotBlank(reqVO.getAvatar())) {
            update.setAvatar(reqVO.getAvatar());
        }
        if (StrUtil.isNotBlank(reqVO.getAnnouncement())) {
            update.setAnnouncement(reqVO.getAnnouncement());
        }
        if (StrUtil.isNotBlank(reqVO.getDescription())) {
            update.setDescription(reqVO.getDescription());
        }
        if (reqVO.getJoinType() != null) {
            update.setJoinType(reqVO.getJoinType());
        }

        update.setUpdateTime(LocalDateTime.now());
        groupInfoMapper.updateById(update);

        log.info("更新群信息成�? groupId={}, operator={}", reqVO.getGroupId(), userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setMemberRole(Long groupId, Long userId, Integer role, Long operatorId) {
        // 检查群�?
        GroupInfoDO group = groupInfoMapper.selectActiveGroup(groupId);
        if (group == null) {
            throw ServiceExceptionUtil.exception(GROUP_NOT_EXISTS);
        }

        // 只有群主可以设置角色
        if (!group.getOwnerUserId().equals(operatorId)) {
            throw ServiceExceptionUtil.exception(GROUP_SET_ROLE_ONLY_OWNER);
        }

        // 不能修改群主角色
        if (userId.equals(group.getOwnerUserId())) {
            throw ServiceExceptionUtil.exception(GROUP_ROLE_OWNER_FORBIDDEN);
        }

        // 检查目标用户是否是成员
        GroupMemberDO member = groupMemberMapper.selectByGroupAndUser(groupId, userId);
        if (member == null) {
            throw ServiceExceptionUtil.exception(GROUP_MEMBER_NOT_EXISTS);
        }

        // 验证角色�?只能设置为管理员(2)或普通成�?3)
        if (role < ROLE_ADMIN || role > ROLE_MEMBER) {
            throw ServiceExceptionUtil.exception(GROUP_ROLE_INVALID);
        }

        groupMemberMapper.updateMemberRole(groupId, userId, role);

        log.info("设置成员角色: groupId={}, userId={}, role={}, operator={}",
                groupId, userId, role, operatorId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void muteMember(GroupMuteMemberReqVO reqVO) {
        Long operatorId = SecurityFrameworkUtils.getLoginUserId();
        if (operatorId == null) {
            throw ServiceExceptionUtil.exception(UNAUTHORIZED);
        }

        // 检查群�?
        GroupInfoDO group = groupInfoMapper.selectActiveGroup(reqVO.getGroupId());
        if (group == null) {
            throw ServiceExceptionUtil.exception(GROUP_NOT_EXISTS);
        }

        // 检查操作者权�?群主或管理员)
        GroupMemberDO operator = groupMemberMapper.selectByGroupAndUser(reqVO.getGroupId(), operatorId);
        if (operator == null || operator.getRole() > ROLE_ADMIN_OR_ABOVE) {
            throw ServiceExceptionUtil.exception(GROUP_MUTE_PERMISSION_DENIED);
        }

        // 不能禁言群主
        if (reqVO.getUserId().equals(group.getOwnerUserId())) {
            throw ServiceExceptionUtil.exception(GROUP_CANNOT_MUTE_OWNER);
        }

        // 检查目标用�?
        GroupMemberDO member = groupMemberMapper.selectByGroupAndUser(reqVO.getGroupId(), reqVO.getUserId());
        if (member == null) {
            throw ServiceExceptionUtil.exception(GROUP_MEMBER_NOT_EXISTS);
        }

        // 管理员不能禁言管理�?
        if (operator.getRole() == ROLE_ADMIN && member.getRole() <= ROLE_ADMIN_OR_ABOVE) {
            throw ServiceExceptionUtil.exception(GROUP_ADMIN_MUTE_DENIED);
        }

        // 设置禁言
        LocalDateTime muteEndTime = LocalDateTime.now().plusMinutes(reqVO.getMuteDuration());
        groupMemberMapper.updateMuteStatus(reqVO.getGroupId(), reqVO.getUserId(), MUTE_STATUS_MUTED, muteEndTime);

        log.info("禁言成员: groupId={}, userId={}, duration={}分钟, operator={}",
                reqVO.getGroupId(), reqVO.getUserId(), reqVO.getMuteDuration(), operatorId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unmuteMember(Long groupId, Long userId, Long operatorId) {
        // 检查群�?
        GroupInfoDO group = groupInfoMapper.selectActiveGroup(groupId);
        if (group == null) {
            throw ServiceExceptionUtil.exception(GROUP_NOT_EXISTS);
        }

        // 检查操作者权�?群主或管理员)
        GroupMemberDO operator = groupMemberMapper.selectByGroupAndUser(groupId, operatorId);
        if (operator == null || operator.getRole() > ROLE_ADMIN_OR_ABOVE) {
            throw ServiceExceptionUtil.exception(GROUP_UNMUTE_PERMISSION_DENIED);
        }

        // 取消禁言
        groupMemberMapper.updateMuteStatus(groupId, userId, MUTE_STATUS_NORMAL, null);

        log.info("取消禁言: groupId={}, userId={}, operator={}", groupId, userId, operatorId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setMuteAll(Long groupId, Integer muteAll, Long operatorId) {
        // 检查群�?
        GroupInfoDO group = groupInfoMapper.selectActiveGroup(groupId);
        if (group == null) {
            throw ServiceExceptionUtil.exception(GROUP_NOT_EXISTS);
        }

        // 只有群主可以设置全员禁言
        if (!group.getOwnerUserId().equals(operatorId)) {
            throw ServiceExceptionUtil.exception(GROUP_MUTEALL_ONLY_OWNER);
        }

        // 更新全员禁言状�?
        GroupInfoDO update = new GroupInfoDO();
        update.setId(groupId);
        update.setMuteAll(muteAll);
        update.setUpdateTime(LocalDateTime.now());

        groupInfoMapper.updateById(update);

        log.info("设置全员禁言: groupId={}, muteAll={}, operator={}", groupId, muteAll, operatorId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void transferOwner(Long groupId, Long newOwnerId, Long currentOwnerId) {
        // 检查群�?
        GroupInfoDO group = groupInfoMapper.selectActiveGroup(groupId);
        if (group == null) {
            throw ServiceExceptionUtil.exception(GROUP_NOT_EXISTS);
        }

        // 验证当前操作者是群主
        if (!group.getOwnerUserId().equals(currentOwnerId)) {
            throw ServiceExceptionUtil.exception(GROUP_TRANSFER_ONLY_OWNER);
        }

        // 检查新群主是否是成�?
        GroupMemberDO newOwner = groupMemberMapper.selectByGroupAndUser(groupId, newOwnerId);
        if (newOwner == null) {
            throw ServiceExceptionUtil.exception(GROUP_TRANSFER_TARGET_NOT_MEMBER);
        }

        // 更新群主
        GroupInfoDO update = new GroupInfoDO();
        update.setId(groupId);
        update.setOwnerUserId(newOwnerId);
        update.setUpdateTime(LocalDateTime.now());
        groupInfoMapper.updateById(update);

        // 更新原群主角色为管理�?
        groupMemberMapper.updateMemberRole(groupId, currentOwnerId, ROLE_ADMIN);

        // 更新新群主角�?
        groupMemberMapper.updateMemberRole(groupId, newOwnerId, ROLE_OWNER);

        log.info("转让群主: groupId={}, from={}, to={}", groupId, currentOwnerId, newOwnerId);

        // 发送系统消�?
        sendSystemMessage(groupId, newOwnerId, "成为新的群主");
    }
}
