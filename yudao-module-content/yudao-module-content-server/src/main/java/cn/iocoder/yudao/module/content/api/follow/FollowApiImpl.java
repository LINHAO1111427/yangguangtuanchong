package cn.iocoder.yudao.module.content.api.follow;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.content.service.follow.FollowService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 关注关系 RPC 实现
 *
 * @author Lin
 */
@RestController
@Validated
public class FollowApiImpl implements FollowApi {

    @Resource
    private FollowService followService;

    @Override
    public CommonResult<Boolean> isFollowingUser(Long followerId, Long targetId) {
        return success(followService.isFollowingUser(followerId, targetId));
    }
}
