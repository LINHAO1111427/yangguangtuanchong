package cn.iocoder.yudao.module.content.controller.app.ad;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.content.controller.app.ad.vo.AppAdCreateReqVO;
import cn.iocoder.yudao.module.content.controller.app.ad.vo.AppAdDetailRespVO;
import cn.iocoder.yudao.module.content.controller.app.ad.vo.AppAdRespVO;
import cn.iocoder.yudao.module.content.controller.app.ad.vo.AppAdStatRespVO;
import cn.iocoder.yudao.module.content.controller.app.ad.vo.AppAdUpdateReqVO;
import cn.iocoder.yudao.module.content.controller.admin.ad.vo.ContentAdStatSummaryRespVO;
import cn.iocoder.yudao.module.content.dal.dataobject.ContentAdDO;
import cn.iocoder.yudao.module.content.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.content.service.ad.ContentAdEventService;
import cn.iocoder.yudao.module.content.service.ad.ContentAdService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "用户 APP - 广告")
@RestController
@RequestMapping({"/ad", "/api/v1.0.1/ad", "/app-api/ad"})
@Validated
public class AppAdController {

    private static final Logger log = LoggerFactory.getLogger(AppAdController.class);

    @Resource
    private ContentAdService contentAdService;
    @Resource
    private ContentAdEventService contentAdEventService;

    @GetMapping("/list")
    @PermitAll
    @Operation(summary = "获取广告列表")
    public CommonResult<List<AppAdRespVO>> listAds(
            @RequestParam(value = "scene", required = false) Integer scene,
            @RequestParam(value = "limit", defaultValue = "20") @Min(1) @Max(100) Integer limit,
            @RequestParam(value = "keyword", required = false) String keyword) {
        Long userId = getLoginUserId();
        List<ContentAdDO> ads = contentAdService.pickAds(userId, scene, limit);
        if (CollUtil.isEmpty(ads)) {
            ads = contentAdService.getActiveAds(scene);
        }
        if (keyword != null) {
            String lower = keyword.toLowerCase();
            ads = ads.stream()
                    .filter(ad -> (ad.getTitle() != null && ad.getTitle().toLowerCase().contains(lower))
                            || (ad.getSubTitle() != null && ad.getSubTitle().toLowerCase().contains(lower)))
                    .collect(Collectors.toList());
        }
        if (ads.size() > limit) {
            ads = ads.subList(0, limit);
        }
        List<AppAdRespVO> resp = ads.stream().map(this::convert).collect(Collectors.toList());
        return success(resp);
    }

    @PostMapping("/click")
    @PermitAll
    @Operation(summary = "上报广告点击")
    public CommonResult<Boolean> click(@RequestBody Map<String, Object> body) {
        Object adIdObj = body.get("ad_id");
        Long adId = null;
        if (adIdObj instanceof Number number) {
            adId = number.longValue();
        } else if (adIdObj instanceof String str) {
            try {
                adId = Long.parseLong(str);
            } catch (NumberFormatException ignored) {
                adId = null;
            }
        }
        Integer scene = null;
        Object sceneObj = body.get("scene");
        if (sceneObj instanceof Number number) {
            scene = number.intValue();
        } else if (sceneObj instanceof String str) {
            try {
                scene = Integer.parseInt(str);
            } catch (NumberFormatException ignored) {
                scene = null;
            }
        }
        log.info("[adClick] userId={} adId={}", getLoginUserId(), adIdObj);
        if (adId != null) {
            contentAdEventService.recordClick(adId, getLoginUserId(), scene);
        }
        return success(Boolean.TRUE);
    }

    @GetMapping("/get")
    @PermitAll
    @Operation(summary = "获取广告详情")
    public CommonResult<AppAdDetailRespVO> getAd(@RequestParam("id") Long id) {
        ContentAdDO ad = contentAdService.getAd(id);
        if (ad == null || !Objects.equals(ad.getDeleted(), 0)) {
            throw exception(ErrorCodeConstants.AD_NOT_EXISTS);
        }
        return success(convertDetail(ad));
    }

