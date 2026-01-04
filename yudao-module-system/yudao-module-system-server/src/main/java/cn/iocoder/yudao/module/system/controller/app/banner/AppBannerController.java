package cn.iocoder.yudao.module.system.controller.app.banner;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.system.controller.app.banner.vo.AppBannerRespVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 APP - Banner")
@RestController
@RequestMapping("/api/v1.0.1/banner")
@Validated
public class AppBannerController {

    @GetMapping("/list")
    @PermitAll
    @Operation(summary = "获取 Banner 列表", description = "当前返回内置数据占位，待接入后台配置后可无缝替换")
    public CommonResult<List<AppBannerRespVO>> list(
            @RequestParam(value = "position", required = false) String position,
            @RequestParam(value = "limit", defaultValue = "5") @Min(1) Integer limit) {
        List<AppBannerRespVO> banners = buildDefaults();
        // 暂无按位置过滤的需求，预留参数便于后续扩展
        banners.sort(Comparator.comparing(AppBannerRespVO::getSort));
        if (banners.size() > limit) {
            banners = banners.subList(0, limit);
        }
        return success(banners);
    }

    private List<AppBannerRespVO> buildDefaults() {
        List<AppBannerRespVO> list = new ArrayList<>();
        AppBannerRespVO first = new AppBannerRespVO();
        first.setId(1L);
        first.setTitle("精选推荐");
        first.setSubTitle("编辑精选内容");
        first.setImage("https://picsum.photos/600/300?random=11");
        first.setLink("");
        first.setSort(1);
        list.add(first);

        AppBannerRespVO second = new AppBannerRespVO();
        second.setId(2L);
        second.setTitle("社区活动");
        second.setSubTitle("立即参与赢取奖励");
        second.setImage("https://picsum.photos/600/300?random=12");
        second.setLink("");
        second.setSort(2);
        list.add(second);

        AppBannerRespVO third = new AppBannerRespVO();
        third.setId(3L);
        third.setTitle("创作指南");
        third.setSubTitle("提升你的创作效率");
        third.setImage("https://picsum.photos/600/300?random=13");
        third.setLink("");
        third.setSort(3);
        list.add(third);
        return list;
    }
}
