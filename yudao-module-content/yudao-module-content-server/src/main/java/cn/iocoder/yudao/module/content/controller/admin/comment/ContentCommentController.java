package cn.iocoder.yudao.module.content.controller.admin.comment;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.content.controller.admin.comment.vo.ContentCommentAuditReqVO;
import cn.iocoder.yudao.module.content.controller.admin.comment.vo.ContentCommentPageReqVO;
import cn.iocoder.yudao.module.content.controller.admin.comment.vo.ContentCommentRespVO;
import cn.iocoder.yudao.module.content.service.ContentCommentAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "Admin - content comments")
@RestController
@RequestMapping("/content/comment")
@Validated
public class ContentCommentController {

    @Resource
    private ContentCommentAdminService contentCommentAdminService;

    @GetMapping("/page")
    @Operation(summary = "Get comment page")
    @PreAuthorize("@ss.hasPermission('content:comment:query')")
    public CommonResult<PageResult<ContentCommentRespVO>> getCommentPage(@Valid ContentCommentPageReqVO reqVO) {
        return success(contentCommentAdminService.getCommentPage(reqVO));
    }

    @PutMapping("/audit")
    @Operation(summary = "Audit comment")
    @PreAuthorize("@ss.hasPermission('content:comment:update')")
    public CommonResult<Boolean> auditComment(@Valid @RequestBody ContentCommentAuditReqVO reqVO) {
        contentCommentAdminService.auditComment(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "Delete comment")
    @Parameter(name = "id", description = "ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('content:comment:delete')")
    public CommonResult<Boolean> deleteComment(@RequestParam("id") Long id) {
        contentCommentAdminService.deleteComment(id);
        return success(true);
    }
}
