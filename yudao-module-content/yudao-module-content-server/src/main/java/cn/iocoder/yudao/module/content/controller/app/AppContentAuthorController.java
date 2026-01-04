package cn.iocoder.yudao.module.content.controller.app;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.content.controller.app.vo.ContentAuthorProfileRespVO;
import cn.iocoder.yudao.module.content.controller.app.vo.ContentListRespVO;
import cn.iocoder.yudao.module.content.controller.app.vo.ContentPageReqVO;
import cn.iocoder.yudao.module.content.service.ContentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 APP - 作者主页")
@Validated
@RestController
@RequestMapping("/content/author")
public class AppContentAuthorController {

    @Resource
    private ContentService contentService;

    @GetMapping("/profile")
    @PermitAll
    @Operation(summary = "获取作者主页信息")
    public CommonResult<ContentAuthorProfileRespVO> getAuthorProfile(@RequestParam("userId") @NotNull Long userId) {
        Long currentUserId = SecurityFrameworkUtils.getLoginUserId();
        return success(contentService.getAuthorProfile(userId, currentUserId));
    }

    @GetMapping("/posts")
    @PermitAll
    @Operation(summary = "获取作者的作品列表")
    public CommonResult<PageResult<ContentListRespVO>> getAuthorPosts(@RequestParam("userId") @NotNull Long userId,
                                                                      @Valid ContentPageReqVO pageReqVO) {
        Long currentUserId = SecurityFrameworkUtils.getLoginUserId();
        return success(contentService.getAuthorContentPage(userId, pageReqVO, currentUserId));
    }
}
