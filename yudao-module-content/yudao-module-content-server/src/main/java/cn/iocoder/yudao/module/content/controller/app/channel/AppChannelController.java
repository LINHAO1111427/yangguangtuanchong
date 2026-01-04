package cn.iocoder.yudao.module.content.controller.app.channel;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.content.controller.app.channel.vo.ChannelBoardRespVO;
import cn.iocoder.yudao.module.content.controller.app.channel.vo.ChannelBoardUpdateReqVO;
import cn.iocoder.yudao.module.content.controller.app.channel.vo.ChannelSimpleRespVO;
import cn.iocoder.yudao.module.content.controller.app.channel.vo.ChannelVisitRespVO;
import cn.iocoder.yudao.module.content.dal.dataobject.ContentChannelDO;
import cn.iocoder.yudao.module.content.service.channel.ContentChannelService;
import cn.iocoder.yudao.module.content.service.channel.bo.ChannelVisitInsight;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 APP - 频道管理")
@Validated
@RestController
@RequestMapping("/content/channel")
public class AppChannelController {

    @Resource
    private ContentChannelService channelService;

    @GetMapping("/board")
    @Operation(summary = "获取频道看板（我的频道+推荐频道）")
    public CommonResult<ChannelBoardRespVO> getChannelBoard() {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        List<ContentChannelDO> myChannels = channelService.getUserChannels(userId);
        List<ContentChannelDO> recommendChannels = channelService.getRecommendChannels(userId)
                .stream()
                .filter(channel -> myChannels.stream().noneMatch(my -> my.getId().equals(channel.getId())))
                .collect(Collectors.toList());
        ChannelBoardRespVO respVO = new ChannelBoardRespVO();
        respVO.setMyChannels(convert(myChannels));
        respVO.setRecommendChannels(convert(recommendChannels));
        return success(respVO);
    }

    @PutMapping("/board")
    @Operation(summary = "更新我的频道配置")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<Boolean> updateMyChannels(@Valid @RequestBody ChannelBoardUpdateReqVO reqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        channelService.updateUserChannels(userId, reqVO.getChannelIds());
        return success(Boolean.TRUE);
    }

    @GetMapping("/frequent")
    @Operation(summary = "获取常访问频道摘要")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<List<ChannelVisitRespVO>> getFrequentChannels(
            @RequestParam(value = "limit", defaultValue = "4") @Min(1) @Max(10) Integer limit,
            @RequestParam(value = "preview_size", defaultValue = "3") @Min(1) @Max(5) Integer previewSize) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        List<ChannelVisitRespVO> resp = channelService.getFrequentChannelInsights(userId, limit, previewSize)
                .stream()
                .map(this::convertVisit)
                .collect(Collectors.toList());
        return success(resp);
    }

    @PutMapping("/add")
    @Operation(summary = "添加一个频道到我的频道")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<Boolean> addChannel(@RequestParam("channel_id") Long channelId) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        channelService.addUserChannel(userId, channelId);
        return success(Boolean.TRUE);
    }

    @PutMapping("/remove")
    @Operation(summary = "从我的频道移除一个频道")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<Boolean> removeChannel(@RequestParam("channel_id") Long channelId) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        channelService.removeUserChannel(userId, channelId);
        return success(Boolean.TRUE);
    }

    private List<ChannelSimpleRespVO> convert(List<ContentChannelDO> channels) {
        return channels.stream().map(this::convert).collect(Collectors.toList());
    }

    private ChannelSimpleRespVO convert(ContentChannelDO channel) {
        ChannelSimpleRespVO vo = new ChannelSimpleRespVO();
        vo.setId(channel.getId());
        vo.setCode(channel.getCode());
        vo.setName(channel.getName());
        vo.setDescription(channel.getDescription());
        vo.setIcon(channel.getIcon());
        vo.setColor(channel.getColor());
        vo.setIsDefault(channel.getIsDefault());
        vo.setIsRequired(channel.getIsRequired());
        return vo;
    }

    private ChannelVisitRespVO convertVisit(ChannelVisitInsight insight) {
        ChannelVisitRespVO vo = new ChannelVisitRespVO();
        vo.setChannelId(insight.getChannelId());
        vo.setChannelName(insight.getChannelName());
        vo.setIcon(insight.getChannelIcon());
        vo.setColor(insight.getChannelColor());
        vo.setDescription(insight.getChannelDescription());
        vo.setVisitCount(insight.getVisitCount());
        vo.setLastVisitTime(insight.getLastVisitTime());
        if (insight.getPreviews() != null) {
            vo.setPreviews(insight.getPreviews().stream().map(pre -> {
                ChannelVisitRespVO.ContentPreview preview = new ChannelVisitRespVO.ContentPreview();
                preview.setContentId(pre.getContentId());
                preview.setTitle(pre.getTitle());
                preview.setCoverImage(pre.getCoverImage());
                preview.setVideoUrl(pre.getVideoUrl());
                preview.setLikeCount(pre.getLikeCount());
                preview.setViewCount(pre.getViewCount());
                preview.setPublishTime(pre.getPublishTime());
                return preview;
            }).collect(Collectors.toList()));
        }
        return vo;
    }
}
