package cn.iocoder.yudao.module.system.controller.app.notice;

import cn.hutool.core.text.CharSequenceUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.string.StrUtils;
import cn.iocoder.yudao.module.system.controller.admin.notice.vo.NoticePageReqVO;
import cn.iocoder.yudao.module.system.controller.app.notice.vo.AppNoticeRespVO;
import cn.iocoder.yudao.module.system.controller.app.notice.vo.NoticeReadReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.notice.NoticeDO;
import cn.iocoder.yudao.module.system.service.notice.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "用户 APP - 系统公告")
@RestController
@RequestMapping("/api/v1.0.1/notice")
@Validated
public class AppNoticeController {

    private static final String READ_KEY_PREFIX = "system:notice:read:";

    @Resource
    private NoticeService noticeService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @GetMapping("/list")
    @PermitAll
    @Operation(summary = "公告列表")
    public CommonResult<PageResult<AppNoticeRespVO>> listNotices(
            @RequestParam(value = "page", defaultValue = "1") @Min(1) Integer page,
            @RequestParam(value = "limit", defaultValue = "20") @Min(1) Integer limit) {
        NoticePageReqVO reqVO = new NoticePageReqVO();
        reqVO.setPageNo(page);
        reqVO.setPageSize(limit);
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());

        PageResult<NoticeDO> pageResult = noticeService.getNoticePage(reqVO);
        Set<String> readIds = loadReadIds();

        List<AppNoticeRespVO> list = pageResult.getList().stream()
                .map(notice -> convert(notice, readIds))
                .collect(Collectors.toList());

        PageResult<AppNoticeRespVO> result = new PageResult<>(list, pageResult.getTotal());
        return success(result);
    }

    @PostMapping("/read")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "标记公告已读")
    public CommonResult<Boolean> markRead(@RequestBody @Valid NoticeReadReqVO reqVO) {
        // 校验公告存在
        noticeService.validateNoticeExists(reqVO.getNoticeId());

        Long userId = getLoginUserId();
        if (userId != null) {
            stringRedisTemplate.opsForSet().add(buildKey(userId), String.valueOf(reqVO.getNoticeId()));
            stringRedisTemplate.expire(buildKey(userId), Duration.ofDays(30));
        }
        return success(Boolean.TRUE);
    }

    private AppNoticeRespVO convert(NoticeDO notice, Set<String> readIds) {
        AppNoticeRespVO vo = new AppNoticeRespVO();
        vo.setId(notice.getId());
        vo.setTitle(notice.getTitle());
        String content = notice.getContent();
        vo.setContent(content);
        vo.setSummary(StrUtils.maxLength(CharSequenceUtil.emptyToDefault(content, ""), 80));
        vo.setType(notice.getType());
        vo.setCreateTime(notice.getCreateTime());
        vo.setRead(readIds.contains(String.valueOf(notice.getId())));
        return vo;
    }

    private Set<String> loadReadIds() {
        Long userId = getLoginUserId();
        if (userId == null) {
            return Set.of();
        }
        String key = buildKey(userId);
        Set<String> readIds = stringRedisTemplate.opsForSet().members(key);
        return readIds != null ? readIds : Set.of();
    }

    private String buildKey(Long userId) {
        return READ_KEY_PREFIX + userId;
    }
}
