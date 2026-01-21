package cn.iocoder.yudao.module.member.controller.app.user;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.module.content.api.ContentApi;
import cn.iocoder.yudao.module.content.api.dto.ContentUserStatsRespDTO;
import cn.iocoder.yudao.module.member.controller.app.user.vo.*;
import cn.iocoder.yudao.module.member.dal.dataobject.social.MemberFriendRelationDO;
import cn.iocoder.yudao.module.member.convert.user.MemberUserConvert;
import cn.iocoder.yudao.module.member.dal.dataobject.level.MemberLevelDO;
import cn.iocoder.yudao.module.member.dal.dataobject.user.MemberUserDO;
import cn.iocoder.yudao.module.member.dal.mysql.social.MemberFriendRelationMapper;
import cn.iocoder.yudao.module.member.service.level.MemberLevelService;
import cn.iocoder.yudao.module.member.service.user.MemberUserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.module.member.enums.ErrorCodeConstants.USER_NOT_EXISTS;

@Tag(name = "用户 APP - 用户个人中心")
@RestController
@RequestMapping({"/member/user", "/app-api/member/user"})
@Validated
@Slf4j
public class AppMemberUserController {

    @Resource
    private MemberUserService userService;
    @Resource
    private MemberLevelService levelService;
    @Resource
    private MemberFriendRelationMapper friendRelationMapper;
    @Resource
    private ContentApi contentApi;

    @GetMapping("/get")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获得基本信息")
    public CommonResult<AppMemberUserInfoRespVO> getUserInfo() {
        Long userId = getLoginUserId();
        if (userId == null) {
            return CommonResult.error(GlobalErrorCodeConstants.UNAUTHORIZED);
        }
        MemberUserDO user = userService.getUser(userId);
        if (user == null) {
            throw exception(USER_NOT_EXISTS);
        }
        MemberLevelDO level = levelService.getLevel(user.getLevelId());
        return success(MemberUserConvert.INSTANCE.convert(user, level));
    }

