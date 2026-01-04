package cn.iocoder.yudao.module.message.service;

import cn.iocoder.yudao.module.message.controller.app.vo.group.*;
import cn.iocoder.yudao.module.message.dal.dataobject.GroupInfoDO;
import cn.iocoder.yudao.module.message.dal.dataobject.GroupMemberDO;
import cn.iocoder.yudao.module.message.dal.dataobject.GroupMessageDO;

import java.util.List;

/**
 * 群组Service接口
 *
 * @author xiaolvshu
 */
public interface GroupService {

    /**
     * 创建群组
     *
     * @param reqVO 创建请求
     * @return 群组ID
     */
    Long createGroup(GroupCreateReqVO reqVO);

    /**
     * 解散群组(仅群主可操作)
     *
     * @param groupId 群组ID
     * @param userId  操作用户ID
     */
    void dissolveGroup(Long groupId, Long userId);

    /**
     * 添加群成员
     *
     * @param reqVO 添加成员请求
     */
    void addMembers(GroupAddMemberReqVO reqVO);

    /**
     * 移除群成员(退出或踢出)
     *
     * @param groupId 群组ID
     * @param userId  要移除的用户ID
     * @param operatorId 操作者ID
     */
    void removeMember(Long groupId, Long userId, Long operatorId);

    /**
     * 退出群组(自己退出)
     *
     * @param groupId 群组ID
     * @param userId  用户ID
     */
    void quitGroup(Long groupId, Long userId);

    /**
     * 发送群消息
     *
     * @param reqVO 发送消息请求
     * @return 消息ID
     */
    Long sendGroupMessage(GroupMessageSendReqVO reqVO);

    /**
     * 撤回群消息
     *
     * @param messageId 消息ID
     * @param userId    用户ID
     */
    void recallMessage(Long messageId, Long userId);

    /**
     * 获取群信息
     *
     * @param groupId 群组ID
     * @return 群组信息
     */
    GroupInfoDO getGroupInfo(Long groupId);

    /**
     * 获取群成员列表
     *
     * @param groupId 群组ID
     * @return 成员列表
     */
    List<GroupMemberDO> getGroupMembers(Long groupId);

    /**
     * 获取用户加入的所有群组
     *
     * @param userId 用户ID
     * @return 群组列表
     */
    List<GroupInfoDO> getUserGroups(Long userId);

    /**
     * 获取群聊天记录
     *
     * @param groupId 群组ID
     * @param limit   限制数量
     * @return 消息列表
     */
    List<GroupMessageDO> getGroupMessages(Long groupId, Integer limit);

    /**
     * 更新群组信息
     *
     * @param reqVO 更新请求
     */
    void updateGroupInfo(GroupUpdateReqVO reqVO);

    /**
     * 设置群成员角色(仅群主可操作)
     *
     * @param groupId 群组ID
     * @param userId  目标用户ID
     * @param role    角色 1-群主 2-管理员 3-普通成员
     * @param operatorId 操作者ID
     */
    void setMemberRole(Long groupId, Long userId, Integer role, Long operatorId);

    /**
     * 设置群成员禁言(群主和管理员可操作)
     *
     * @param reqVO 禁言请求
     */
    void muteMember(GroupMuteMemberReqVO reqVO);

    /**
     * 取消群成员禁言
     *
     * @param groupId 群组ID
     * @param userId  目标用户ID
     * @param operatorId 操作者ID
     */
    void unmuteMember(Long groupId, Long userId, Long operatorId);

    /**
     * 设置全员禁言(仅群主可操作)
     *
     * @param groupId 群组ID
     * @param muteAll 是否全员禁言 0-否 1-是
     * @param operatorId 操作者ID
     */
    void setMuteAll(Long groupId, Integer muteAll, Long operatorId);

    /**
     * 转让群主(仅群主可操作)
     *
     * @param groupId 群组ID
     * @param newOwnerId 新群主用户ID
     * @param currentOwnerId 当前群主ID
     */
    void transferOwner(Long groupId, Long newOwnerId, Long currentOwnerId);
}
