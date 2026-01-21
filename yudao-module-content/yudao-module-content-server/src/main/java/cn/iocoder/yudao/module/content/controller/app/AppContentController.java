package cn.iocoder.yudao.module.content.controller.app;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.content.controller.app.vo.ContentCreateReqVO;
import cn.iocoder.yudao.module.content.controller.app.vo.ContentDetailRespVO;
import cn.iocoder.yudao.module.content.controller.app.vo.ContentListRespVO;
import cn.iocoder.yudao.module.content.controller.app.vo.ContentPageReqVO;
import cn.iocoder.yudao.module.content.controller.app.vo.ContentToggleRespVO;
import cn.iocoder.yudao.module.content.controller.app.vo.ContentUpdateReqVO;
import cn.iocoder.yudao.module.content.dal.dataobject.ContentDO;
import cn.iocoder.yudao.module.content.service.ContentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * App-facing controller for content publishing and interaction.
 *
 * <p>The original source file contained unreadable characters, so the class is
 * rewritten in English. Only the business behaviour required by the rest of the
 * project is retained.</p>
 */
@Tag(name = "用户 APP - 内容发布")
@Validated
@RestController
@RequestMapping("/content/publish")
public class AppContentController {

    private static final Logger log = LoggerFactory.getLogger(AppContentController.class);

    @Resource
    private ContentService contentService;

    @PostMapping("/create")
    @Operation(summary = "Create content")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<Map<String, Object>> createContent(@Valid @RequestBody ContentCreateReqVO createReqVO,
                                                           HttpServletRequest request) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        createReqVO.setUserId(userId);
        createReqVO.setIpAddress(ServletUtils.getClientIP(request));
        createReqVO.setUserAgent(request.getHeader("User-Agent"));

        Long contentId = contentService.createContent(createReqVO);
        log.info("User {} created content {}", userId, contentId);

