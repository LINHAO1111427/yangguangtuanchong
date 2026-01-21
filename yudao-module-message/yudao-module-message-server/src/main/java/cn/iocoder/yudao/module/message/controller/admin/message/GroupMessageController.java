package cn.iocoder.yudao.module.message.controller.admin.message;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.message.controller.admin.message.vo.GroupMessagePageReqVO;
import cn.iocoder.yudao.module.message.controller.admin.message.vo.GroupMessageRespVO;
import cn.iocoder.yudao.module.message.service.GroupMessageAdminService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "Admin - message group")
@RestController
@RequestMapping("/message/group-message")
@Validated
public class GroupMessageController {

    @Resource
    private GroupMessageAdminService groupMessageAdminService;

    @GetMapping("/page")
    @Operation(summary = "Get group message page")
    @PreAuthorize("@ss.hasPermission('message:group-message:query')")
    public CommonResult<PageResult<GroupMessageRespVO>> getMessagePage(@Valid GroupMessagePageReqVO reqVO) {
        return success(groupMessageAdminService.getMessagePage(reqVO));
    }

    @PutMapping("/recall")
    @Operation(summary = "Recall group message")
    @Parameter(name = "id", description = "Message ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('message:group-message:update')")
    public CommonResult<Boolean> recallMessage(@RequestParam("id") Long id) {
        groupMessageAdminService.recallMessage(id);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "Delete group message")
    @Parameter(name = "id", description = "Message ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('message:group-message:delete')")
    public CommonResult<Boolean> deleteMessage(@RequestParam("id") Long id) {
        groupMessageAdminService.deleteMessage(id);
        return success(true);
    }
}