    @GetMapping("/stats")
    @Operation(summary = "获取广告统计")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<AppAdStatRespVO> getAdStats(
            @RequestParam("id") Long id,
            @RequestParam(value = "scene", required = false) Integer scene) {
        Long userId = getLoginUserId();
        ContentAdDO ad = contentAdService.getAd(id);
        if (ad == null || !Objects.equals(ad.getDeleted(), 0)) {
            throw exception(ErrorCodeConstants.AD_NOT_EXISTS);
        }
        Long publisherUserId = getPublisherUserId(ad);
        if (publisherUserId != null && !Objects.equals(publisherUserId, userId)) {
            throw exception(ErrorCodeConstants.AD_ACCESS_DENIED);
        }
        ContentAdStatSummaryRespVO summary = contentAdEventService.getStatSummary(id, scene, null, null);
        return success(convertStat(summary));
    }

    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "创建广告")
    public CommonResult<Long> create(@Valid @RequestBody AppAdCreateReqVO reqVO) {
        Long userId = getLoginUserId();
        ContentAdDO ad = new ContentAdDO();
        ad.setTitle(reqVO.getTitle());
        ad.setSubTitle(reqVO.getDesc());
        ad.setCardType(reqVO.getCardType());
        ad.setMediaType(reqVO.getMediaType());
        ad.setCoverImage(reqVO.getCoverImage());
        ad.setVideoUrl(reqVO.getVideoUrl());
        ad.setJumpUrl(reqVO.getLink());
        ad.setCallToAction(reqVO.getCallToAction());
        ad.setAdvertiserName(reqVO.getAdvertiserName());
        ad.setStatus(0);
        ad.setPriority(reqVO.getPriority() == null ? 0 : reqVO.getPriority());
        Integer displayScene = reqVO.getScene();
        if (displayScene == null || displayScene < 1 || displayScene > 3) {
            displayScene = 1;
        }
        ad.setDisplayScene(displayScene);
        ad.setFrequencyCap(reqVO.getFrequencyCap());
        ad.setStartTime(reqVO.getStartTime());
        ad.setEndTime(reqVO.getEndTime());

        Map<String, Object> styleMeta = new HashMap<>();
        if (reqVO.getStyleMeta() != null) {
            styleMeta.putAll(reqVO.getStyleMeta());
        }
        if (userId != null) {
            styleMeta.put("publisherUserId", userId);
        }
        if (reqVO.getCategoryName() != null) {
            styleMeta.put("categoryName", reqVO.getCategoryName());
        }
        if (reqVO.getTargetLocation() != null) {
            styleMeta.put("targetLocation", reqVO.getTargetLocation());
        }
        ad.setStyleMeta(styleMeta.isEmpty() ? null : styleMeta);

        Long id = contentAdService.createAd(ad);
        return success(id);
    }

    @PutMapping("/update")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "更新广告")
    public CommonResult<Boolean> update(@Valid @RequestBody AppAdUpdateReqVO reqVO) {
        Long userId = getLoginUserId();
        ContentAdDO existing = contentAdService.getAd(reqVO.getId());
        if (existing == null || !Objects.equals(existing.getDeleted(), 0)) {
            throw exception(ErrorCodeConstants.AD_NOT_EXISTS);
        }
        Long publisherUserId = getPublisherUserId(existing);
        if (publisherUserId != null && !Objects.equals(publisherUserId, userId)) {
            throw exception(ErrorCodeConstants.AD_ACCESS_DENIED);
        }

        existing.setTitle(reqVO.getTitle());
        existing.setSubTitle(reqVO.getDesc());
        existing.setCardType(reqVO.getCardType());
        existing.setMediaType(reqVO.getMediaType());
        existing.setCoverImage(reqVO.getCoverImage());
        existing.setVideoUrl(reqVO.getVideoUrl());
        existing.setJumpUrl(reqVO.getLink());
        existing.setCallToAction(reqVO.getCallToAction());
        existing.setAdvertiserName(reqVO.getAdvertiserName());
        existing.setStatus(0);
        existing.setPriority(reqVO.getPriority());
        Integer nextScene = reqVO.getScene();
        if (nextScene != null && nextScene >= 1 && nextScene <= 3) {
            existing.setDisplayScene(nextScene);
        }
        existing.setFrequencyCap(reqVO.getFrequencyCap());
        existing.setStartTime(reqVO.getStartTime());
        existing.setEndTime(reqVO.getEndTime());

        Map<String, Object> styleMeta = existing.getStyleMeta() == null ? new HashMap<>() : new HashMap<>(existing.getStyleMeta());
        if (reqVO.getStyleMeta() != null) {
            styleMeta.putAll(reqVO.getStyleMeta());
        }
        if (publisherUserId != null) {
            styleMeta.put("publisherUserId", publisherUserId);
        }
        if (reqVO.getCategoryName() != null) {
            styleMeta.put("categoryName", reqVO.getCategoryName());
        }
        if (reqVO.getTargetLocation() != null) {
            styleMeta.put("targetLocation", reqVO.getTargetLocation());
        }
        existing.setStyleMeta(styleMeta.isEmpty() ? null : styleMeta);

        contentAdService.updateAd(existing);
        return success(Boolean.TRUE);
    }

    @DeleteMapping("/delete")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "删除广告")
    public CommonResult<Boolean> delete(@RequestParam("id") Long id) {
        Long userId = getLoginUserId();
        ContentAdDO existing = contentAdService.getAd(id);
        if (existing == null || !Objects.equals(existing.getDeleted(), 0)) {
            throw exception(ErrorCodeConstants.AD_NOT_EXISTS);
        }
        Long publisherUserId = getPublisherUserId(existing);
        if (publisherUserId != null && !Objects.equals(publisherUserId, userId)) {
            throw exception(ErrorCodeConstants.AD_ACCESS_DENIED);
        }
        contentAdService.deleteAd(id);
        return success(Boolean.TRUE);
    }

    private AppAdRespVO convert(ContentAdDO ad) {
        AppAdRespVO vo = new AppAdRespVO();
        vo.setId(ad.getId());
        vo.setTitle(ad.getTitle());
        vo.setDesc(ad.getSubTitle());
        vo.setImage(ad.getCoverImage());
        vo.setCoverImage(ad.getCoverImage());
        vo.setMediaType(ad.getMediaType());
        vo.setVideoUrl(ad.getVideoUrl());
        vo.setLink(ad.getJumpUrl());
        vo.setScene(ad.getDisplayScene());
        vo.setCategoryName(getStyleMetaString(ad, "categoryName"));
        vo.setPriority(ad.getPriority());
        return vo;
    }

    private AppAdDetailRespVO convertDetail(ContentAdDO ad) {
        AppAdDetailRespVO vo = new AppAdDetailRespVO();
        vo.setId(ad.getId());
        vo.setTitle(ad.getTitle());
        vo.setDesc(ad.getSubTitle());
        vo.setCardType(ad.getCardType());
        vo.setMediaType(ad.getMediaType());
        vo.setCoverImage(ad.getCoverImage());
        vo.setVideoUrl(ad.getVideoUrl());
        vo.setLink(ad.getJumpUrl());
        vo.setCallToAction(ad.getCallToAction());
        vo.setAdvertiserName(ad.getAdvertiserName());
        vo.setScene(ad.getDisplayScene());
        vo.setPriority(ad.getPriority());
        vo.setFrequencyCap(ad.getFrequencyCap());
        vo.setStartTime(ad.getStartTime());
        vo.setEndTime(ad.getEndTime());
        vo.setStyleMeta(ad.getStyleMeta());
        vo.setPublisherUserId(getPublisherUserId(ad));
        vo.setCategoryName(getStyleMetaString(ad, "categoryName"));
        vo.setTargetLocation(getStyleMetaString(ad, "targetLocation"));
        return vo;
    }

    private AppAdStatRespVO convertStat(ContentAdStatSummaryRespVO summary) {
        AppAdStatRespVO vo = new AppAdStatRespVO();
        if (summary == null) {
            return vo;
        }
        vo.setImpressionCount(summary.getImpressionCount());
        vo.setClickCount(summary.getClickCount());
        vo.setUniqueImpressionCount(summary.getUniqueImpressionCount());
        vo.setUniqueClickCount(summary.getUniqueClickCount());
        vo.setRevenue(summary.getRevenue());
        return vo;
    }

    private Long getPublisherUserId(ContentAdDO ad) {
        if (ad == null || ad.getStyleMeta() == null) {
            return null;
        }
        Object value = ad.getStyleMeta().get("publisherUserId");
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String str) {
            try {
                return Long.parseLong(str);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private String getStyleMetaString(ContentAdDO ad, String key) {
        if (ad == null || ad.getStyleMeta() == null || key == null) {
            return null;
        }
        Object value = ad.getStyleMeta().get(key);
        return value == null ? null : String.valueOf(value);
    }
}
