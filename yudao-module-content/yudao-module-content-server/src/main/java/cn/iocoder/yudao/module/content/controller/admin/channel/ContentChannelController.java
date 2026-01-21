package cn.iocoder.yudao.module.content.controller.admin.channel;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.content.controller.admin.channel.vo.ContentChannelPageReqVO;
import cn.iocoder.yudao.module.content.controller.admin.channel.vo.ContentChannelRespVO;
import cn.iocoder.yudao.module.content.controller.admin.channel.vo.ContentChannelSaveReqVO;
import cn.iocoder.yudao.module.content.service.ContentChannelAdminService;
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

@Tag(name = "Admin - content channels")
@RestController
@RequestMapping("/content/channel")
@Validated
public class ContentChannelController {

    @Resource
    private ContentChannelAdminService contentChannelAdminService;

    @GetMapping("/page")
    @Operation(summary = "Get channel page")
    @PreAuthorize("@ss.hasPermission('content:channel:query')")
    public CommonResult<PageResult<ContentChannelRespVO>> getChannelPage(@Valid ContentChannelPageReqVO reqVO) {
        return success(contentChannelAdminService.getChannelPage(reqVO));
    }

    @GetMapping("/get")
    @Operation(summary = "Get channel detail")
    @Parameter(name = "id", description = "ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('content:channel:query')")
    public CommonResult<ContentChannelRespVO> getChannel(@RequestParam("id") Long id) {
        return success(contentChannelAdminService.getChannel(id));
    }

    @PostMapping("/create")
    @Operation(summary = "Create channel")
    @PreAuthorize("@ss.hasPermission('content:channel:create')")
    public CommonResult<Long> createChannel(@Valid @RequestBody ContentChannelSaveReqVO reqVO) {
        return success(contentChannelAdminService.createChannel(reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "Update channel")
    @PreAuthorize("@ss.hasPermission('content:channel:update')")
    public CommonResult<Boolean> updateChannel(@Valid @RequestBody ContentChannelSaveReqVO reqVO) {
        contentChannelAdminService.updateChannel(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "Delete channel")
    @Parameter(name = "id", description = "ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('content:channel:delete')")
    public CommonResult<Boolean> deleteChannel(@RequestParam("id") Long id) {
        contentChannelAdminService.deleteChannel(id);
        return success(true);
    }
}
