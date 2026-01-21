package cn.iocoder.yudao.module.content.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.content.controller.app.vo.ContentAuthorProfileRespVO;
import cn.iocoder.yudao.module.content.controller.app.vo.ContentCreateReqVO;
import cn.iocoder.yudao.module.content.controller.app.vo.ContentDetailRespVO;
import cn.iocoder.yudao.module.content.controller.app.vo.ContentListRespVO;
import cn.iocoder.yudao.module.content.controller.app.vo.ContentPageReqVO;
import cn.iocoder.yudao.module.content.controller.app.vo.ContentUpdateReqVO;
import cn.iocoder.yudao.module.content.dal.dataobject.ContentChannelDO;
import cn.iocoder.yudao.module.content.dal.dataobject.ContentDO;
import cn.iocoder.yudao.module.content.dal.dataobject.ContentInteractionDO;
import cn.iocoder.yudao.module.content.dal.mysql.ContentInteractionMapper;
import cn.iocoder.yudao.module.content.dal.mysql.ContentMapper;
import cn.iocoder.yudao.module.content.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.content.framework.kafka.producer.ContentKafkaProducer;
import cn.iocoder.yudao.module.content.service.channel.ContentChannelService;
import cn.iocoder.yudao.module.content.service.favorite.ContentFavoriteService;
import cn.iocoder.yudao.module.content.service.favorite.bo.FavoriteActionReqBO;
import cn.iocoder.yudao.module.content.service.follow.FollowService;
import cn.iocoder.yudao.module.content.service.vo.ContentAuthorStats;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

@Service
public class ContentServiceImpl implements ContentService {

    private static final Logger log = LoggerFactory.getLogger(ContentServiceImpl.class);

    private static final int INTERACTION_VIEW = 1;
    private static final int INTERACTION_LIKE = 2;
    private static final int INTERACTION_COLLECT = 3;
    private static final int INTERACTION_SHARE = 4;
    private static final short NOT_DELETED_FLAG = 0;
    private static final short DELETED_FLAG = 1;

    private static final int MAX_IMAGE_COUNT = 9;
    private static final String SHARE_URL_TEMPLATE = "https://share.xiaolvshu.com/post/%d";
    private static final String EXTRA_LOCATION_KEY = "location";
    private static final String EXTRA_IP_KEY = "ip_address";
    private static final String EXTRA_UA_KEY = "user_agent";

    @Resource
    private ContentMapper contentMapper;
    @Resource
    private TopicService topicService;
    @Resource
    private ContentInteractionMapper contentInteractionMapper;
    @Resource
    private ContentFavoriteService contentFavoriteService;
    @Resource
    private ContentChannelService contentChannelService;
    @Resource
    private MemberUserApi memberUserApi;
    @Resource
    private FollowService followService;
    @Resource
    private ContentKafkaProducer contentKafkaProducer;
    private final ConcurrentMap<Long, List<Map<String, Object>>> reportStorage = new ConcurrentHashMap<>();

    @Override
    public Long createContent(ContentCreateReqVO createReqVO) {
        validateCreateRequest(createReqVO);
        ContentDO content = mapToContent(createReqVO);
        ContentChannelDO channel = contentChannelService.matchChannel(
                createReqVO.getTitle(),
                createReqVO.getContent(),
                createReqVO.getTags());
        if (channel != null) {
            content.setChannelId(channel.getId());
            content.setChannelName(channel.getName());
        }
        contentMapper.insert(content);
        log.info("Create content success, id={}, userId={}", content.getId(), content.getUserId());
        return content.getId();
    }

    @Override
    public void updateContent(ContentUpdateReqVO updateReqVO) {
        ContentDO content = validateContentExists(updateReqVO.getId());
        validateContentOwner(content.getId(), updateReqVO.getUserId());
        applyUpdate(content, updateReqVO);
        contentMapper.updateById(content);
    }

    @Override
    public void deleteContent(Long id) {
        ContentDO content = validateContentExists(id);
        content.setStatus(ContentDO.StatusEnum.DELETED.getStatus());
        content.setDeleted(1);
        content.setUpdateTime(LocalDateTime.now());
        contentMapper.updateById(content);
    }

    @Override
    public ContentDO getContent(Long id) {
        return contentMapper.selectById(id);
    }

    @Override
    public ContentDetailRespVO getContentDetail(Long id, Long currentUserId) {
        ContentDO content = validateContentVisible(id);
        MemberUserRespDTO author = getAuthor(content.getUserId());
        return convertToDetailVO(content, currentUserId, author);
    }

    @Override
    public PageResult<ContentListRespVO> getContentPage(ContentPageReqVO pageReqVO, Long currentUserId) {
        ContentPageReqVO queryReq = clonePageReq(pageReqVO);
        queryReq.setUserId(pageReqVO.getUserId());
        queryReq.setChannelId(pageReqVO.getChannelId());
        if (isRecommendChannel(queryReq.getChannelId())) {
            queryReq.setChannelId(null);
        }
        PageResult<ContentDO> page = selectPageWithOrder(queryReq,
                Comparator.comparing(ContentDO::getPublishTime, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(ContentDO::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder())));
        return convertPage(page, currentUserId);
    }

