package cn.iocoder.yudao.module.message.controller.app;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import org.springframework.security.access.prepost.PreAuthorize;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.message.controller.app.vo.group.*;
import cn.iocoder.yudao.module.message.dal.dataobject.GroupInfoDO;
import cn.iocoder.yudao.module.message.dal.dataobject.GroupMemberDO;
import cn.iocoder.yudao.module.message.dal.dataobject.GroupMessageDO;
import cn.iocoder.yudao.module.message.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 群组API - APP端
 *
 * @author xiaolvshu
 */
@Tag(name = "APP - 群组管理")
@RestController
// app-api 前缀由 YudaoWebAutoConfiguration 基于包名自动添加，这里只保留业务路径
@RequestMapping("/message/group")
@Validated
@Slf4j
public class AppGroupController {

    @Resource
    private GroupService groupService;

    @Resource
    private MemberUserApi memberUserApi;

    // ========== 群组管理接口 ==========

    /**
     * 创建群组
     */
    @PostMapping("/create")
    @Operation(summary = "创建群组")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<Long> createGroup(@Valid @RequestBody GroupCreateReqVO reqVO) {
        Long groupId = groupService.createGroup(reqVO);
        return CommonResult.success(groupId);
    }

    /**
     * 解散群组
     */
    @DeleteMapping("/dissolve/{groupId}")
    @Operation(summary = "解散群组")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<Boolean> dissolveGroup(@PathVariable("groupId") Long groupId) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        groupService.dissolveGroup(groupId, userId);
        return CommonResult.success(true);
    }

    /**
     * 获取群信息
     */
    @GetMapping("/info/{groupId}")
    @Operation(summary = "获取群信息")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<GroupInfoDO> getGroupInfo(@PathVariable("groupId") Long groupId) {
        GroupInfoDO group = groupService.getGroupInfo(groupId);
        return CommonResult.success(group);
    }

    /**
     * 更新群信息
     */
    @PutMapping("/update")
    @Operation(summary = "更新群信息")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<Boolean> updateGroupInfo(@Valid @RequestBody GroupUpdateReqVO reqVO) {
        groupService.updateGroupInfo(reqVO);
        return CommonResult.success(true);
    }

    /**
     * 获取用户加入的所有群组
     */
    @GetMapping("/my-groups")
    @Operation(summary = "获取我的群组列表")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<List<GroupInfoDO>> getUserGroups() {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        List<GroupInfoDO> groups = groupService.getUserGroups(userId);
        return CommonResult.success(groups);
    }

    // ========== 群成员管理接口 ==========

    /**
     * 添加群成员
     */
    @PostMapping("/member/add")
    @Operation(summary = "添加群成员")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<Boolean> addMembers(@Valid @RequestBody GroupAddMemberReqVO reqVO) {
        groupService.addMembers(reqVO);
        return CommonResult.success(true);
    }

    /**
     * 移除群成员
     */
    @DeleteMapping("/member/remove")
    @Operation(summary = "移除群成员")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<Boolean> removeMember(
            @Parameter(description = "群组ID", required = true) @RequestParam("groupId") Long groupId,
            @Parameter(description = "用户ID", required = true) @RequestParam("userId") Long userId) {
        Long operatorId = SecurityFrameworkUtils.getLoginUserId();
        groupService.removeMember(groupId, userId, operatorId);
        return CommonResult.success(true);
    }

    /**
     * 退出群组
     */
    @PostMapping("/member/quit")
    @Operation(summary = "退出群组")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<Boolean> quitGroup(@RequestParam("groupId") Long groupId) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        groupService.quitGroup(groupId, userId);
        return CommonResult.success(true);
    }

    /**
     * 获取群成员列表
     */
    @GetMapping("/member/list")
    @Operation(summary = "获取群成员列表")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<List<GroupMemberDO>> getGroupMembers(
            @Parameter(description = "群组ID", required = true) @RequestParam("groupId") Long groupId) {
        List<GroupMemberDO> members = groupService.getGroupMembers(groupId);
        return CommonResult.success(members);
    }

    /**
     * 设置群成员角色
     */
    @PostMapping("/member/set-role")
    @Operation(summary = "设置群成员角色")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<Boolean> setMemberRole(
            @Parameter(description = "群组ID", required = true) @RequestParam("groupId") Long groupId,
            @Parameter(description = "用户ID", required = true) @RequestParam("userId") Long userId,
            @Parameter(description = "角色 2-管理员 3-普通成员", required = true) @RequestParam("role") Integer role) {
        Long operatorId = SecurityFrameworkUtils.getLoginUserId();
        groupService.setMemberRole(groupId, userId, role, operatorId);
        return CommonResult.success(true);
    }

    /**
     * 禁言群成员
     */
    @PostMapping("/member/mute")
    @Operation(summary = "禁言群成员")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<Boolean> muteMember(@Valid @RequestBody GroupMuteMemberReqVO reqVO) {
        groupService.muteMember(reqVO);
        return CommonResult.success(true);
    }

    /**
     * 取消禁言
     */
    @PostMapping("/member/unmute")
    @Operation(summary = "取消禁言")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<Boolean> unmuteMember(
            @Parameter(description = "群组ID", required = true) @RequestParam("groupId") Long groupId,
            @Parameter(description = "用户ID", required = true) @RequestParam("userId") Long userId) {
        Long operatorId = SecurityFrameworkUtils.getLoginUserId();
        groupService.unmuteMember(groupId, userId, operatorId);
        return CommonResult.success(true);
    }

    /**
     * 设置全员禁言
     */
    @PostMapping("/mute-all")
    @Operation(summary = "设置全员禁言")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<Boolean> setMuteAll(
            @Parameter(description = "群组ID", required = true) @RequestParam("groupId") Long groupId,
            @Parameter(description = "是否全员禁言 0-否 1-是", required = true) @RequestParam("muteAll") Integer muteAll) {
        Long operatorId = SecurityFrameworkUtils.getLoginUserId();
        groupService.setMuteAll(groupId, muteAll, operatorId);
        return CommonResult.success(true);
    }

    /**
     * 转让群主
     */
    @PostMapping("/transfer-owner")
    @Operation(summary = "转让群主")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<Boolean> transferOwner(
            @Parameter(description = "群组ID", required = true) @RequestParam("groupId") Long groupId,
            @Parameter(description = "新群主用户ID", required = true) @RequestParam("newOwnerId") Long newOwnerId) {
        Long currentOwnerId = SecurityFrameworkUtils.getLoginUserId();
        groupService.transferOwner(groupId, newOwnerId, currentOwnerId);
        return CommonResult.success(true);
    }

    // ========== 群消息接口 ==========

    /**
     * 发送群消息
     */
    @PostMapping("/message/send")
    @Operation(summary = "发送群消息")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<Long> sendGroupMessage(@Valid @RequestBody GroupMessageSendReqVO reqVO) {
        Long messageId = groupService.sendGroupMessage(reqVO);
        return CommonResult.success(messageId);
    }

    /**
     * 撤回群消息
     */
    @PostMapping("/message/recall")
    @Operation(summary = "撤回群消息")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<Boolean> recallMessage(@RequestParam("messageId") Long messageId) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        groupService.recallMessage(messageId, userId);
        return CommonResult.success(true);
    }

    /**
     * 获取群聊天记录
     */
    @GetMapping("/message/list")
    @Operation(summary = "获取群聊天记录")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<List<AppGroupMessageRespVO>> getGroupMessages(
            @Parameter(description = "群组ID", required = true) @RequestParam("groupId") Long groupId,
            @Parameter(description = "限制数量", example = "20") @RequestParam(value = "limit", required = false) Integer limit) {
        List<GroupMessageDO> messages = groupService.getGroupMessages(groupId, limit);
        if (messages == null || messages.isEmpty()) {
            return CommonResult.success(Collections.emptyList());
        }

        List<GroupMemberDO> members = groupService.getGroupMembers(groupId);
        Map<Long, String> nicknameMap = members.stream()
                .collect(Collectors.toMap(GroupMemberDO::getUserId,
                        m -> StrUtil.blankToDefault(m.getNickname(), ""),
                        (a, b) -> a));

        Set<Long> senderIds = messages.stream().map(GroupMessageDO::getFromUserId).collect(Collectors.toSet());
        Map<Long, MemberUserRespDTO> userMap = memberUserApi.getUserMap(senderIds);

        List<AppGroupMessageRespVO> resp = messages.stream().map(m -> {
            AppGroupMessageRespVO vo = new AppGroupMessageRespVO();
            vo.setId(m.getId());
            vo.setGroupId(m.getGroupId());
            vo.setFromUserId(m.getFromUserId());
            MemberUserRespDTO user = userMap.get(m.getFromUserId());

            String nickname = nicknameMap.get(m.getFromUserId());
            if (StrUtil.isBlank(nickname) && user != null) {
                nickname = user.getNickname();
            }
            vo.setSenderNickname(nickname);
            vo.setSenderAvatar(user != null ? user.getAvatar() : null);

            vo.setType(m.getType());
            vo.setContent(m.getContent());
            vo.setExtraData(m.getExtraData());
            vo.setStatus(m.getStatus());
            vo.setCreateTime(m.getCreateTime());
            return vo;
        }).collect(Collectors.toList());

        return CommonResult.success(resp);
    }
}

