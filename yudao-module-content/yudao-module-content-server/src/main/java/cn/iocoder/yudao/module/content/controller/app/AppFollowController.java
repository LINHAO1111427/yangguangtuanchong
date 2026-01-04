package cn.iocoder.yudao.module.content.controller.app;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.content.controller.app.vo.FollowUserRespVO;
import cn.iocoder.yudao.module.content.service.follow.FollowService;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 APP - 关注/粉丝")
@Validated
@RestController
@RequestMapping("/content/follow")
public class AppFollowController {

    @Resource
    private FollowService followService;
    @Resource
    private MemberUserApi memberUserApi;

    @PostMapping("/user")
    @Operation(summary = "关注用户")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<Boolean> followUser(@NotNull @RequestParam("target_id") Long targetId,
                                            @RequestParam(value = "remark", required = false) String remark) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        boolean ok = followService.followUser(userId, targetId, remark);
        return success(ok);
    }

    @DeleteMapping("/user")
    @Operation(summary = "取消关注用户")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<Boolean> unfollowUser(@NotNull @RequestParam("target_id") Long targetId) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        boolean ok = followService.unfollowUser(userId, targetId);
        return success(ok);
    }

    @PostMapping("/topic")
    @Operation(summary = "关注话题")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<Boolean> followTopic(@NotNull @RequestParam("topic_id") Long topicId) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        return success(followService.followTopic(userId, topicId));
    }

    @DeleteMapping("/topic")
    @Operation(summary = "取消关注话题")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<Boolean> unfollowTopic(@NotNull @RequestParam("topic_id") Long topicId) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        return success(followService.unfollowTopic(userId, topicId));
    }

    @GetMapping("/users")
    @Operation(summary = "我的关注列表")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<PageResult<FollowUserRespVO>> listFollowing(
            @RequestParam(value = "page", defaultValue = "1") @Min(1) Integer page,
            @RequestParam(value = "size", defaultValue = "20") @Min(1) Integer size) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        return success(buildUserPage(followService.getFollowingUserIds(userId), page, size, true));
    }

    @GetMapping("/fans")
    @Operation(summary = "我的粉丝列表")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<PageResult<FollowUserRespVO>> listFans(
            @RequestParam(value = "page", defaultValue = "1") @Min(1) Integer page,
            @RequestParam(value = "size", defaultValue = "20") @Min(1) Integer size) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        return success(buildUserPage(followService.getFansUserIds(userId), page, size, false));
    }

    private PageResult<FollowUserRespVO> buildUserPage(List<Long> ids, Integer page, Integer size, boolean followingList) {
        if (CollUtil.isEmpty(ids)) {
            return PageResult.empty();
        }
        int pageNo = Math.max(1, page);
        int pageSize = Math.max(1, size);
        int from = Math.min((pageNo - 1) * pageSize, ids.size());
        int to = Math.min(from + pageSize, ids.size());
        List<Long> pageIds = ids.subList(from, to);
        List<MemberUserRespDTO> users = memberUserApi.getUserList(pageIds).getCheckedData();
        Map<Long, MemberUserRespDTO> mapped = users == null ? Collections.emptyMap()
                : users.stream().collect(Collectors.toMap(MemberUserRespDTO::getId, Function.identity()));

        Set<Long> myFollowing = followingList ? Collections.emptySet()
                : followService.getFollowingUserIds(SecurityFrameworkUtils.getLoginUserId()).stream().collect(Collectors.toSet());

        List<FollowUserRespVO> data = pageIds.stream().map(id -> {
            MemberUserRespDTO user = mapped.get(id);
            FollowUserRespVO vo = new FollowUserRespVO();
            vo.setUserId(id);
            vo.setNickname(user != null ? user.getNickname() : null);
            vo.setAvatar(user != null ? user.getAvatar() : null);
            vo.setMutual(myFollowing.contains(id));
            return vo;
        }).toList();
        return new PageResult<>(data, (long) ids.size());
    }
}
