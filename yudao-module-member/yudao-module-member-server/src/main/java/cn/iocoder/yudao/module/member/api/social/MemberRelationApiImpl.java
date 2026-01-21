package cn.iocoder.yudao.module.member.api.social;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.member.api.social.dto.MemberRelationRespDTO;
import cn.iocoder.yudao.module.member.service.social.MemberRelationService;
import cn.iocoder.yudao.module.member.service.social.bo.MemberRelationSummary;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * RPC 服务 - 用户关系概况
 */
@RestController
@Validated
public class MemberRelationApiImpl implements MemberRelationApi {

    @Resource
    private MemberRelationService relationService;

    @Override
    public CommonResult<MemberRelationRespDTO> getRelation(Long userId, Long targetUserId) {
        MemberRelationSummary summary = relationService.getRelation(userId, targetUserId);
        MemberRelationRespDTO resp = new MemberRelationRespDTO();
        if (summary != null) {
            resp.setFollowing(summary.isFollowing());
            resp.setFollower(summary.isFollower());
            resp.setMutualFollow(summary.isMutualFollow());
            resp.setBlockedByMe(summary.isBlockedByMe());
            resp.setBlockedMe(summary.isBlockedMe());
            resp.setCanMessage(summary.isCanMessage());
            resp.setFollowState(summary.getFollowState());
            resp.setNeedApproval(summary.isNeedApproval());
        }
        return success(resp);
    }
}
