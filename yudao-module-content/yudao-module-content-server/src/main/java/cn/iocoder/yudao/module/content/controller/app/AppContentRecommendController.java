package cn.iocoder.yudao.module.content.controller.app;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.content.controller.app.vo.ContentListRespVO;
import cn.iocoder.yudao.module.content.service.ContentService;
import cn.iocoder.yudao.module.content.service.recommend.ContentRecommendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * Recommendation related APIs exposed to the app client.
 */
@Tag(name = "APP - Content Recommendation")
@Validated
@RestController
@RequestMapping("/content/recommend")
public class AppContentRecommendController {

    private static final Logger log = LoggerFactory.getLogger(AppContentRecommendController.class);

    @Resource
    private ContentService contentService;

    @Resource
    private ContentRecommendService contentRecommendService;

    @GetMapping("/feed")
    @Operation(summary = "Get personalised recommend feed")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<PageResult<ContentListRespVO>> getRecommendFeed(
            @RequestParam(value = "page", defaultValue = "1") @Min(1) Integer page,
            @RequestParam(value = "page_size", defaultValue = "20") @Min(1) @Max(100) Integer pageSize) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        ContentRecommendService.RecommendResult recommend =
                contentRecommendService.getRecommendFeed(userId, page, pageSize);
        PageResult<ContentListRespVO> result = buildPageResult(recommend, userId, page, pageSize);
        log.info("Recommend feed strategy={} userId={} page={} size={}",
                recommend.getRecommendStrategy(), userId, page, pageSize);
        return success(result);
    }

    @GetMapping("/hot")
    @Operation(summary = "Get hot content feed")
    public CommonResult<PageResult<ContentListRespVO>> getHotFeed(
            @RequestParam(value = "page", defaultValue = "1") @Min(1) Integer page,
            @RequestParam(value = "page_size", defaultValue = "20") @Min(1) @Max(100) Integer pageSize) {
        ContentRecommendService.RecommendResult recommend =
                contentRecommendService.getHotContent(page, pageSize);
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        PageResult<ContentListRespVO> result = buildPageResult(recommend, userId, page, pageSize);
        return success(result);
    }

    @PostMapping("/behavior")
    @Operation(summary = "Record user behaviour for recommendation")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<Boolean> recordUserBehavior(@Valid @RequestBody UserBehaviorRequest request) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        contentRecommendService.recordUserBehavior(userId, request.getContentId(), request.getActionType());
        return success(Boolean.TRUE);
    }

    @PostMapping("/hot-score")
    @Operation(summary = "Update hot score for single content")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<Boolean> updateContentHotScore(@Valid @RequestBody ContentHotScoreRequest request) {
        contentRecommendService.updateContentHotScore(
                request.getContentId(),
                request.getViewCount(),
                request.getLikeCount(),
                request.getCommentCount(),
                request.getShareCount()
        );
        return success(Boolean.TRUE);
    }

    @PostMapping("/hot-score/batch")
    @Operation(summary = "Batch update hot score")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<Integer> batchUpdateContentHotScore(
            @Valid @RequestBody BatchContentHotScoreRequest request) {
        if (request.getItems().isEmpty()) {
            return success(0);
        }
        int successCounter = 0;
        for (ContentHotScoreRequest item : request.getItems()) {
            try {
                contentRecommendService.updateContentHotScore(
                        item.getContentId(),
                        item.getViewCount(),
                        item.getLikeCount(),
                        item.getCommentCount(),
                        item.getShareCount()
                );
                successCounter++;
            } catch (Exception ex) {
                log.warn("Batch update hot score failed for contentId={}", item.getContentId(), ex);
            }
        }
        return success(successCounter);
    }

    @PostMapping("/refresh-cache")
    @Operation(summary = "Clear and refresh personalised recommend cache")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<Boolean> refreshRecommendCache() {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        contentRecommendService.recordUserBehavior(userId, null, "refresh-cache");
        log.info("Cleared recommend cache for user {}", userId);
        return success(Boolean.TRUE);
    }

    private PageResult<ContentListRespVO> buildPageResult(ContentRecommendService.RecommendResult recommend,
                                                          Long userId,
                                                          int page,
                                                          int pageSize) {
        List<Long> contentIds = recommend.getContentIds();
        if (contentIds == null || contentIds.isEmpty()) {
            return PageResult.empty(recommend.getTotalCount() == null ? 0 : recommend.getTotalCount());
        }
        List<ContentListRespVO> list =
                contentService.getContentListByIds(contentIds, userId).stream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
        // ensure ordering is identical to recommendation order
        List<ContentListRespVO> ordered = contentIds.stream()
                .map(id -> list.stream().filter(item -> id.equals(item.getId())).findFirst().orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        long total = recommend.getTotalCount() != null ? recommend.getTotalCount() : ordered.size();
        return new PageResult<>(ordered, total);
    }

    // ========== Request DTOs ==========

    public static class UserBehaviorRequest {
        @NotNull(message = "Content id is required")
        @Parameter(description = "Content id", required = true)
        private Long contentId;

        @NotBlank(message = "Action type is required")
        @Parameter(description = "Action type (view/like/favorite/share)", required = true)
        private String actionType;

        public Long getContentId() {
            return contentId;
        }

        public void setContentId(Long contentId) {
            this.contentId = contentId;
        }

        public String getActionType() {
            return actionType;
        }

        public void setActionType(String actionType) {
            this.actionType = actionType;
        }
    }

    public static class ContentHotScoreRequest {
        @NotNull(message = "Content id is required")
        private Long contentId;

        @NotNull(message = "View count is required")
        private Long viewCount;

        @NotNull(message = "Like count is required")
        private Long likeCount;

        @NotNull(message = "Comment count is required")
        private Long commentCount;

        @NotNull(message = "Share count is required")
        private Long shareCount;

        public Long getContentId() {
            return contentId;
        }

        public void setContentId(Long contentId) {
            this.contentId = contentId;
        }

        public Long getViewCount() {
            return viewCount;
        }

        public void setViewCount(Long viewCount) {
            this.viewCount = viewCount;
        }

        public Long getLikeCount() {
            return likeCount;
        }

        public void setLikeCount(Long likeCount) {
            this.likeCount = likeCount;
        }

        public Long getCommentCount() {
            return commentCount;
        }

        public void setCommentCount(Long commentCount) {
            this.commentCount = commentCount;
        }

        public Long getShareCount() {
            return shareCount;
        }

        public void setShareCount(Long shareCount) {
            this.shareCount = shareCount;
        }
    }

    public static class BatchContentHotScoreRequest {
        @NotNull(message = "Item list is required")
        private List<@Valid ContentHotScoreRequest> items = Collections.emptyList();

        public List<ContentHotScoreRequest> getItems() {
            return items;
        }

        public void setItems(List<ContentHotScoreRequest> items) {
            this.items = items == null ? Collections.emptyList() : items;
        }
    }
}