    @PutMapping("/update")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "修改基本信息")
    public CommonResult<Boolean> updateUser(@RequestBody @Valid AppMemberUserUpdateReqVO reqVO) {
        Long userId = getLoginUserId();
        if (userId == null) {
            return CommonResult.error(GlobalErrorCodeConstants.UNAUTHORIZED);
        }
        userService.updateUser(userId, reqVO);
        return success(true);
    }

    @PutMapping("/update-mobile")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "修改用户手机")
    public CommonResult<Boolean> updateUserMobile(@RequestBody @Valid AppMemberUserUpdateMobileReqVO reqVO) {
        Long userId = getLoginUserId();
        if (userId == null) {
            return CommonResult.error(GlobalErrorCodeConstants.UNAUTHORIZED);
        }
        userService.updateUserMobile(userId, reqVO);
        return success(true);
    }

    @PutMapping("/update-mobile-by-weixin")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "基于微信小程序的授权码，修改用户手机")
    public CommonResult<Boolean> updateUserMobileByWeixin(@RequestBody @Valid AppMemberUserUpdateMobileByWeixinReqVO reqVO) {
        Long userId = getLoginUserId();
        if (userId == null) {
            return CommonResult.error(GlobalErrorCodeConstants.UNAUTHORIZED);
        }
        userService.updateUserMobileByWeixin(userId, reqVO);
        return success(true);
    }

    @PutMapping("/update-password")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "修改用户密码", description = "用户修改密码时使用")
    public CommonResult<Boolean> updateUserPassword(@RequestBody @Valid AppMemberUserUpdatePasswordReqVO reqVO) {
        Long userId = getLoginUserId();
        if (userId == null) {
            return CommonResult.error(GlobalErrorCodeConstants.UNAUTHORIZED);
        }
        userService.updateUserPassword(userId, reqVO);
        return success(true);
    }

    @PutMapping("/reset-password")
    @Operation(summary = "重置密码", description = "用户忘记密码时使用")
    @PermitAll
    public CommonResult<Boolean> resetUserPassword(@RequestBody @Valid AppMemberUserResetPasswordReqVO reqVO) {
        userService.resetUserPassword(reqVO);
        return success(true);
    }

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取用户详情（含统计数据）")
    public CommonResult<AppMemberUserProfileRespVO> getUserProfile() {
        Long userId = getLoginUserId();
        if (userId == null) {
            return CommonResult.error(GlobalErrorCodeConstants.UNAUTHORIZED);
        }
        MemberUserDO user = userService.getUser(userId);
        if (user == null) {
            throw exception(USER_NOT_EXISTS);
        }
        MemberLevelDO level = levelService.getLevel(user.getLevelId());

        // 构建返回对象
        AppMemberUserProfileRespVO profile = new AppMemberUserProfileRespVO();
        profile.setId(user.getId());
        profile.setCustomId(user.getCustomId() != null ? String.valueOf(user.getCustomId()) : null);
        profile.setNickname(user.getNickname());
        profile.setAvatar(user.getAvatar());
        profile.setMobile(maskMobile(user.getMobile()));
        profile.setSex(user.getSex());
        profile.setBirthday(user.getBirthday() != null ? user.getBirthday().toLocalDate().toString() : null);
        profile.setBio(user.getMark());
        profile.setBackgroundUrl(user.getBackgroundUrl());
        profile.setRegion(user.getRegion());
        profile.setOccupation(user.getOccupation());
        profile.setSchool(user.getSchool());
        profile.setOriginalVerifyStatus(user.getOriginalVerifyStatus());
        profile.setPoint(user.getPoint());
        profile.setExperience(user.getExperience());
        profile.setIsVip(false); // TODO: 从会员服务获取VIP状态

        if (level != null) {
            AppMemberUserInfoRespVO.Level levelVO = new AppMemberUserInfoRespVO.Level();
            levelVO.setId(level.getId());
            levelVO.setName(level.getName());
            levelVO.setLevel(level.getLevel());
            levelVO.setIcon(level.getIcon());
            profile.setLevel(levelVO);
        }

        // 获取社交统计数据
        AppMemberUserProfileRespVO.Stats stats = new AppMemberUserProfileRespVO.Stats();
        stats.setFriendsCount(defaultLong(friendRelationMapper.selectMutualFriendCount(userId)));
        ContentUserStatsRespDTO contentStats = loadContentStats(userId);
        if (contentStats != null) {
            stats.setFollowingCount(defaultLong(contentStats.getFollowingCount()));
            stats.setFollowersCount(defaultLong(contentStats.getFollowersCount()));
            stats.setLikesCount(defaultLong(contentStats.getTotalLikeCount()));
            stats.setWorksCount(defaultLong(contentStats.getWorkCount()));
            stats.setWishlistCount(defaultLong(contentStats.getTotalCollectCount()));
            stats.setFootprintCount(defaultLong(contentStats.getTotalViewCount()));
            stats.setCommentCount(defaultLong(contentStats.getTotalCommentCount()));
        } else {
            stats.setFollowingCount(friendRelationMapper.selectCount(
                    new LambdaQueryWrapper<MemberFriendRelationDO>()
                            .eq(MemberFriendRelationDO::getUserId, userId)
                            .eq(MemberFriendRelationDO::getState, 1)));
            stats.setFollowersCount(friendRelationMapper.selectCount(
                    new LambdaQueryWrapper<MemberFriendRelationDO>()
                            .eq(MemberFriendRelationDO::getFriendId, userId)
                            .eq(MemberFriendRelationDO::getState, 1)));
            stats.setLikesCount(0L);
            stats.setWorksCount(0L);
            stats.setWishlistCount(0L);
            stats.setFootprintCount(0L);
            stats.setCommentCount(0L);
        }
        profile.setStats(stats);

        return success(profile);
    }

    private String maskMobile(String mobile) {
        if (mobile == null || mobile.length() < 11) {
            return mobile;
        }
        return mobile.substring(0, 3) + "****" + mobile.substring(7);
    }

    private ContentUserStatsRespDTO loadContentStats(Long userId) {
        try {
            CommonResult<ContentUserStatsRespDTO> result = contentApi.getAuthorStats(userId);
            return result != null ? result.getData() : null;
        } catch (Exception ex) {
            log.warn("Load content stats failed userId={}", userId, ex);
            return null;
        }
    }

    private long defaultLong(Long value) {
        return value == null ? 0L : value;
    }

}
