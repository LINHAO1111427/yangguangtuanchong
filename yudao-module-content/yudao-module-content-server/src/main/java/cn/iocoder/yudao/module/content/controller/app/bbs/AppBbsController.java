package cn.iocoder.yudao.module.content.controller.app.bbs;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.content.controller.app.bbs.vo.AppBbsPostCreateReqVO;
import cn.iocoder.yudao.module.content.controller.app.bbs.vo.AppBbsPostRespVO;
import cn.iocoder.yudao.module.content.controller.app.bbs.vo.AppBbsTopicDetailRespVO;
import cn.iocoder.yudao.module.content.controller.app.bbs.vo.AppBbsTopicRespVO;
import cn.iocoder.yudao.module.content.controller.app.vo.ContentListRespVO;
import cn.iocoder.yudao.module.content.controller.app.vo.ContentPageReqVO;
import cn.iocoder.yudao.module.content.controller.app.vo.ContentCreateReqVO;
import cn.iocoder.yudao.module.content.dal.dataobject.TopicDO;
import cn.iocoder.yudao.module.content.dal.mysql.TopicMapper;
import cn.iocoder.yudao.module.content.service.ContentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.error;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.NOT_FOUND;

@Tag(name = "用户 APP - 论坛(BBS)")
@RestController
@RequestMapping("/api/v1.0.1/bbs")
@Validated
public class AppBbsController {

    @Resource
    private TopicMapper topicMapper;
    @Resource
    private ContentService contentService;

    @GetMapping("/hot_topics")
    @PermitAll
    @Operation(summary = "热门话题")
    public CommonResult<List<AppBbsTopicRespVO>> getHotTopics(
            @RequestParam(value = "limit", defaultValue = "8") @Min(1) @Max(50) Integer limit) {
        List<TopicDO> topics = topicMapper.selectList(null);
        topics = topics.stream()
                .filter(t -> Objects.equals(t.getStatus(), 1))
                .sorted((a, b) -> {
                    double left = a.getHotScore() != null ? a.getHotScore() : 0.0;
                    double right = b.getHotScore() != null ? b.getHotScore() : 0.0;
                    return Double.compare(right, left);
                })
                .limit(limit)
                .collect(Collectors.toList());
        List<AppBbsTopicRespVO> resp = topics.stream().map(this::convertTopic).collect(Collectors.toList());
        return success(resp);
    }

    @GetMapping("/posts")
    @PermitAll
    @Operation(summary = "帖子列表")
    public CommonResult<PageResult<AppBbsPostRespVO>> getPosts(
            @RequestParam(value = "page", defaultValue = "1") @Min(1) Integer page,
            @RequestParam(value = "limit", defaultValue = "10") @Min(1) @Max(100) Integer limit,
            @RequestParam(value = "sort", defaultValue = "latest") String sort) {
        ContentPageReqVO pageReqVO = new ContentPageReqVO();
        pageReqVO.setPageNo(page);
        pageReqVO.setPageSize(limit);
        PageResult<ContentListRespVO> pageResult = "hot".equalsIgnoreCase(sort)
                ? contentService.getHotContents(pageReqVO, getLoginUserId())
                : contentService.getLatestContents(pageReqVO, getLoginUserId());
        List<AppBbsPostRespVO> list = pageResult.getList().stream()
                .map(this::convertPost)
                .collect(Collectors.toList());
        PageResult<AppBbsPostRespVO> result = new PageResult<>(list, pageResult.getTotal());
        result.setPageNo(page);
        result.setPageSize(limit);
        return success(result);
    }

