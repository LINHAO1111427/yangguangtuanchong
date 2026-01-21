package cn.iocoder.yudao.module.content.api;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.content.api.dto.ContentRespDTO;
import cn.iocoder.yudao.module.content.api.dto.ContentUserStatsRespDTO;
import cn.iocoder.yudao.module.content.dal.dataobject.ContentDO;
import cn.iocoder.yudao.module.content.dal.mysql.ContentMapper;
import cn.iocoder.yudao.module.content.service.ContentService;
import cn.iocoder.yudao.module.content.service.follow.FollowService;
import cn.iocoder.yudao.module.content.service.vo.ContentAuthorStats;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * Content API 实现类
 *
 * @author xiaolvshu
 */
@RestController
@Validated
public class ContentApiImpl implements ContentApi {

    @Resource
    private ContentService contentService;
    @Resource
    private ContentMapper contentMapper;
    @Resource
    private FollowService followService;

    @Override
    public CommonResult<ContentRespDTO> getContent(Long id) {
        ContentDO content = contentService.getContent(id);
        return success(convert(content));
    }

    @Override
    public CommonResult<List<ContentRespDTO>> getContentList(Collection<Long> ids) {
        List<ContentDO> list = contentService.getContentList(ids);
        return success(convertList(list));
    }

    @Override
    public CommonResult<List<ContentRespDTO>> getContentListByUserId(Long userId) {
        List<ContentDO> list = contentService.getContentListByUserId(userId);
        return success(convertList(list));
    }

    @Override
    public CommonResult<ContentUserStatsRespDTO> getAuthorStats(Long userId) {
        ContentAuthorStats stats = contentMapper.selectAuthorStats(userId, ContentDO.StatusEnum.PUBLISHED.getStatus());
        if (stats == null) {
            stats = ContentAuthorStats.empty(userId);
        }
        ContentUserStatsRespDTO dto = new ContentUserStatsRespDTO();
        dto.setUserId(stats.getUserId());
        dto.setWorkCount(defaultLong(stats.getWorkCount()));
        dto.setTotalLikeCount(defaultLong(stats.getTotalLikeCount()));
        dto.setTotalCommentCount(defaultLong(stats.getTotalCommentCount()));
        dto.setTotalCollectCount(defaultLong(stats.getTotalCollectCount()));
        dto.setTotalViewCount(defaultLong(stats.getTotalViewCount()));
        dto.setFollowingCount(defaultLong(followService.countFollowing(userId)));
        dto.setFollowersCount(defaultLong(followService.countFans(userId)));
        return success(dto);
    }

    /**
     * Convert ContentDO to ContentRespDTO
     */
    private ContentRespDTO convert(ContentDO content) {
        if (content == null) {
            return null;
        }
        ContentRespDTO dto = new ContentRespDTO();
        dto.setId(content.getId());
        dto.setUserId(content.getUserId());
        dto.setContentType(content.getContentType());
        dto.setTitle(content.getTitle());
        dto.setContent(content.getContent());
        dto.setImages(content.getImages());
        dto.setVideoUrl(content.getVideoUrl());
        dto.setVideoCover(content.getVideoCover());
        dto.setVideoDuration(content.getVideoDuration());
        dto.setVideoWidth(content.getVideoWidth());
        dto.setVideoHeight(content.getVideoHeight());
        dto.setVideoFileSize(content.getVideoFileSize());
        dto.setVideoFormat(content.getVideoFormat());
        dto.setVideoQuality(content.getVideoQuality());
        dto.setIsPublic(content.getIsPublic());
        dto.setAuditStatus(content.getAuditStatus());
        dto.setAuditRemark(content.getAuditRemark());
        dto.setViewCount(content.getViewCount());
        dto.setLikeCount(content.getLikeCount());
        dto.setCommentCount(content.getCommentCount());
        dto.setShareCount(content.getShareCount());
        dto.setCollectCount(content.getCollectCount());
        dto.setForwardCount(content.getForwardCount());
        dto.setCompletionRate(toBigDecimal(content.getCompletionRate()));
        dto.setAvgWatchTime(toBigDecimal(content.getAvgWatchTime()));
        dto.setLastPlayTime(content.getLastPlayTime());
        dto.setIsRecommend(toBoolean(content.getIsRecommend()));
        dto.setIsHot(toBoolean(content.getIsHot()));
        dto.setIsTop(toBoolean(content.getIsTop()));
        dto.setAllowDownload(toBoolean(content.getAllowDownload()));
        dto.setPublishTime(content.getPublishTime());
        dto.setCreateTime(content.getCreateTime());
        dto.setUpdateTime(content.getUpdateTime());
        return dto;
    }

    private BigDecimal toBigDecimal(Double value) {
        return value == null ? null : BigDecimal.valueOf(value);
    }

    private BigDecimal toBigDecimal(Integer value) {
        return value == null ? null : BigDecimal.valueOf(value);
    }

    private Boolean toBoolean(Integer value) {
        return value == null ? null : value != 0;
    }

    /**
     * Convert List<ContentDO> to List<ContentRespDTO>
     */
    private List<ContentRespDTO> convertList(List<ContentDO> list) {
        if (list == null) {
            return null;
        }
        return list.stream().map(this::convert).collect(Collectors.toList());
    }

    private Long defaultLong(Long value) {
        return value == null ? 0L : value;
    }

}