        Map<String, Object> result = new HashMap<>();
        result.put("content_id", contentId);
        result.put("success", Boolean.TRUE);
        boolean draft = createReqVO.getStatus() != null && createReqVO.getStatus() == 0;
        result.put("message", draft ? "Draft saved" : "Publish succeeded");
        return success(result);
    }

    @PutMapping("/update")
    @Operation(summary = "Update content")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<Boolean> updateContent(@Valid @RequestBody ContentUpdateReqVO updateReqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        updateReqVO.setUserId(userId);
        contentService.validateContentOwner(updateReqVO.getId(), userId);
        contentService.updateContent(updateReqVO);
        log.info("User {} updated content {}", userId, updateReqVO.getId());
        return success(Boolean.TRUE);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "Delete content")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<Boolean> deleteContent(@NotNull @RequestParam("id") Long id) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        contentService.validateContentOwner(id, userId);
        contentService.deleteContent(id);
        log.info("User {} deleted content {}", userId, id);
        return success(Boolean.TRUE);
    }

    @GetMapping("/detail")
    @Operation(summary = "Get content detail")
    public CommonResult<ContentDetailRespVO> getContentDetail(@NotNull @RequestParam("id") Long id,
                                                              HttpServletRequest request) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        ContentDetailRespVO detail = contentService.getContentDetail(id, userId);
        if (userId != null) {
            contentService.recordContentView(id, userId,
                    ServletUtils.getClientIP(request),
                    request.getHeader("User-Agent"));
        }
        return success(detail);
    }

    @GetMapping("/index")
    @Operation(summary = "Page query content")
    public CommonResult<PageResult<ContentListRespVO>> getContentPage(@Valid ContentPageReqVO pageReqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        PageResult<ContentListRespVO> page = contentService.getContentPage(pageReqVO, userId);
        return success(page);
    }

    @GetMapping("/my")
    @Operation(summary = "Page query my content")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<PageResult<ContentListRespVO>> getMyContentPage(@Valid ContentPageReqVO pageReqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        pageReqVO.setUserId(userId);
        PageResult<ContentListRespVO> page = contentService.getMyContentPage(pageReqVO, userId);
        return success(page);
    }

    @GetMapping("/drafts")
    @Operation(summary = "List my drafts")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<List<ContentListRespVO>> getMyDrafts() {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        return success(contentService.getUserDrafts(userId));
    }

    @PostMapping("/like")
    @Operation(summary = "Toggle like")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<ContentToggleRespVO> toggleLike(@NotNull @RequestParam("content_id") Long contentId,
                                            HttpServletRequest request) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        boolean liked = contentService.toggleLike(contentId, userId,
                ServletUtils.getClientIP(request),
                request.getHeader("User-Agent"));
        ContentDO content = contentService.getContent(contentId);
        ContentToggleRespVO resp = new ContentToggleRespVO();
        resp.setLiked(liked);
        resp.setLikeCount(content != null ? content.getLikeCount() : null);
        return success(resp);
    }

    @PostMapping("/collect")
    @Operation(summary = "Toggle favourite")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<ContentToggleRespVO> toggleCollect(@NotNull @RequestParam("content_id") Long contentId,
                                               HttpServletRequest request) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        boolean collected = contentService.toggleCollect(contentId, userId,
                ServletUtils.getClientIP(request),
                request.getHeader("User-Agent"));
        ContentDO content = contentService.getContent(contentId);
        ContentToggleRespVO resp = new ContentToggleRespVO();
        resp.setCollected(collected);
        resp.setCollectCount(content != null ? content.getCollectCount() : null);
        return success(resp);
    }

    @PostMapping("/share")
    @Operation(summary = "Record share action")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<Map<String, Object>> shareContent(
            @NotNull @RequestParam("content_id") Long contentId,
            @NotBlank @RequestParam(value = "platform", defaultValue = "link") String platform,
            HttpServletRequest request) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        String ip = ServletUtils.getClientIP(request);
        String ua = request.getHeader("User-Agent");
        contentService.recordContentShare(contentId, userId, platform, ip, ua);
        Map<String, Object> result = new HashMap<>();
        result.put("success", Boolean.TRUE);
        result.put("share_url", contentService.generateShareUrl(contentId));
        return success(result);
    }

    @PostMapping("/report")
    @Operation(summary = "Report content")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<Boolean> reportContent(
            @NotNull @RequestParam("content_id") Long contentId,
            @NotBlank @RequestParam("reason") String reason,
            @RequestParam(value = "description", required = false) String description,
            HttpServletRequest request) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        contentService.reportContent(contentId, userId, reason, description,
                ServletUtils.getClientIP(request),
                request.getHeader("User-Agent"));
        return success(Boolean.TRUE);
    }

    @GetMapping("/hot")
    @Operation(summary = "Get hot contents")
    public CommonResult<PageResult<ContentListRespVO>> getHotContents(
            @RequestParam(value = "page", defaultValue = "1") @Min(1) Integer page,
            @RequestParam(value = "size", defaultValue = "20") @Min(1) @Max(100) Integer size) {
        ContentPageReqVO pageReqVO = new ContentPageReqVO();
        pageReqVO.setPageNo(page);
        pageReqVO.setPageSize(size);
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        return success(contentService.getHotContents(pageReqVO, userId));
    }

    @GetMapping("/latest")
    @Operation(summary = "Get latest contents")
    public CommonResult<PageResult<ContentListRespVO>> getLatestContents(
            @RequestParam(value = "page", defaultValue = "1") @Min(1) Integer page,
            @RequestParam(value = "size", defaultValue = "20") @Min(1) @Max(100) Integer size) {
        ContentPageReqVO pageReqVO = new ContentPageReqVO();
        pageReqVO.setPageNo(page);
        pageReqVO.setPageSize(size);
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        return success(contentService.getLatestContents(pageReqVO, userId));
    }

    @GetMapping("/following")
    @Operation(summary = "Get contents from followed users")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<PageResult<ContentListRespVO>> getFollowingContents(
            @RequestParam(value = "page", defaultValue = "1") @Min(1) Integer page,
            @RequestParam(value = "size", defaultValue = "20") @Min(1) @Max(100) Integer size) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        ContentPageReqVO pageReqVO = new ContentPageReqVO();
        pageReqVO.setPageNo(page);
        pageReqVO.setPageSize(size);
        return success(contentService.getFollowingContents(userId, pageReqVO));
    }

    @GetMapping("/search")
    @Operation(summary = "Search contents by keyword")
    public CommonResult<PageResult<ContentListRespVO>> searchContents(
            @RequestParam("keyword") @NotBlank String keyword,
            @RequestParam(value = "page", defaultValue = "1") @Min(1) Integer page,
            @RequestParam(value = "size", defaultValue = "20") @Min(1) @Max(100) Integer size) {
        ContentPageReqVO pageReqVO = new ContentPageReqVO();
        pageReqVO.setKeyword(keyword);
        pageReqVO.setPageNo(page);
        pageReqVO.setPageSize(size);
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        return success(contentService.searchContents(pageReqVO, userId));
    }

    @GetMapping("/topic/{topicId}")
    @Operation(summary = "Get contents of the specified topic")
    public CommonResult<PageResult<ContentListRespVO>> getTopicContents(
            @PathVariable("topicId") Long topicId,
            @RequestParam(value = "page", defaultValue = "1") @Min(1) Integer page,
            @RequestParam(value = "size", defaultValue = "20") @Min(1) @Max(100) Integer size) {
        ContentPageReqVO pageReqVO = new ContentPageReqVO();
        pageReqVO.setPublishTopicId(topicId);
        pageReqVO.setPageNo(page);
        pageReqVO.setPageSize(size);
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        return success(contentService.getTopicContents(pageReqVO, userId));
    }

    @GetMapping("/liked")
    @Operation(summary = "Get my liked content list")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<PageResult<ContentListRespVO>> getMyLikedContents(@Valid ContentPageReqVO pageReqVO,
            @RequestParam(value = "page", required = false) @Min(1) Integer page,
            @RequestParam(value = "size", required = false) @Min(1) @Max(100) Integer size) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        if (pageReqVO.getPageNo() == null && page != null) {
            pageReqVO.setPageNo(page);
        }
        if (pageReqVO.getPageSize() == null && size != null) {
            pageReqVO.setPageSize(size);
        }
        return success(contentService.getMyLikedContents(userId, pageReqVO));
    }

    @GetMapping("/collected")
    @Operation(summary = "Get my collected content list")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<PageResult<ContentListRespVO>> getMyCollectedContents(@Valid ContentPageReqVO pageReqVO,
            @RequestParam(value = "page", required = false) @Min(1) Integer page,
            @RequestParam(value = "size", required = false) @Min(1) @Max(100) Integer size) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        if (pageReqVO.getPageNo() == null && page != null) {
            pageReqVO.setPageNo(page);
        }
        if (pageReqVO.getPageSize() == null && size != null) {
            pageReqVO.setPageSize(size);
        }
        return success(contentService.getMyCollectedContents(userId, pageReqVO));
    }

    @GetMapping("/history")
    @Operation(summary = "获取我的浏览历史")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<PageResult<ContentListRespVO>> getMyViewHistory(
            @RequestParam(value = "page", defaultValue = "1") @Min(1) Integer page,
            @RequestParam(value = "size", defaultValue = "20") @Min(1) @Max(100) Integer size) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        ContentPageReqVO pageReqVO = new ContentPageReqVO();
        pageReqVO.setPageNo(page);
        pageReqVO.setPageSize(size);
        return success(contentService.getMyViewHistory(userId, pageReqVO));
    }

    @DeleteMapping("/history/delete")
    @Operation(summary = "删除我的浏览历史（指定内容）")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<Boolean> deleteMyViewHistory(@RequestBody @NotEmpty List<Long> contentIds) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        contentService.deleteMyViewHistory(userId, contentIds);
        return success(Boolean.TRUE);
    }

    @DeleteMapping("/history")
    @Operation(summary = "清空我的浏览历史")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<Boolean> clearMyViewHistory() {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        contentService.clearMyViewHistory(userId);
        return success(Boolean.TRUE);
    }
}
