package cn.iocoder.yudao.module.content.controller.app;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.content.controller.app.vo.ContentListRespVO;
import cn.iocoder.yudao.module.content.controller.app.vo.SearchAllRespVO;
import cn.iocoder.yudao.module.content.service.search.SearchService;
import cn.iocoder.yudao.module.content.service.search.SearchService.SearchUserRespVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 APP - 内容搜索")
@Validated
@RestController
@RequestMapping("/content/search")
public class AppSearchController {

    @Resource
    private SearchService searchService;

    @GetMapping("/content")
    @Operation(summary = "Search content")
    public CommonResult<PageResult<ContentListRespVO>> searchContent(
            @RequestParam("keyword") @Parameter(description = "Search keyword") String keyword,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        PageResult<ContentListRespVO> result =
                searchService.searchContents(keyword, page, size, userId);
        return success(result);
    }

    @GetMapping("/users")
    @Operation(summary = "Search users (placeholder)")
    public CommonResult<List<SearchUserRespVO>> searchUsers(
            @RequestParam("keyword") String keyword) {
        List<SearchUserRespVO> users = searchService.searchUsers(keyword);
        return success(users);
    }

    @GetMapping("/hot-keywords")
    @Operation(summary = "Get hot keywords (placeholder)")
    public CommonResult<List<String>> getHotKeywords(
            @RequestParam(value = "top", defaultValue = "10") Integer top) {
        return success(searchService.getHotKeywords(top));
    }

    @GetMapping("/history")
    @Operation(summary = "Get search history (placeholder)")
    public CommonResult<List<String>> getHistory(
            @RequestParam(value = "userId", required = false) Long userId) {
        Long loginUser = userId != null ? userId : SecurityFrameworkUtils.getLoginUserId();
        return success(searchService.getHistory(loginUser, 50));
    }

    @GetMapping("/clear-history")
    @Operation(summary = "Clear search history (placeholder)")
    public CommonResult<Boolean> clearHistory(
            @RequestParam(value = "userId", required = false) Long userId) {
        Long loginUser = userId != null ? userId : SecurityFrameworkUtils.getLoginUserId();
        return success(searchService.clearHistory(loginUser));
    }

    @GetMapping("/all")
    @Operation(summary = "Search all (content + users placeholder)")
    public CommonResult<SearchAllRespVO> searchAll(
            @RequestParam("keyword") String keyword,
            @RequestParam(value = "type", defaultValue = "all") String type,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        SearchAllRespVO resp = searchService.searchAll(keyword, type, page, size, userId);
        return success(resp);
    }
}
