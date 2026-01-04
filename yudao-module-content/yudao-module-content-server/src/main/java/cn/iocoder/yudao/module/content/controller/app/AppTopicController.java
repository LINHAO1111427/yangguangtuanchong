package cn.iocoder.yudao.module.content.controller.app;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.content.controller.app.vo.TopicListRespVO;
import cn.iocoder.yudao.module.content.controller.app.vo.TopicOptionRespVO;
import cn.iocoder.yudao.module.content.controller.app.vo.TopicPageReqVO;
import cn.iocoder.yudao.module.content.controller.app.vo.TopicRespVO;
import cn.iocoder.yudao.module.content.controller.app.vo.TopicSimpleRespVO;
import cn.iocoder.yudao.module.content.dal.dataobject.TopicDO;
import cn.iocoder.yudao.module.content.service.TopicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 APP - 话题")
@Validated
@RestController
@RequestMapping("/content/topic")
public class AppTopicController {

    @Resource
    private TopicService topicService;

    @GetMapping("/page")
    @Operation(summary = "Page query topics")
    public CommonResult<PageResult<TopicDO>> getTopicPage(TopicPageReqVO pageReqVO) {
        return success(topicService.getTopicPage(pageReqVO));
    }

    @GetMapping("/recommend")
    @Operation(summary = "Recommended topics")
    public CommonResult<List<TopicListRespVO>> getRecommendTopics(@RequestParam(value = "limit", defaultValue = "10") Integer limit) {
        return success(topicService.getRecommendTopics(limit));
    }

    @GetMapping("/hot")
    @Operation(summary = "Hot topics")
    public CommonResult<List<TopicListRespVO>> getHotTopics(@RequestParam(value = "limit", defaultValue = "10") Integer limit) {
        return success(topicService.getHotTopics(limit));
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Topics by type")
    public CommonResult<List<TopicListRespVO>> getTopicsByType(@PathVariable("type") Integer type) {
        return success(topicService.getTopicsByType(type));
    }

    @GetMapping("/search")
    @Operation(summary = "Search topics")
    public CommonResult<List<TopicListRespVO>> searchTopics(@RequestParam("keyword") String keyword,
                                                            @RequestParam(value = "limit", defaultValue = "10") Integer limit) {
        return success(topicService.searchTopics(keyword, limit));
    }

    @GetMapping("/options")
    @Operation(summary = "Topic options")
    public CommonResult<List<TopicOptionRespVO>> getTopicOptions() {
        return success(topicService.getAllTopicOptions());
    }

    @GetMapping("/enabled")
    @Operation(summary = "All enabled topics")
    public CommonResult<List<TopicSimpleRespVO>> getAllEnabledTopics() {
        return success(topicService.getAllEnabledTopics());
    }

    @GetMapping("/detail/{id}")
    @Operation(summary = "Topic detail")
    public CommonResult<TopicRespVO> getTopicDetail(@PathVariable("id") Long id) {
        return success(topicService.getTopicDetail(id));
    }
}