    @PostMapping("/posts/create")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "创建帖子")
    public CommonResult<Long> createPost(@Valid @RequestBody AppBbsPostCreateReqVO reqVO) {
        // 复用内容发布逻辑
        ContentCreateReqVO createReq = new ContentCreateReqVO();
        createReq.setUserId(getLoginUserId());
        createReq.setTitle(reqVO.getTitle());
        createReq.setContent(reqVO.getContent());
        createReq.setImages(reqVO.getImages());
        createReq.setPublishTopicId(reqVO.getTopicId());
        createReq.setAllowComment(reqVO.getAllowComment());
        createReq.setIsAnonymous(reqVO.getIsAnonymous());
        createReq.setStatus(reqVO.getStatus());
        createReq.setContentType(1); // 论坛帖子默认图文
        if (CharSequenceUtil.isNotBlank(reqVO.getCategory())) {
            createReq.setTags(List.of(reqVO.getCategory()));
        }
        Long id = contentService.createContent(createReq);
        return success(id);
    }

    @GetMapping("/topic/detail")
    @PermitAll
    @Operation(summary = "话题详情")
    public CommonResult<AppBbsTopicDetailRespVO> getTopicDetail(@RequestParam("id") Long id) {
        TopicDO topic = topicMapper.selectById(id);
        if (topic == null) {
            return error(NOT_FOUND);
        }
        AppBbsTopicDetailRespVO vo = new AppBbsTopicDetailRespVO();
        vo.setId(topic.getId());
        vo.setTitle(topic.getName());
        vo.setDescription(topic.getDescription());
        vo.setCover(topic.getCover());
        vo.setParticipants(defaultInt(topic.getParticipantCount()));
        vo.setPosts(defaultInt(topic.getContentCount()));
        vo.setViews(defaultInt(topic.getContentCount()) * 20);
        vo.setTags(topic.getTags() == null ? List.of() : Arrays.asList(topic.getTags()));
        return success(vo);
    }

    @GetMapping("/topic/posts")
    @PermitAll
    @Operation(summary = "话题下的帖子")
    public CommonResult<PageResult<AppBbsPostRespVO>> getTopicPosts(
            @RequestParam("topic_id") Long topicId,
            @RequestParam(value = "page", defaultValue = "1") @Min(1) Integer page,
            @RequestParam(value = "limit", defaultValue = "10") @Min(1) @Max(100) Integer limit) {
        ContentPageReqVO pageReqVO = new ContentPageReqVO();
        pageReqVO.setPageNo(page);
        pageReqVO.setPageSize(limit);
        pageReqVO.setPublishTopicId(topicId);
        PageResult<ContentListRespVO> pageResult = contentService.getTopicContents(pageReqVO, getLoginUserId());
        List<AppBbsPostRespVO> list = pageResult.getList().stream()
                .map(this::convertPost)
                .collect(Collectors.toList());
        PageResult<AppBbsPostRespVO> result = new PageResult<>(list, pageResult.getTotal());
        result.setPageNo(page);
        result.setPageSize(limit);
        return success(result);
    }

    private AppBbsTopicRespVO convertTopic(TopicDO topic) {
        AppBbsTopicRespVO vo = new AppBbsTopicRespVO();
        vo.setId(topic.getId());
        vo.setTitle(topic.getName());
        vo.setDesc(topic.getDescription());
        vo.setImage(topic.getCover());
        vo.setParticipants(defaultInt(topic.getParticipantCount()));
        vo.setPosts(defaultInt(topic.getContentCount()));
        return vo;
    }

    private AppBbsPostRespVO convertPost(ContentListRespVO content) {
        AppBbsPostRespVO vo = new AppBbsPostRespVO();
        vo.setId(content.getId());
        vo.setTitle(content.getTitle());
        String preview = CharSequenceUtil.emptyToDefault(content.getSummary(),
                CharSequenceUtil.emptyToDefault(content.getContentText(), ""));
        vo.setPreview(CharSequenceUtil.sub(preview, 0, 120));
        vo.setUsername(content.getAuthorNickname());
        vo.setAvatar(content.getAuthorAvatar());
        vo.setCategory(content.getChannelName() != null ? content.getChannelName() : content.getPublishTopicName());
        vo.setImage(CollUtil.isNotEmpty(content.getImages()) ? content.getImages().get(0) : content.getCoverImage());
        vo.setLikes(content.getLikeCount());
        vo.setComments(content.getCommentCount());
        vo.setShares(content.getShareCount());
        vo.setTime(content.getPublishTime() != null ? content.getPublishTime() : content.getCreateTime());
        return vo;
    }

    private Integer defaultInt(Integer value) {
        return value == null ? 0 : value;
    }
}
