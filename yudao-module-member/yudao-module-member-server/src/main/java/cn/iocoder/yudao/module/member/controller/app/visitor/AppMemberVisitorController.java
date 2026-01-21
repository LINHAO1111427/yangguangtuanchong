package cn.iocoder.yudao.module.member.controller.app.visitor;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.member.controller.app.visitor.vo.AppMemberVisitorLogRespVO;
import cn.iocoder.yudao.module.member.controller.app.visitor.vo.AppMemberVisitorPageReqVO;
import cn.iocoder.yudao.module.member.controller.app.visitor.vo.AppMemberVisitorRecordReqVO;
import cn.iocoder.yudao.module.member.controller.app.visitor.vo.AppMemberVisitorStatsRespVO;
import cn.iocoder.yudao.module.member.dal.dataobject.user.MemberUserDO;
import cn.iocoder.yudao.module.member.dal.dataobject.visitor.MemberVisitorLogDO;
import cn.iocoder.yudao.module.member.service.user.MemberUserService;
import cn.iocoder.yudao.module.member.service.visitor.MemberVisitorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "用户 APP - 访客")
@RestController
@RequestMapping("/member/visitor")
@Validated
public class AppMemberVisitorController {

    @Resource
    private MemberVisitorService memberVisitorService;
    @Resource
    private MemberUserService memberUserService;

    @PostMapping("/record")
    @Operation(summary = "记录访问")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<Boolean> recordVisit(@Valid @RequestBody AppMemberVisitorRecordReqVO reqVO) {
        Long visitorId = getLoginUserId();
        memberVisitorService.recordVisit(reqVO.getOwnerId(), visitorId, reqVO.getVisitType(), reqVO.getTargetId());
        return success(Boolean.TRUE);
    }

    @GetMapping("/page")
    @Operation(summary = "获取访客记录分页")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<PageResult<AppMemberVisitorLogRespVO>> getVisitorPage(@Valid AppMemberVisitorPageReqVO reqVO) {
        Long userId = getLoginUserId();
        PageResult<MemberVisitorLogDO> pageResult = memberVisitorService.getVisitorPage(userId, reqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty());
        }

        boolean asVisitor = Boolean.TRUE.equals(reqVO.getAsVisitor());
        Set<Long> counterpartIds = convertSet(pageResult.getList(),
                asVisitor ? MemberVisitorLogDO::getUserId : MemberVisitorLogDO::getVisitorId);
        Map<Long, MemberUserDO> userMap = convertMap(memberUserService.getUserList(counterpartIds),
                MemberUserDO::getId);

        return success(BeanUtils.toBean(pageResult, AppMemberVisitorLogRespVO.class, vo -> {
            Long counterpartId = asVisitor ? vo.getUserId() : vo.getVisitorId();
            vo.setCounterpartUserId(counterpartId);
            MemberUserDO user = userMap.get(counterpartId);
            if (user != null) {
                vo.setCounterpartNickname(user.getNickname());
                vo.setCounterpartAvatar(user.getAvatar());
            }
        }));
    }

    @DeleteMapping("/clear")
    @Operation(summary = "清空访客记录")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<Boolean> clearVisitorLogs(@RequestParam(value = "asVisitor", required = false) Boolean asVisitor) {
        Long userId = getLoginUserId();
        memberVisitorService.clearVisitorLogs(userId, asVisitor);
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
