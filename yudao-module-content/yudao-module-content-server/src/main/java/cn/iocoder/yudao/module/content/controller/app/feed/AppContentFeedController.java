package cn.iocoder.yudao.module.content.controller.app.feed;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.content.controller.app.feed.vo.FeedAdRespVO;
import cn.iocoder.yudao.module.content.controller.app.feed.vo.FeedCardRespVO;
import cn.iocoder.yudao.module.content.controller.app.feed.vo.FeedStreamRespVO;
import cn.iocoder.yudao.module.content.controller.app.feed.vo.QuickCollectReqVO;
import cn.iocoder.yudao.module.content.controller.app.feed.vo.RewardSummaryRespVO;
import cn.iocoder.yudao.module.content.controller.app.vo.ContentListRespVO;
import cn.iocoder.yudao.module.content.service.feed.ContentFeedService;
import cn.iocoder.yudao.module.content.service.feed.bo.FeedCardBO;
import cn.iocoder.yudao.module.content.service.feed.bo.FeedStreamRequestBO;
import cn.iocoder.yudao.module.content.service.feed.bo.FeedStreamResultBO;
import cn.iocoder.yudao.module.content.service.feed.ContentFeedService.RewardSummary;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Validated
@Tag(name = "APP - 内容Feed")
@RestController
@RequestMapping("/content/feed")
public class AppContentFeedController {

    @Resource
    private ContentFeedService contentFeedService;

    @GetMapping("/stream")
    @Operation(summary = "获取推荐 Feed 流")
    public CommonResult<FeedStreamRespVO> getFeedStream(
            @RequestParam(value = "page_no", defaultValue = "1") @Min(1) Integer pageNo,
            @RequestParam(value = "page_size", defaultValue = "20") @Min(1) @Max(50) Integer pageSize,
            @RequestParam(value = "scene", defaultValue = "home") String scene,
            @RequestParam(value = "include_ads", defaultValue = "true") boolean includeAds,
            @RequestParam(value = "ad_interval", defaultValue = "5") @Min(3) Integer adInterval) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        FeedStreamRequestBO requestBO = new FeedStreamRequestBO();
        requestBO.setPageNo(pageNo);
        requestBO.setPageSize(pageSize);
        requestBO.setScene(scene);
        requestBO.setIncludeAds(includeAds);
        requestBO.setAdInterval(adInterval);
        FeedStreamResultBO result = contentFeedService.getHomeFeed(userId, requestBO);
        FeedStreamRespVO respVO = new FeedStreamRespVO();
        List<FeedCardRespVO> cards = result.getCards().stream()
                .map(this::convertCard)
                .collect(Collectors.toList());
        respVO.setCards(cards);
        respVO.setTotal(result.getTotal());
        respVO.setStrategySummary(result.getStrategySummary());
        return success(respVO);
    }

    @GetMapping("/history")
    @Operation(summary = "读取浏览记录")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<List<ContentListRespVO>> getBrowseHistory(
            @RequestParam(value = "limit", defaultValue = "20") @Min(1) @Max(100) Integer limit) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        return success(contentFeedService.getBrowseHistory(userId, limit));
    }

    @PostMapping("/quick_collect")
    @Operation(summary = "快捷收藏内容")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<Boolean> quickCollect(@Valid @RequestBody QuickCollectReqVO reqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        boolean collected = contentFeedService.quickCollect(userId, reqVO.getContentId(), reqVO.getGroupId());
        return success(collected);
    }

    @GetMapping("/reward_summary")
    @Operation(summary = "查询内容打赏汇总")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<RewardSummaryRespVO> getRewardSummary(
            @RequestParam("content_id") @NotNull Long contentId,
            @RequestParam(value = "author_id", required = false) Long authorId) {
        RewardSummary summary = contentFeedService.getRewardSummary(contentId, authorId);
        RewardSummaryRespVO respVO = new RewardSummaryRespVO();
        respVO.setPostRewardAmount(summary.getPostAmount());
        respVO.setAuthorIncomeAmount(summary.getAuthorAmount());
        return success(respVO);
    }

    private FeedCardRespVO convertCard(FeedCardBO card) {
        FeedCardRespVO vo = new FeedCardRespVO();
        vo.setCardType(card.getCardType() != null ? card.getCardType().name().toLowerCase() : "content");
        vo.setLayout(card.getLayout());
        vo.setStrategy(card.getStrategy());
        vo.setRewardAmount(card.getRewardAmount());
        vo.setScore(card.getScore());
        if (card.getContent() != null) {
            vo.setContent(card.getContent());
        }
        if (card.getAd() != null) {
            vo.setAd(convertAd(card.getAd()));
        }
        return vo;
    }

    private FeedAdRespVO convertAd(cn.iocoder.yudao.module.content.dal.dataobject.ContentAdDO ad) {
        FeedAdRespVO vo = new FeedAdRespVO();
        vo.setAdId(ad.getId());
        vo.setTitle(ad.getTitle());
        vo.setSubTitle(ad.getSubTitle());
        vo.setCardType(ad.getCardType());
        vo.setMediaType(ad.getMediaType());
        vo.setCoverImage(ad.getCoverImage());
        vo.setVideoUrl(ad.getVideoUrl());
        vo.setJumpUrl(ad.getJumpUrl());
        vo.setCallToAction(ad.getCallToAction());
        vo.setAdvertiserName(ad.getAdvertiserName());
        vo.setStyleMeta(ad.getStyleMeta());
        return vo;
    }
}
