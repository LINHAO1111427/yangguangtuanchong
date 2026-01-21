package cn.iocoder.yudao.module.member.api.social;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.member.api.social.dto.MemberRelationRespDTO;
import cn.iocoder.yudao.module.member.enums.ApiConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = ApiConstants.NAME)
@Tag(name = "RPC 服务 - 会员社交关系")
public interface MemberRelationApi {

    String PREFIX = ApiConstants.PREFIX + "/relation";

    @GetMapping(PREFIX + "/get")
    @Operation(summary = "获取用户关系概况")
    @Parameter(name = "userId", description = "当前用户编号", required = true, example = "1024")
    @Parameter(name = "targetUserId", description = "目标用户编号", required = true, example = "2048")
    CommonResult<MemberRelationRespDTO> getRelation(@RequestParam("userId") Long userId,
                                                    @RequestParam("targetUserId") Long targetUserId);
}