    @Override
    public List<ContentListRespVO> getContentListByIds(List<Long> contentIds, Long currentUserId) {
        if (CollUtil.isEmpty(contentIds)) {
            return Collections.emptyList();
        }
        List<ContentDO> list = contentMapper.selectListByIds(contentIds);
        Map<Long, ContentDO> mapped = list.stream()
                .filter(this::isNotDeleted)
                .collect(Collectors.toMap(ContentDO::getId, c -> c));
        Map<Long, MemberUserRespDTO> userMap = loadUserMap(mapped.values());
        List<ContentListRespVO> result = new ArrayList<>(contentIds.size());
        for (Long id : contentIds) {
            ContentDO content = mapped.get(id);
            if (content != null) {
                MemberUserRespDTO author = userMap.get(content.getUserId());
                result.add(convertToListVO(content, currentUserId, author));
            }
        }
        return result;
    }

    @Override
    public PageResult<ContentListRespVO> getMyContentPage(ContentPageReqVO pageReqVO, Long currentUserId) {
        PageResult<ContentDO> page = selectPageWithOrder(pageReqVO,
                Comparator.comparing(ContentDO::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder())));
        return convertPage(page, currentUserId);
    }

    @Override
    public List<ContentListRespVO> getUserDrafts(Long userId) {
        List<ContentDO> drafts = contentMapper.selectDraftListByUserId(userId);
        MemberUserRespDTO author = getAuthor(userId);
        return drafts.stream()
                .map(content -> convertToListVO(content, userId, author))
                .collect(Collectors.toList());
    }

    @Override
    public PageResult<ContentListRespVO> getHotContents(ContentPageReqVO pageReqVO, Long currentUserId) {
        PageResult<ContentDO> page = selectPageWithOrder(pageReqVO,
                Comparator.comparing(ContentDO::getHotScore, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(ContentDO::getPublishTime, Comparator.nullsLast(Comparator.reverseOrder())));
        return convertPage(page, currentUserId);
    }

    @Override
    public PageResult<ContentListRespVO> getLatestContents(ContentPageReqVO pageReqVO, Long currentUserId) {
        PageResult<ContentDO> page = selectPageWithOrder(pageReqVO,
                Comparator.comparing(ContentDO::getPublishTime, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(ContentDO::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder())));
        return convertPage(page, currentUserId);
    }

    @Override
    public PageResult<ContentListRespVO> getFollowingContents(Long userId, ContentPageReqVO pageReqVO) {
        List<Long> followingUserIds = followService.getFollowingUserIds(userId);
        if (CollUtil.isEmpty(followingUserIds)) {
            return PageResult.empty();
        }
        ContentPageReqVO query = clonePageReq(pageReqVO);
        query.setUserId(null); // 不限制具体用户，使用 in 过滤
        PageResult<ContentDO> page = selectPageWithOrder(query,
                Comparator.comparing(ContentDO::getPublishTime, Comparator.nullsLast(Comparator.reverseOrder())),
                wrapper -> wrapper.in(ContentDO::getUserId, followingUserIds));
        return convertPage(page, userId);
    }

    @Override
    public PageResult<ContentListRespVO> searchContents(ContentPageReqVO pageReqVO, Long currentUserId) {
        if (StrUtil.isBlank(pageReqVO.getKeyword())) {
            throw exception(ErrorCodeConstants.SEARCH_KEYWORD_EMPTY);
        }
        PageResult<ContentDO> page = selectPageWithOrder(pageReqVO,
                Comparator.comparing(ContentDO::getPublishTime, Comparator.nullsLast(Comparator.reverseOrder())));
        return convertPage(page, currentUserId);
    }

    @Override
    public PageResult<ContentListRespVO> getTopicContents(ContentPageReqVO pageReqVO, Long currentUserId) {
        PageResult<ContentDO> page = selectPageWithOrder(pageReqVO,
                Comparator.comparing(ContentDO::getPublishTime, Comparator.nullsLast(Comparator.reverseOrder())));
        return convertPage(page, currentUserId);
    }

    @Override
    public ContentAuthorProfileRespVO getAuthorProfile(Long authorId, Long currentUserId) {
        if (authorId == null) {
            throw exception(ErrorCodeConstants.CONTENT_AUTHOR_NOT_EXISTS);
        }
        MemberUserRespDTO author = getAuthor(authorId);
        if (author == null) {
            throw exception(ErrorCodeConstants.CONTENT_AUTHOR_NOT_EXISTS);
        }
        ContentAuthorStats stats = contentMapper.selectAuthorStats(authorId, ContentDO.StatusEnum.PUBLISHED.getStatus());
        if (stats == null) {
            stats = ContentAuthorStats.empty(authorId);
        }
        ContentAuthorProfileRespVO vo = new ContentAuthorProfileRespVO();
        vo.setUserId(authorId);
        vo.setNickname(StrUtil.blankToDefault(author.getNickname(), "匿名用户"));
        vo.setAvatar(author.getAvatar());
        vo.setCustomId(author.getCustomId() != null ? String.valueOf(author.getCustomId()) : null);
        vo.setBackgroundUrl(author.getBackgroundUrl());
        vo.setPoint(author.getPoint());
        vo.setWorkCount(stats.getWorkCount());
        vo.setFollowingCount(followService.countFollowing(authorId));
        vo.setFollowersCount(followService.countFans(authorId));
        vo.setIsFollowed(currentUserId != null && followService.isFollowingUser(currentUserId, authorId));
        vo.setTotalLikeCount(stats.getTotalLikeCount());
        vo.setTotalCommentCount(stats.getTotalCommentCount());
        vo.setTotalCollectCount(stats.getTotalCollectCount());
        vo.setTotalViewCount(stats.getTotalViewCount());
        vo.setMine(currentUserId != null && Objects.equals(currentUserId, authorId));
        vo.setJoinTime(author.getCreateTime());
        return vo;
    }

    @Override
    public PageResult<ContentListRespVO> getAuthorContentPage(Long authorId, ContentPageReqVO pageReqVO, Long currentUserId) {
        ContentPageReqVO query = clonePageReq(pageReqVO);
        query.setUserId(authorId);
        return getContentPage(query, currentUserId);
    }

    @Override
    public void validateContentOwner(Long contentId, Long userId) {
        ContentDO content = validateContentExists(contentId);
        if (!Objects.equals(content.getUserId(), userId)) {
            throw exception(ErrorCodeConstants.CONTENT_ACCESS_DENIED);
        }
    }

    @Override
    public void recordContentView(Long contentId, Long userId, String ipAddress, String userAgent) {
        ContentDO content = validateContentVisible(contentId);
        content.setViewCount(safeInt(content.getViewCount()) + 1);
        content.setLastPlayTime(LocalDateTime.now());
        content.setUpdateTime(LocalDateTime.now());
        contentMapper.updateById(content);
        upsertInteraction(contentId, userId, INTERACTION_VIEW, ipAddress, userAgent, true);
        log.debug("Record view contentId={}, userId={}, ip={}", contentId, userId, ipAddress);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleLike(Long contentId, Long userId, String ipAddress, String userAgent) {
        ContentDO content = validateContentVisible(contentId);
        boolean liked = toggleInteraction(contentId, userId, INTERACTION_LIKE, ipAddress, userAgent);
        contentMapper.updateLikeCount(contentId, liked ? 1 : -1);
        if (liked) {
            Map<String, Object> event = new HashMap<>();
            event.put("behaviorType", "like");
            event.put("action", "add");
            event.put("actorUserId", userId);
            event.put("targetUserId", content.getUserId());
            event.put("contentId", contentId);
            event.put("eventTime", LocalDateTime.now().toString());
            contentKafkaProducer.sendBehaviorEvent(event);
        }
        log.debug("Toggle like contentId={}, userId={}, liked={}", contentId, userId, liked);
        return liked;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleCollect(Long contentId, Long userId, String ipAddress, String userAgent) {
        ContentDO content = validateContentVisible(contentId);
        FavoriteActionReqBO reqBO = new FavoriteActionReqBO();
        reqBO.setContentId(contentId);
        boolean collected = contentFavoriteService.toggleFavorite(userId, reqBO);
        upsertInteraction(contentId, userId, INTERACTION_COLLECT, ipAddress, userAgent, collected);
        contentMapper.updateCollectCount(contentId, collected ? 1 : -1);
        if (collected) {
            Map<String, Object> event = new HashMap<>();
            event.put("behaviorType", "collect");
            event.put("action", "add");
            event.put("actorUserId", userId);
            event.put("targetUserId", content.getUserId());
            event.put("contentId", contentId);
            event.put("eventTime", LocalDateTime.now().toString());
            contentKafkaProducer.sendBehaviorEvent(event);
        }
        log.debug("Toggle collect contentId={}, userId={}, collected={}", contentId, userId, collected);
        return collected;
    }

    @Override
    public void recordContentShare(Long contentId, Long userId, String platform, String ipAddress, String userAgent) {
        ContentDO content = validateContentVisible(contentId);
        content.setShareCount(safeInt(content.getShareCount()) + 1);
        content.setUpdateTime(LocalDateTime.now());
        contentMapper.updateById(content);
        upsertInteraction(contentId, userId, INTERACTION_SHARE, ipAddress, userAgent, true);
        log.debug("Record share contentId={}, userId={}, platform={}", contentId, userId, platform);
    }

    @Override
    public void reportContent(Long contentId, Long userId, String reason, String description, String ipAddress, String userAgent) {
        ContentDO content = validateContentVisible(contentId);
        if (Objects.equals(content.getUserId(), userId)) {
            throw exception(ErrorCodeConstants.REPORT_SELF_NOT_ALLOWED);
        }
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("userId", userId);
        report.put("reason", reason);
        report.put("description", description);
        report.put("ip", ipAddress);
        report.put("userAgent", userAgent);
        report.put("time", LocalDateTime.now());
        reportStorage.computeIfAbsent(contentId, k -> Collections.synchronizedList(new ArrayList<>())).add(report);
        log.info("Receive content report contentId={}, userId={}, reason={}", contentId, userId, reason);
    }

    @Override
    public String generateShareUrl(Long contentId) {
        return String.format(SHARE_URL_TEMPLATE, contentId);
    }

    @Override
    public void auditContent(Long contentId, Integer auditStatus, String auditRemark) {
        ContentDO content = validateContentExists(contentId);
        content.setAuditStatus(auditStatus);
        content.setAuditRemark(auditRemark);
        content.setUpdateTime(LocalDateTime.now());
        contentMapper.updateById(content);
    }

    @Override
    public void updateContentHotScore(Long contentId) {
        ContentDO content = validateContentExists(contentId);
        double hotScore = safeInt(content.getViewCount()) * 0.3
                + safeInt(content.getLikeCount()) * 1.5
                + safeInt(content.getCommentCount()) * 1.2
                + safeInt(content.getShareCount()) * 2.0
                + safeInt(content.getCollectCount()) * 1.8;
        content.setHotScore(hotScore);
        content.setUpdateTime(LocalDateTime.now());
        contentMapper.updateById(content);
    }

    @Override
    public void updateContentRecommendScore(Long contentId) {
        ContentDO content = validateContentExists(contentId);
        double recommendScore = ObjectUtil.defaultIfNull(content.getHotScore(), 0D)
                + safeInt(content.getCollectCount()) * 2.0
                + safeInt(content.getLikeCount()) * 1.0;
        content.setRecommendScore(recommendScore);
        content.setUpdateTime(LocalDateTime.now());
        contentMapper.updateById(content);
    }

    @Override
    public List<ContentDO> getContentList(Collection<Long> ids) {
        return contentMapper.selectListByIds(ids).stream()
                .filter(this::isNotDeleted)
                .collect(Collectors.toList());
    }

    @Override
    public List<ContentDO> getContentListByUserId(Long userId) {
        return contentMapper.selectListByUserId(userId, false);
    }

    // ======================= helper methods =======================

    private void validateCreateRequest(ContentCreateReqVO reqVO) {
        if (reqVO.getImages() != null && reqVO.getImages().size() > MAX_IMAGE_COUNT) {
            throw exception(ErrorCodeConstants.CONTENT_IMAGES_TOO_MANY);
        }
    }

    private ContentDO mapToContent(ContentCreateReqVO reqVO) {
        LocalDateTime now = LocalDateTime.now();
        ContentDO content = new ContentDO();
        content.setUserId(reqVO.getUserId());
        content.setContentType(reqVO.getContentType());
        content.setTitle(reqVO.getTitle());
        content.setContent(reqVO.getContent());
        content.setPublishTopicId(reqVO.getPublishTopicId());
        content.setImages(copyList(reqVO.getImages()));
        content.setVideoUrl(reqVO.getVideoUrl());
        content.setVideoCover(reqVO.getVideoCover());
        content.setVideoDuration(reqVO.getVideoDuration());
        content.setVideoWidth(reqVO.getVideoWidth());
        content.setVideoHeight(reqVO.getVideoHeight());
        content.setVideoFileSize(reqVO.getVideoFileSize());
        content.setVideoFormat(reqVO.getVideoFormat());
        content.setVideoQuality(reqVO.getVideoQuality());
        content.setAudioDuration(reqVO.getAudioDuration());
        content.setIsPublic(ObjectUtil.defaultIfNull(reqVO.getIsPublic(), 1));
        Integer status = ObjectUtil.defaultIfNull(reqVO.getStatus(), ContentDO.StatusEnum.PUBLISHED.getStatus());
        content.setStatus(status);
        content.setAllowComment(ObjectUtil.defaultIfNull(reqVO.getAllowComment(), 1));
        content.setTags(copyList(reqVO.getTags()));
        content.setExtra(buildExtra(reqVO.getExtra(), reqVO.getLocation(), reqVO.getIpAddress(), reqVO.getUserAgent()));
        content.setCreateTime(now);
        content.setUpdateTime(now);
        if (Objects.equals(status, ContentDO.StatusEnum.PUBLISHED.getStatus())) {
            content.setPublishTime(now);
        }
        content.setAuditStatus(ContentDO.AuditStatusEnum.PENDING.getStatus());
        content.setViewCount(0);
        content.setLikeCount(0);
        content.setCommentCount(0);
        content.setShareCount(0);
        content.setCollectCount(0);
        content.setForwardCount(0);
        content.setHotScore(0D);
        content.setRecommendScore(0D);
        return content;
    }

    private void applyUpdate(ContentDO content, ContentUpdateReqVO reqVO) {
        LocalDateTime now = LocalDateTime.now();
        if (reqVO.getTitle() != null) {
            content.setTitle(reqVO.getTitle());
        }
        if (reqVO.getContent() != null) {
            content.setContent(reqVO.getContent());
        }
        if (reqVO.getContentType() != null) {
            content.setContentType(reqVO.getContentType());
        }
        if (reqVO.getPublishTopicId() != null) {
            content.setPublishTopicId(reqVO.getPublishTopicId());
        }
        if (reqVO.getImages() != null) {
            if (reqVO.getImages().size() > MAX_IMAGE_COUNT) {
                throw exception(ErrorCodeConstants.CONTENT_IMAGES_TOO_MANY);
            }
            content.setImages(copyList(reqVO.getImages()));
        }
        if (reqVO.getVideoUrl() != null) {
            content.setVideoUrl(reqVO.getVideoUrl());
        }
        if (reqVO.getVideoCover() != null) {
            content.setVideoCover(reqVO.getVideoCover());
        }
        if (reqVO.getVideoDuration() != null) {
            content.setVideoDuration(reqVO.getVideoDuration());
        }
        if (reqVO.getVideoWidth() != null) {
            content.setVideoWidth(reqVO.getVideoWidth());
        }
        if (reqVO.getVideoHeight() != null) {
            content.setVideoHeight(reqVO.getVideoHeight());
        }
        if (reqVO.getVideoFileSize() != null) {
            content.setVideoFileSize(reqVO.getVideoFileSize());
        }
        if (reqVO.getVideoFormat() != null) {
            content.setVideoFormat(reqVO.getVideoFormat());
        }
        if (reqVO.getVideoQuality() != null) {
            content.setVideoQuality(reqVO.getVideoQuality());
        }
        if (reqVO.getAudioDuration() != null) {
            content.setAudioDuration(reqVO.getAudioDuration());
        }
        if (reqVO.getIsPublic() != null) {
            content.setIsPublic(reqVO.getIsPublic());
        }
        if (reqVO.getAllowComment() != null) {
            content.setAllowComment(reqVO.getAllowComment());
        }
        if (reqVO.getStatus() != null) {
            content.setStatus(reqVO.getStatus());
            if (Objects.equals(reqVO.getStatus(), ContentDO.StatusEnum.PUBLISHED.getStatus())
                    && content.getPublishTime() == null) {
                content.setPublishTime(now);
            }
        }
        if (reqVO.getTags() != null) {
            content.setTags(copyList(reqVO.getTags()));
        }
        if (reqVO.getExtra() != null || reqVO.getLocation() != null) {
            Map<String, Object> merged = buildExtra(reqVO.getExtra(), reqVO.getLocation(), null, null);
            Map<String, Object> base = Optional.ofNullable(content.getExtra())
                    .map(LinkedHashMap::new)
                    .orElseGet(LinkedHashMap::new);
            if (merged != null) {
                base.putAll(merged);
            }
            content.setExtra(base.isEmpty() ? null : base);
        }
        content.setUpdateTime(now);
    }

    private List<String> copyList(List<String> source) {
        if (source == null) {
            return null;
        }
        return source.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private Map<String, Object> buildExtra(Map<String, Object> extra,
                                           Map<String, Object> location,
                                           String ip,
                                           String userAgent) {
        Map<String, Object> result = extra == null ? new LinkedHashMap<>() : new LinkedHashMap<>(extra);
        if (location != null && !location.isEmpty()) {
            result.put(EXTRA_LOCATION_KEY, new LinkedHashMap<>(location));
        }
        if (StrUtil.isNotBlank(ip)) {
            result.put(EXTRA_IP_KEY, ip);
        }
        if (StrUtil.isNotBlank(userAgent)) {
            result.put(EXTRA_UA_KEY, userAgent);
        }
        return result.isEmpty() ? null : result;
    }

    private PageResult<ContentListRespVO> convertPage(PageResult<ContentDO> page, Long currentUserId) {
        List<ContentDO> data = page.getList();
        Map<Long, Boolean> likedMap = loadInteractionFlags(data, currentUserId, INTERACTION_LIKE);
        Map<Long, Boolean> collectedMap = loadInteractionFlags(data, currentUserId, INTERACTION_COLLECT);
        Map<Long, MemberUserRespDTO> userMap = loadUserMap(data);
        List<ContentListRespVO> list = data.stream()
                .map(content -> convertToListVO(content, currentUserId,
                        likedMap.getOrDefault(content.getId(), Boolean.FALSE),
                        collectedMap.getOrDefault(content.getId(), Boolean.FALSE),
                        userMap.get(content.getUserId())))
                .collect(Collectors.toList());
        return new PageResult<>(list, page.getTotal());
    }

    private ContentListRespVO convertToListVO(ContentDO content, Long currentUserId, MemberUserRespDTO author) {
        Boolean likedFlag = currentUserId != null && hasInteraction(content.getId(), currentUserId, INTERACTION_LIKE);
        Boolean collectFlag = currentUserId != null && hasInteraction(content.getId(), currentUserId, INTERACTION_COLLECT);
        return convertToListVO(content, currentUserId, likedFlag, collectFlag, author);
    }

    private ContentListRespVO convertToListVO(ContentDO content,
                                              Long currentUserId,
                                              Boolean likedFlag,
                                              Boolean collectedFlag,
                                              MemberUserRespDTO author) {
        ContentListRespVO vo = new ContentListRespVO();
        vo.setId(content.getId());
        vo.setUserId(content.getUserId());
        vo.setTitle(content.getTitle());
        vo.setSummary(buildSummary(content.getContent()));
        vo.setContentText(buildContentText(content.getContent()));
        vo.setContentType(content.getContentType());
        vo.setPublishTopicId(content.getPublishTopicId());
        vo.setPublishTopicName(resolveTopicName(content.getPublishTopicId()));
        vo.setChannelId(content.getChannelId());
        vo.setChannelName(content.getChannelName());
        vo.setCoverImage(resolveCoverImage(content));
        vo.setImages(Optional.ofNullable(content.getImages()).orElse(Collections.emptyList()));
        vo.setVideoUrl(content.getVideoUrl());
        vo.setVideoCover(content.getVideoCover());
        vo.setVideoDuration(content.getVideoDuration());
        vo.setVideoWidth(content.getVideoWidth());
        vo.setVideoHeight(content.getVideoHeight());
        vo.setLikeCount(safeInt(content.getLikeCount()));
        vo.setCommentCount(safeInt(content.getCommentCount()));
        vo.setShareCount(safeInt(content.getShareCount()));
        vo.setCollectCount(safeInt(content.getCollectCount()));
        vo.setViewCount(safeInt(content.getViewCount()));
        vo.setIsMine(currentUserId != null && Objects.equals(content.getUserId(), currentUserId));
        vo.setIsLiked(Boolean.TRUE.equals(likedFlag));
        vo.setIsCollected(Boolean.TRUE.equals(collectedFlag));
        vo.setIsFollowed(currentUserId != null && isFollowed(currentUserId, content));
        if (author != null) {
            vo.setAuthorNickname(StrUtil.blankToDefault(author.getNickname(), "匿名用户"));
            vo.setAuthorAvatar(author.getAvatar());
        } else {
            vo.setAuthorNickname("匿名用户");
        }
        vo.setExtra(sanitizeExtra(content.getExtra(), false));
        vo.setCreateTime(content.getCreateTime());
        vo.setUpdateTime(content.getUpdateTime());
        vo.setPublishTime(content.getPublishTime());
        return vo;
    }

    private ContentDetailRespVO convertToDetailVO(ContentDO content, Long currentUserId, MemberUserRespDTO author) {
        ContentDetailRespVO detail = new ContentDetailRespVO();
        ContentListRespVO base = convertToListVO(content, currentUserId, author);
        detail.setId(base.getId());
        detail.setUserId(base.getUserId());
        detail.setTitle(base.getTitle());
        detail.setSummary(base.getSummary());
        detail.setContentText(base.getContentText());
        detail.setPublishTopicId(base.getPublishTopicId());
        detail.setPublishTopicName(base.getPublishTopicName());
        detail.setChannelId(base.getChannelId());
        detail.setChannelName(base.getChannelName());
        detail.setCoverImage(base.getCoverImage());
        detail.setImages(base.getImages());
        detail.setVideoUrl(base.getVideoUrl());
        detail.setVideoDuration(base.getVideoDuration());
        detail.setVideoWidth(base.getVideoWidth());
        detail.setVideoHeight(base.getVideoHeight());
        detail.setLikeCount(base.getLikeCount());
        detail.setCommentCount(base.getCommentCount());
        detail.setShareCount(base.getShareCount());
        detail.setCollectCount(base.getCollectCount());
        detail.setViewCount(base.getViewCount());
        detail.setIsMine(base.getIsMine());
        detail.setIsLiked(base.getIsLiked());
        detail.setIsCollected(base.getIsCollected());
        detail.setIsFollowed(base.getIsFollowed());
        detail.setAuthorNickname(base.getAuthorNickname());
        detail.setAuthorAvatar(base.getAuthorAvatar());
        detail.setCreateTime(base.getCreateTime());
        detail.setUpdateTime(base.getUpdateTime());
        detail.setPublishTime(base.getPublishTime());
        detail.setContent(content.getContent());

        Map<String, Object> extension = sanitizeExtra(content.getExtra(), true);
        Map<String, Object> location = null;
        if (extension.containsKey(EXTRA_LOCATION_KEY)) {
            Object loc = extension.remove(EXTRA_LOCATION_KEY);
            if (loc instanceof Map) {
                //noinspection unchecked
                location = new LinkedHashMap<>((Map<String, Object>) loc);
            }
        }
        detail.setLocation(location);
        detail.setExtension(extension);
        detail.setExtra(extension);
        return detail;
    }

    private String resolveCoverImage(ContentDO content) {
        if (StrUtil.isNotBlank(content.getCoverImage())) {
            return content.getCoverImage();
        }
        if (StrUtil.isNotBlank(content.getVideoCover())) {
            return content.getVideoCover();
        }
        List<String> images = content.getImages();
        if (CollUtil.isNotEmpty(images)) {
            return images.get(0);
        }
        return null;
    }

    private Map<Long, MemberUserRespDTO> loadUserMap(Collection<ContentDO> contents) {
        if (CollUtil.isEmpty(contents)) {
            return Collections.emptyMap();
        }
        List<Long> userIds = contents.stream()
                .map(ContentDO::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(userIds)) {
            return Collections.emptyMap();
        }
        try {
            return memberUserApi.getUserMap(userIds);
        } catch (Exception ex) {
            log.warn("Failed to batch load authors {}", userIds, ex);
            return Collections.emptyMap();
        }
    }

    private MemberUserRespDTO getAuthor(Long userId) {
        if (userId == null) {
            return null;
        }
        try {
            return memberUserApi.getUser(userId).getCheckedData();
        } catch (Exception ex) {
            log.warn("Failed to load author userId={}", userId, ex);
            return null;
        }
    }

    private Map<Long, Boolean> loadInteractionFlags(List<ContentDO> contents, Long userId, int type) {
        if (CollUtil.isEmpty(contents) || userId == null) {
            return Collections.emptyMap();
        }
        List<Long> ids = contents.stream().map(ContentDO::getId).collect(Collectors.toList());
        List<ContentInteractionDO> interactions =
                contentInteractionMapper.selectByUserAndContentIds(userId, ids, type);
        Map<Long, Boolean> result = new LinkedHashMap<>();
        for (ContentInteractionDO interaction : interactions) {
            result.put(interaction.getContentId(), !isDeletedFlag(interaction.getDeleted()));
        }
        return result;
    }

    private boolean hasInteraction(Long contentId, Long userId, int type) {
        if (userId == null || contentId == null) {
            return false;
        }
        ContentInteractionDO interaction = contentInteractionMapper.selectOne(contentId, userId, type);
        return interaction != null && !isDeletedFlag(interaction.getDeleted());
    }

    private boolean toggleInteraction(Long contentId, Long userId, int type, String ip, String ua) {
        ContentInteractionDO interaction = contentInteractionMapper.selectOneIncludeDeleted(contentId, userId, type);
        boolean nextStatus = interaction == null || isDeletedFlag(interaction.getDeleted());
        persistInteraction(interaction, contentId, userId, type, ip, ua, nextStatus);
        return nextStatus;
    }

    private void upsertInteraction(Long contentId, Long userId, int type, String ip, String ua, boolean active) {
        ContentInteractionDO interaction = contentInteractionMapper.selectOneIncludeDeleted(contentId, userId, type);
        persistInteraction(interaction, contentId, userId, type, ip, ua, active);
    }

    private void persistInteraction(ContentInteractionDO interaction,
                                    Long contentId,
                                    Long userId,
                                    int type,
                                    String ip,
                                    String ua,
                                    boolean active) {
        if (userId == null) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        if (interaction == null) {
            interaction = new ContentInteractionDO();
            interaction.setContentId(contentId);
            interaction.setUserId(userId);
            interaction.setInteractionType(type);
            interaction.setCreateTime(now);
            interaction.setCreator(String.valueOf(userId));
            interaction.setDeleted(NOT_DELETED_FLAG);
        }
        interaction.setIpAddress(ip);
        interaction.setUserAgent(ua);
        interaction.setDeleted(active ? NOT_DELETED_FLAG : DELETED_FLAG);
        interaction.setUpdateTime(now);
        interaction.setUpdater(String.valueOf(userId));
        if (interaction.getId() == null) {
            contentInteractionMapper.insert(interaction);
        } else {
            contentInteractionMapper.updateByIdIncludeDeleted(interaction);
        }
    }

    private PageResult<ContentDO> selectPageWithOrder(ContentPageReqVO pageReqVO,
                                                      Comparator<ContentDO> comparator) {
        return selectPageWithOrder(pageReqVO, comparator, null);
    }

    private PageResult<ContentDO> selectPageWithOrder(ContentPageReqVO pageReqVO,
                                                      Comparator<ContentDO> comparator,
                                                      java.util.function.Consumer<cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX<ContentDO>> wrapperCustomizer) {
        cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX<ContentDO> wrapper =
                contentMapper.buildPageWrapper(pageReqVO);
        if (wrapperCustomizer != null) {
            wrapperCustomizer.accept(wrapper);
        }
        PageResult<ContentDO> page = contentMapper.selectPage(pageReqVO, wrapper);
        if (page.getList() != null && comparator != null) {
            page.getList().sort(comparator);
        }
        return page;
    }

    private ContentDO validateContentExists(Long id) {
        ContentDO content = contentMapper.selectById(id);
        if (content == null) {
            throw exception(ErrorCodeConstants.CONTENT_NOT_EXISTS);
        }
        return content;
    }

    private ContentDO validateContentVisible(Long id) {
        ContentDO content = validateContentExists(id);
        if (!isNotDeleted(content)) {
            throw exception(ErrorCodeConstants.CONTENT_NOT_EXISTS);
        }
        return content;
    }

    private boolean isNotDeleted(ContentDO content) {
        return !Boolean.TRUE.equals(content.getDeleted())
                && !Objects.equals(content.getStatus(), ContentDO.StatusEnum.DELETED.getStatus());
    }

    private boolean isDeletedFlag(Short flag) {
        return flag != null && flag.equals(DELETED_FLAG);
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private String buildSummary(String content) {
        if (StrUtil.isBlank(content)) {
            return "";
        }
        String plain = StrUtil.cleanBlank(StrUtil.replace(content, "\n", " "));
        return plain.length() <= 120 ? plain : plain.substring(0, 120);
    }

    private String buildContentText(String content) {
        if (content == null) {
            return "";
        }
        return content.length() <= 300 ? content : content.substring(0, 300);
    }

    private boolean isFollowed(Long userId, ContentDO content) {
        return followService.isFollowingUser(userId, content.getUserId())
                || (content.getPublishTopicId() != null
                && followService.isTopicFollowed(userId, content.getPublishTopicId()));
    }

    private Map<String, Object> sanitizeExtra(Map<String, Object> extra, boolean includeLocation) {
        if (extra == null || extra.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Object> result = new LinkedHashMap<>(extra);
        result.remove(EXTRA_IP_KEY);
        result.remove(EXTRA_UA_KEY);
        if (!includeLocation) {
            result.remove(EXTRA_LOCATION_KEY);
        }
        return result;
    }

    private String resolveTopicName(Long topicId) {
        if (topicId == null) {
            return null;
        }
        try {
            return Optional.ofNullable(topicService.getTopic(topicId))
                    .map(topic -> StrUtil.blankToDefault(topic.getName(), null))
                    .orElse(null);
        } catch (Exception ex) {
            log.warn("Failed to resolve topic name id={}", topicId, ex);
            return null;
        }
    }

    private ContentPageReqVO clonePageReq(ContentPageReqVO source) {
        ContentPageReqVO target = new ContentPageReqVO();
        target.setPageNo(source.getPageNo());
        target.setPageSize(source.getPageSize());
        target.setKeyword(source.getKeyword());
        target.setPublishTopicId(source.getPublishTopicId());
        target.setChannelId(source.getChannelId());
        target.setContentType(source.getContentType());
        target.setAuditStatus(source.getAuditStatus());
        target.setStatus(source.getStatus());
        target.setIsPublic(source.getIsPublic());
        target.setTitle(source.getTitle());
        target.setContent(source.getContent());
        target.setCreateTimeStart(source.getCreateTimeStart());
        target.setCreateTimeEnd(source.getCreateTimeEnd());
        return target;
    }

    private boolean isRecommendChannel(Long channelId) {
        if (channelId == null) {
            return false;
        }
        try {
            ContentChannelDO channel = contentChannelService.getChannel(channelId);
            return channel != null && "recommend".equalsIgnoreCase(channel.getCode());
        } catch (Exception ex) {
            log.warn("Failed to fetch channel info channelId={}", channelId, ex);
            return false;
        }
    }

    @Override
    public PageResult<ContentListRespVO> getMyLikedContents(Long userId, ContentPageReqVO pageReqVO) {
        // Calculate offset and limit
        int pageNo = Math.max(1, ObjectUtil.defaultIfNull(pageReqVO.getPageNo(), 1));
        int pageSize = ObjectUtil.defaultIfNull(pageReqVO.getPageSize(), 20);
        int offset = (pageNo - 1) * pageSize;

        // Query liked interactions
        List<ContentInteractionDO> interactions = contentInteractionMapper.selectPageByUserAndType(
                userId, INTERACTION_LIKE, offset, pageSize);

        if (CollUtil.isEmpty(interactions)) {
            return PageResult.empty();
        }

        // Extract content IDs
        List<Long> contentIds = interactions.stream()
                .map(ContentInteractionDO::getContentId)
                .collect(Collectors.toList());

        // Get content details preserving order
        List<ContentListRespVO> contentList = getContentListByIds(contentIds, userId);

        // Get total count
        Long total = contentInteractionMapper.countByUserAndType(userId, INTERACTION_LIKE);

        return new PageResult<>(contentList, total);
    }

    @Override
    public PageResult<ContentListRespVO> getMyCollectedContents(Long userId, ContentPageReqVO pageReqVO) {
        // Calculate offset and limit
        int pageNo = Math.max(1, ObjectUtil.defaultIfNull(pageReqVO.getPageNo(), 1));
        int pageSize = ObjectUtil.defaultIfNull(pageReqVO.getPageSize(), 20);
        int offset = (pageNo - 1) * pageSize;

        // Query collected interactions
        List<ContentInteractionDO> interactions = contentInteractionMapper.selectPageByUserAndType(
                userId, INTERACTION_COLLECT, offset, pageSize);

        if (CollUtil.isEmpty(interactions)) {
            return PageResult.empty();
        }

        // Extract content IDs
        List<Long> contentIds = interactions.stream()
                .map(ContentInteractionDO::getContentId)
                .collect(Collectors.toList());

        // Get content details preserving order
        List<ContentListRespVO> contentList = getContentListByIds(contentIds, userId);

        // Get total count
        Long total = contentInteractionMapper.countByUserAndType(userId, INTERACTION_COLLECT);

        return new PageResult<>(contentList, total);
    }

    @Override
    public PageResult<ContentListRespVO> getMyViewHistory(Long userId, ContentPageReqVO pageReqVO) {
        if (userId == null) {
            return PageResult.empty();
        }
        int pageNo = Math.max(1, ObjectUtil.defaultIfNull(pageReqVO.getPageNo(), 1));
        int pageSize = ObjectUtil.defaultIfNull(pageReqVO.getPageSize(), 20);
        int offset = (pageNo - 1) * pageSize;

        List<ContentInteractionDO> interactions = contentInteractionMapper.selectPageByUserAndType(
                userId, INTERACTION_VIEW, offset, pageSize);
        if (CollUtil.isEmpty(interactions)) {
            return PageResult.empty();
        }
        List<Long> contentIds = interactions.stream()
                .map(ContentInteractionDO::getContentId)
                .collect(Collectors.toList());
        List<ContentListRespVO> contentList = getContentListByIds(contentIds, userId);
        Long total = contentInteractionMapper.countByUserAndType(userId, INTERACTION_VIEW);
        return new PageResult<>(contentList, total);
    }

    @Override
    public void deleteMyViewHistory(Long userId, Collection<Long> contentIds) {
        if (userId == null || CollUtil.isEmpty(contentIds)) {
            return;
        }
        contentInteractionMapper.softDeleteByUserAndTypeAndContentIds(userId, INTERACTION_VIEW, contentIds);
    }

    @Override
    public void clearMyViewHistory(Long userId) {
        if (userId == null) {
            return;
        }
        contentInteractionMapper.softDeleteByUserAndType(userId, INTERACTION_VIEW);
    }
}
