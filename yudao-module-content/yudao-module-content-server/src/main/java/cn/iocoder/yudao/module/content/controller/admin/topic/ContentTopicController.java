package cn.iocoder.yudao.module.content.controller.admin.topic;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.content.controller.admin.topic.vo.ContentTopicRespVO;
import cn.iocoder.yudao.module.content.controller.admin.topic.vo.ContentTopicSaveReqVO;
import cn.iocoder.yudao.module.content.controller.app.vo.TopicPageReqVO;
import cn.iocoder.yudao.module.content.service.ContentTopicAdminService;
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

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "Admin - content topics")
@RestController
@RequestMapping("/content/topic")
@Validated
public class ContentTopicController {

    @Resource
    private ContentTopicAdminService contentTopicAdminService;

    @GetMapping("/page")
    @Operation(summary = "Get topic page")
    @PreAuthorize("@ss.hasPermission('content:topic:query')")
    public CommonResult<PageResult<ContentTopicRespVO>> getTopicPage(@Valid TopicPageReqVO reqVO) {
        return success(contentTopicAdminService.getTopicPage(reqVO));
    }

    @GetMapping("/get")
    @Operation(summary = "Get topic detail")
    @Parameter(name = "id", description = "ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('content:topic:query')")
    public CommonResult<ContentTopicRespVO> getTopic(@RequestParam("id") Long id) {
        return success(contentTopicAdminService.getTopic(id));
    }

    @PostMapping("/create")
    @Operation(summary = "Create topic")
    @PreAuthorize("@ss.hasPermission('content:topic:create')")
    public CommonResult<Long> createTopic(@Valid @RequestBody ContentTopicSaveReqVO reqVO) {
        return success(contentTopicAdminService.createTopic(reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "Update topic")
    @PreAuthorize("@ss.hasPermission('content:topic:update')")
    public CommonResult<Boolean> updateTopic(@Valid @RequestBody ContentTopicSaveReqVO reqVO) {
        contentTopicAdminService.updateTopic(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "Delete topic")
    @Parameter(name = "id", description = "ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('content:topic:delete')")
    public CommonResult<Boolean> deleteTopic(@RequestParam("id") Long id) {
        contentTopicAdminService.deleteTopic(id);
        return success(true);
    }
}
