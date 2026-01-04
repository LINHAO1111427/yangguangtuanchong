package cn.iocoder.yudao.module.pay.api.reward;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.pay.enums.ApiConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = ApiConstants.NAME)
@Tag(name = "RPC 服务 - 打赏")
public interface PayRewardApi {

    String PREFIX = ApiConstants.PREFIX + "/reward";

    @GetMapping(PREFIX + "/total-amount-by-post")
    @Operation(summary = "查询作品的打赏总金额")
    @Parameter(name = "postId", description = "作品ID", required = true, example = "1024")
    CommonResult<Integer> getTotalRewardAmountByPost(@RequestParam("postId") Long postId);

    @GetMapping(PREFIX + "/total-amount-by-author")
    @Operation(summary = "查询作者收益总金额")
    @Parameter(name = "authorId", description = "作者ID", required = true, example = "1024")
    CommonResult<Integer> getTotalIncomeAmount(@RequestParam("authorId") Long authorId);

}
