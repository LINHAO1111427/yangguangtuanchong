package cn.iocoder.yudao.module.message.controller.admin.message;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.message.controller.admin.message.vo.MessagePrivatePageReqVO;
import cn.iocoder.yudao.module.message.controller.admin.message.vo.MessagePrivateRespVO;
import cn.iocoder.yudao.module.message.service.MessagePrivateAdminService;
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

@Tag(name = "Admin - message private")
@RestController
@RequestMapping("/message/private")
@Validated
public class MessagePrivateController {

    @Resource
    private MessagePrivateAdminService messagePrivateAdminService;

    @GetMapping("/page")
    @Operation(summary = "Get private message page")
    @PreAuthorize("@ss.hasPermission('message:private:query')")
    public CommonResult<PageResult<MessagePrivateRespVO>> getMessagePage(@Valid MessagePrivatePageReqVO reqVO) {
        return success(messagePrivateAdminService.getMessagePage(reqVO));
    }

    @PutMapping("/recall")
    @Operation(summary = "Recall private message")
    @Parameter(name = "id", description = "Message ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('message:private:update')")
    public CommonResult<Boolean> recallMessage(@RequestParam("id") Long id) {
        messagePrivateAdminService.recallMessage(id);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "Delete private message")
    @Parameter(name = "id", description = "Message ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('message:private:delete')")
    public CommonResult<Boolean> deleteMessage(@RequestParam("id") Long id) {
        messagePrivateAdminService.deleteMessage(id);
        return success(true);
    }
}
