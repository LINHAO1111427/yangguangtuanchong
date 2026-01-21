package cn.iocoder.yudao.module.content.controller.admin.ad;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.content.controller.admin.ad.vo.ContentAdDetailRespVO;
import cn.iocoder.yudao.module.content.controller.admin.ad.vo.ContentAdPageReqVO;
import cn.iocoder.yudao.module.content.controller.admin.ad.vo.ContentAdRespVO;
import cn.iocoder.yudao.module.content.controller.admin.ad.vo.ContentAdSaveReqVO;
import cn.iocoder.yudao.module.content.controller.admin.ad.vo.ContentAdUpdateStatusReqVO;
import cn.iocoder.yudao.module.content.dal.dataobject.ContentAdDO;
import cn.iocoder.yudao.module.content.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.content.service.ad.ContentAdService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
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

import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "Admin - content ads")
@RestController
@RequestMapping("/content/ad")
@Validated
public class ContentAdController {

    @Resource
    private ContentAdService contentAdService;

    @GetMapping("/page")
    @Operation(summary = "Get ad page")
    @PreAuthorize("@ss.hasPermission('content:ad:query')")
    public CommonResult<PageResult<ContentAdRespVO>> getAdPage(@Valid ContentAdPageReqVO reqVO) {
        PageResult<ContentAdDO> page = contentAdService.getAdPage(reqVO);
        return success(new PageResult<>(BeanUtils.toBean(page.getList(), ContentAdRespVO.class), page.getTotal()));
    }

    @GetMapping("/get")
    @Operation(summary = "Get ad detail")
    @Parameter(name = "id", description = "ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('content:ad:query')")
    public CommonResult<ContentAdDetailRespVO> getAd(@RequestParam("id") Long id) {
        ContentAdDO ad = contentAdService.getAd(id);
        if (ad == null || !Objects.equals(ad.getDeleted(), 0)) {
            throw exception(ErrorCodeConstants.AD_NOT_EXISTS);
        }
        return success(BeanUtils.toBean(ad, ContentAdDetailRespVO.class));
    }

    @PostMapping("/create")
    @Operation(summary = "Create ad")
    @PreAuthorize("@ss.hasPermission('content:ad:create')")
    public CommonResult<Long> createAd(@Valid @RequestBody ContentAdSaveReqVO reqVO) {
        ContentAdDO ad = BeanUtils.toBean(reqVO, ContentAdDO.class);
        Long id = contentAdService.createAd(ad);
        return success(id);
    }

    @PutMapping("/update")
    @Operation(summary = "Update ad")
    @PreAuthorize("@ss.hasPermission('content:ad:update')")
    public CommonResult<Boolean> updateAd(@Valid @RequestBody ContentAdSaveReqVO reqVO) {
        ContentAdDO existing = contentAdService.getAd(reqVO.getId());
        if (existing == null || !Objects.equals(existing.getDeleted(), 0)) {
            throw exception(ErrorCodeConstants.AD_NOT_EXISTS);
        }
        applyUpdate(existing, reqVO);
        contentAdService.updateAd(existing);
        return success(true);
    }

    @PutMapping("/update-status")
    @Operation(summary = "Update ad status")
    @PreAuthorize("@ss.hasPermission('content:ad:update')")
    public CommonResult<Boolean> updateAdStatus(@Valid @RequestBody ContentAdUpdateStatusReqVO reqVO) {
        contentAdService.updateAdStatus(reqVO.getId(), reqVO.getStatus());
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "Delete ad")
    @Parameter(name = "id", description = "ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('content:ad:delete')")
    public CommonResult<Boolean> deleteAd(@RequestParam("id") Long id) {
        contentAdService.deleteAd(id);
        return success(true);
    }

    private void applyUpdate(ContentAdDO ad, ContentAdSaveReqVO reqVO) {
        ad.setTitle(reqVO.getTitle());
        ad.setSubTitle(reqVO.getSubTitle());
        ad.setCardType(reqVO.getCardType());
        ad.setMediaType(reqVO.getMediaType());
        ad.setCoverImage(reqVO.getCoverImage());
        ad.setVideoUrl(reqVO.getVideoUrl());
        ad.setJumpUrl(reqVO.getJumpUrl());
        ad.setCallToAction(reqVO.getCallToAction());
        ad.setAdvertiserName(reqVO.getAdvertiserName());
        ad.setStatus(reqVO.getStatus());
        ad.setPriority(reqVO.getPriority());
        ad.setDisplayScene(reqVO.getDisplayScene());
        ad.setFrequencyCap(reqVO.getFrequencyCap());
        ad.setStartTime(reqVO.getStartTime());
        ad.setEndTime(reqVO.getEndTime());
        ad.setStyleMeta(reqVO.getStyleMeta());
    }
}
