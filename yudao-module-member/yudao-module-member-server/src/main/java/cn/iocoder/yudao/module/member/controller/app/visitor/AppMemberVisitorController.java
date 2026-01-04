package cn.iocoder.yudao.module.member.controller.app.visitor;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.member.controller.app.visitor.vo.AppMemberVisitorRecordReqVO;
import cn.iocoder.yudao.module.member.controller.app.visitor.vo.AppMemberVisitorStatsRespVO;
import cn.iocoder.yudao.module.member.service.visitor.MemberVisitorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "用户 APP - 访客")
@RestController
@RequestMapping("/member/visitor")
@Validated
public class AppMemberVisitorController {

    @Resource
    private MemberVisitorService memberVisitorService;

    @PostMapping("/record")
    @Operation(summary = "记录访问")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<Boolean> recordVisit(@Valid @RequestBody AppMemberVisitorRecordReqVO reqVO) {
        Long visitorId = getLoginUserId();
        memberVisitorService.recordVisit(reqVO.getOwnerId(), visitorId, reqVO.getVisitType(), reqVO.getTargetId());
        return success(Boolean.TRUE);
    }

    @GetMapping("/stats")
    @Operation(summary = "获取我的访客统计")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<AppMemberVisitorStatsRespVO> getMyVisitorStats(
            @RequestParam(value = "days", required = false) Integer days) {
        Long ownerId = getLoginUserId();
        return success(memberVisitorService.getMyVisitorStats(ownerId, days));
    }
}

