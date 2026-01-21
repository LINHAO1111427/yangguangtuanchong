package cn.iocoder.yudao.module.content.api.follow;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.content.enums.ApiConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 关注关系 RPC 接口
 *
 * @author Lin
 */
@FeignClient(name = ApiConstants.NAME)
@Tag(name = "RPC 服务 - 关注关系")
public interface FollowApi {

    String PREFIX = ApiConstants.PREFIX + "/follow";

    @GetMapping(PREFIX + "/is-following")
    @Operation(summary = "判断用户是否已关注目标用户")
    CommonResult<Boolean> isFollowingUser(@RequestParam("followerId") Long followerId,
                                          @RequestParam("targetId") Long targetId);

    @PostMapping(PREFIX + "/follow-user")
    @Operation(summary = "Follow user")
    CommonResult<Boolean> followUser(@RequestParam("followerId") Long followerId,
                                     @RequestParam("targetId") Long targetId,
                                     @RequestParam(value = "remark", required = false) String remark);

    @PostMapping(PREFIX + "/unfollow-user")
    @Operation(summary = "Unfollow user")
    CommonResult<Boolean> unfollowUser(@RequestParam("followerId") Long followerId,
                                       @RequestParam("targetId") Long targetId);

}
