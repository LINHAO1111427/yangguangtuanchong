package cn.iocoder.yudao.module.member.controller.app.social;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.member.controller.app.social.vo.*;
import cn.iocoder.yudao.module.member.dal.dataobject.user.MemberUserDO;
import cn.iocoder.yudao.module.member.service.social.MemberRelationService;
import cn.iocoder.yudao.module.member.service.social.bo.MemberPotentialFriendBO;
import cn.iocoder.yudao.module.member.service.social.bo.MemberRelationSummary;
import cn.iocoder.yudao.module.member.service.social.bo.MemberSocialUserBO;
import cn.iocoder.yudao.module.member.service.user.MemberUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 APP - 社交关系")
@RestController
@RequestMapping({"/member/social", "/app-api/member/social"})
@Validated
public class AppMemberSocialController {

    @Resource
    private MemberRelationService relationService;
    @Resource
    private MemberUserService userService;

    @PostMapping("/follow")
    @Operation(summary = "发起关注/好友请求")
    public CommonResult<Long> follow(@RequestBody AppRelationApplyReqVO reqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        Long requestId = relationService.createRelationRequest(reqVO.toBO(userId));
        return success(requestId);
    }

    @PostMapping("/follow/approve")
    @Operation(summary = "同意关注/好友请求")
    public CommonResult<Boolean> approve(@RequestBody AppRelationHandleReqVO reqVO) {
        relationService.approveRelation(reqVO.getRequestId(), SecurityFrameworkUtils.getLoginUserId());
        return success(true);
    }

    @PostMapping("/follow/reject")
    @Operation(summary = "拒绝关注/好友请求")
    public CommonResult<Boolean> reject(@RequestBody AppRelationHandleReqVO reqVO) {
        relationService.rejectRelation(reqVO.getRequestId(), SecurityFrameworkUtils.getLoginUserId(), reqVO.getReason());
        return success(true);
    }

    @DeleteMapping("/follow/{targetUserId}")
    @Operation(summary = "取消关注/删除好友")
    public CommonResult<Boolean> unfollow(@PathVariable("targetUserId") Long targetUserId) {
        relationService.removeRelation(SecurityFrameworkUtils.getLoginUserId(), targetUserId);
        return success(true);
    }

    @PostMapping("/blacklist")
    @Operation(summary = "添加黑名单")
    public CommonResult<Boolean> addBlacklist(@RequestBody AppBlacklistReqVO reqVO) {
        relationService.addToBlacklist(SecurityFrameworkUtils.getLoginUserId(),
                reqVO.getTargetUserId(), reqVO.getReason());
        return success(true);
    }

    @DeleteMapping("/blacklist/{targetUserId}")
    @Operation(summary = "移除黑名单")
    public CommonResult<Boolean> removeBlacklist(@PathVariable("targetUserId") Long targetUserId) {
        relationService.removeFromBlacklist(SecurityFrameworkUtils.getLoginUserId(), targetUserId);
        return success(true);
    }

    @GetMapping("/search")
    @Operation(summary = "搜索用户")
    public CommonResult<List<AppSearchUserRespVO>> search(@RequestParam("keyword") String keyword,
                                                          @RequestParam(value = "limit", defaultValue = "20") Integer limit) {
        List<MemberSocialUserBO> list = relationService.searchUsers(SecurityFrameworkUtils.getLoginUserId(), keyword, limit);
        return success(AppSearchUserRespVO.from(list));
    }

    @GetMapping("/recommend")
    @Operation(summary = "可能认识的人")
    public CommonResult<List<AppPotentialFriendRespVO>> recommend(@RequestParam(value = "limit", defaultValue = "20") Integer limit) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        List<MemberPotentialFriendBO> list = relationService.getPotentialFriends(userId, limit);
        Map<Long, MemberRelationSummary> relationMap = relationService.getRelationSummary(userId,
                list.stream().map(MemberPotentialFriendBO::getTargetUserId).toList());
        List<MemberUserDO> users = userService.getUserList(list.stream()
                .map(MemberPotentialFriendBO::getTargetUserId).toList());
        return success(AppPotentialFriendRespVO.from(list, users, relationMap));
    }

    @GetMapping("/relation")
    @Operation(summary = "获取关系概况")
    public CommonResult<MemberRelationSummary> relation(@RequestParam("targetUserId") @NotNull Long targetUserId) {
        MemberRelationSummary summary = relationService.getRelation(SecurityFrameworkUtils.getLoginUserId(), targetUserId);
        return success(summary);
    }

    @GetMapping("/following")
    @Operation(summary = "获取关注列表")
    public CommonResult<List<AppSocialUserRespVO>> getFollowingList(
            @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        List<MemberSocialUserBO> list = relationService.getFollowingList(userId, pageNo, pageSize);
        return success(AppSocialUserRespVO.fromBOList(list));
    }

    @GetMapping("/followers")
    @Operation(summary = "获取粉丝列表")
    public CommonResult<List<AppSocialUserRespVO>> getFollowersList(
            @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        List<MemberSocialUserBO> list = relationService.getFollowersList(userId, pageNo, pageSize);
        return success(AppSocialUserRespVO.fromBOList(list));
    }
}
