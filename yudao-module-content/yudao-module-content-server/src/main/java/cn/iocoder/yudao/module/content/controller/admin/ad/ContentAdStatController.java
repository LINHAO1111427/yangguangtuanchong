package cn.iocoder.yudao.module.content.controller.admin.ad;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.content.controller.admin.ad.vo.ContentAdStatPageReqVO;
import cn.iocoder.yudao.module.content.controller.admin.ad.vo.ContentAdStatRespVO;
import cn.iocoder.yudao.module.content.controller.admin.ad.vo.ContentAdStatSummaryReqVO;
import cn.iocoder.yudao.module.content.controller.admin.ad.vo.ContentAdStatSummaryRespVO;
import cn.iocoder.yudao.module.content.service.ad.ContentAdEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "Admin - content ad stats")
@RestController
@RequestMapping("/content/ad/stat")
@Validated
public class ContentAdStatController {

    @Resource
    private ContentAdEventService contentAdEventService;

    @GetMapping("/page")
    @Operation(summary = "Get ad stat page")
    @PreAuthorize("@ss.hasPermission('content:ad:stat:query')")
    public CommonResult<PageResult<ContentAdStatRespVO>> getStatPage(@Valid ContentAdStatPageReqVO reqVO) {
        return success(contentAdEventService.getStatPage(reqVO));
    }

    @GetMapping("/summary")
    @Operation(summary = "Get ad stat summary")
    @PreAuthorize("@ss.hasPermission('content:ad:stat:query')")
    public CommonResult<ContentAdStatSummaryRespVO> getStatSummary(@Valid ContentAdStatSummaryReqVO reqVO) {
        return success(contentAdEventService.getStatSummary(
                reqVO.getAdId(), reqVO.getScene(), reqVO.getStartTime(), reqVO.getEndTime()));
    }
}
