package cn.iocoder.yudao.module.content.controller.app;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.content.controller.app.vo.CommentCreateReqVO;
import cn.iocoder.yudao.module.content.controller.app.vo.CommentPageReqVO;
import cn.iocoder.yudao.module.content.controller.app.vo.CommentRespVO;
import cn.iocoder.yudao.module.content.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.content.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.UNAUTHORIZED;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 APP - 评论")
@Validated
@RestController
@RequestMapping("/content/comment")
public class AppCommentController {

    @Resource
    private CommentService commentService;

    @GetMapping("/list")
    @PermitAll
    @Operation(summary = "List comments of a content or replies of a comment")
    public CommonResult<PageResult<CommentRespVO>> getCommentList(@Valid CommentPageReqVO pageReqVO) {
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        if (Boolean.TRUE.equals(pageReqVO.getMine()) && loginUserId == null) {
            throw exception(UNAUTHORIZED);
        }
        return success(commentService.getCommentPage(pageReqVO, loginUserId));
    }

    @GetMapping("/page")
    @PermitAll
    @Operation(summary = "Legacy comment pagination API")
    public CommonResult<PageResult<CommentRespVO>> getCommentPage(@Valid CommentPageReqVO pageReqVO) {
        return getCommentList(pageReqVO);
    }

    @PostMapping("/publish")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Publish a top-level comment")
    public CommonResult<CommentRespVO> publishComment(@Valid @RequestBody CommentCreateReqVO reqVO,
                                                      HttpServletRequest request) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        CommentRespVO respVO = commentService.createComment(reqVO, userId,
                ServletUtils.getClientIP(request), request.getHeader("User-Agent"));
        return success(respVO);
    }

    @PostMapping("/reply")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Reply to a comment")
    public CommonResult<CommentRespVO> replyComment(@Valid @RequestBody CommentCreateReqVO reqVO,
                                                    HttpServletRequest request) {
        if (reqVO.getParentId() == null) {
            throw exception(ErrorCodeConstants.COMMENT_NOT_EXISTS);
        }
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        CommentRespVO respVO = commentService.createComment(reqVO, userId,
                ServletUtils.getClientIP(request), request.getHeader("User-Agent"));
        return success(respVO);
    }

    @PostMapping("/create")
    @Deprecated
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Legacy create comment API")
    public CommonResult<Long> createComment(@Valid @RequestBody CommentCreateReqVO reqVO,
                                            HttpServletRequest request) {
        CommentRespVO respVO = commentService.createComment(reqVO,
                SecurityFrameworkUtils.getLoginUserId(),
                ServletUtils.getClientIP(request),
                request.getHeader("User-Agent"));
        return success(respVO.getId());
    }

    @DeleteMapping("/delete")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete comment")
    public CommonResult<Boolean> deleteComment(@RequestParam("id") @NotNull Long id) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        commentService.deleteComment(id, userId);
        return success(Boolean.TRUE);
    }
}
