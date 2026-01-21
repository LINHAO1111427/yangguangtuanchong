package cn.iocoder.yudao.module.message.controller.admin.message;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.message.controller.admin.message.vo.NotificationPageReqVO;
import cn.iocoder.yudao.module.message.controller.admin.message.vo.NotificationRespVO;
import cn.iocoder.yudao.module.message.service.NotificationAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "Admin - message notification")
@RestController
@RequestMapping("/message/notification")
@Validated
public class NotificationController {

    @Resource
    private NotificationAdminService notificationAdminService;

    @GetMapping("/page")
    @Operation(summary = "Get notification page")
    @PreAuthorize("@ss.hasPermission('message:notification:query')")
    public CommonResult<PageResult<NotificationRespVO>> getNotificationPage(@Valid NotificationPageReqVO reqVO) {
        return success(notificationAdminService.getNotificationPage(reqVO));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "Delete notification")
    @Parameter(name = "id", description = "Notification ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('message:notification:delete')")
    public CommonResult<Boolean> deleteNotification(@RequestParam("id") Long id) {
        notificationAdminService.deleteNotification(id);
        return success(true);
    }
}
