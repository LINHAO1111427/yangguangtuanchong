package cn.iocoder.yudao.module.content.controller.admin.content;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.content.controller.admin.content.vo.ContentPostAuditReqVO;
import cn.iocoder.yudao.module.content.controller.admin.content.vo.ContentPostDetailRespVO;
import cn.iocoder.yudao.module.content.controller.admin.content.vo.ContentPostPageReqVO;
import cn.iocoder.yudao.module.content.controller.admin.content.vo.ContentPostRespVO;
import cn.iocoder.yudao.module.content.controller.admin.content.vo.ContentPostUpdateReqVO;
import cn.iocoder.yudao.module.content.service.ContentPostAdminService;
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

@Tag(name = "Admin - content posts")
@RestController
@RequestMapping("/content/post")
@Validated
public class ContentPostController {

    @Resource
    private ContentPostAdminService contentPostAdminService;

    @GetMapping("/page")
    @Operation(summary = "Get content post page")
    @PreAuthorize("@ss.hasPermission('content:post:query')")
    public CommonResult<PageResult<ContentPostRespVO>> getPostPage(@Valid ContentPostPageReqVO reqVO) {
        return success(contentPostAdminService.getPostPage(reqVO));
    }

    @GetMapping("/get")
    @Operation(summary = "Get content post detail")
    @Parameter(name = "id", description = "ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('content:post:query')")
    public CommonResult<ContentPostDetailRespVO> getPostDetail(@RequestParam("id") Long id) {
        return success(contentPostAdminService.getPostDetail(id));
    }

    @PutMapping("/audit")
    @Operation(summary = "Audit content post")
    @PreAuthorize("@ss.hasPermission('content:post:update')")
    public CommonResult<Boolean> auditPost(@Valid @RequestBody ContentPostAuditReqVO reqVO) {
        contentPostAdminService.auditPost(reqVO);
        return success(true);
    }

    @PutMapping("/update")
    @Operation(summary = "Update content post flags")
    @PreAuthorize("@ss.hasPermission('content:post:update')")
    public CommonResult<Boolean> updatePost(@Valid @RequestBody ContentPostUpdateReqVO reqVO) {
        contentPostAdminService.updatePost(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "Delete content post")
    @Parameter(name = "id", description = "ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('content:post:delete')")
    public CommonResult<Boolean> deletePost(@RequestParam("id") Long id) {
        contentPostAdminService.deletePost(id);
        return success(true);
    }
}
