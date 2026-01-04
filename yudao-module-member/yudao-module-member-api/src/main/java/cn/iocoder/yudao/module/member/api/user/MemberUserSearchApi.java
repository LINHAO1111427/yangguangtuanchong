package cn.iocoder.yudao.module.member.api.user;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import cn.iocoder.yudao.module.member.enums.ApiConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = ApiConstants.NAME)
@Tag(name = "RPC 服务 - 会员用户搜索")
public interface MemberUserSearchApi {

    String PREFIX = ApiConstants.PREFIX + "/user";

    @GetMapping(PREFIX + "/search")
    @Operation(summary = "根据昵称模糊搜索用户")
    @Parameter(name = "keyword", description = "昵称关键字", required = true, example = "小绿")
    CommonResult<List<MemberUserRespDTO>> searchUsers(@RequestParam("keyword") String keyword,
                                                      @RequestParam(value = "limit", required = false, defaultValue = "20") Integer limit);
}
