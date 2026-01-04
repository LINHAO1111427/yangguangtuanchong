package cn.iocoder.yudao.module.content.service.recommend;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.content.dal.dataobject.ContentDO;
import cn.iocoder.yudao.module.content.dal.dataobject.ContentFavoriteRecordDO;
import cn.iocoder.yudao.module.content.dal.dataobject.ContentInteractionDO;
import cn.iocoder.yudao.module.content.dal.mysql.ContentFavoriteRecordMapper;
import cn.iocoder.yudao.module.content.dal.mysql.ContentInteractionMapper;
import cn.iocoder.yudao.module.content.dal.mysql.ContentMapper;
import cn.iocoder.yudao.module.content.service.ContentService;
import cn.iocoder.yudao.module.content.service.follow.FollowService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.module.content.constants.ContentConstants.BehaviorType.LIKE;
import static cn.iocoder.yudao.module.content.constants.ContentConstants.BehaviorType.VIEW;

/**
 * 内容推荐服务，实现多策略混排（标签、关注、热度、探索）。
 */
@Service
public class ContentRecommendService {

    private static final Logger log = LoggerFactory.getLogger(ContentRecommendService.class);

    private static final double TAG_WEIGHT = 0.45;
    private static final double FOLLOW_WEIGHT = 0.25;
    private static final double HOT_WEIGHT = 0.2;
    private static final double EXPLORE_WEIGHT = 0.1;
    private static final int RECENT_CONTENT_POOL_SIZE = 200;

    @Resource
    private ContentService contentService;
    @Resource
    private ContentMapper contentMapper;
    @Resource
    private ContentFavoriteRecordMapper contentFavoriteRecordMapper;
    @Resource
    private ContentInteractionMapper contentInteractionMapper;
    @Resource
    private FollowService followService;

    public RecommendResult getRecommendFeed(Long userId, int page, int pageSize) {
        return getRecommendFeed(userId, page, pageSize, null);
    }

    public RecommendResult getRecommendFeed(Long userId, int page, int pageSize, Long channelId) {
        Map<Long, RankedContent> ranking = new LinkedHashMap<>();

        appendCandidates(ranking, buildTagCandidates(userId, pageSize * 2, channelId));
        appendCandidates(ranking, buildFollowCandidates(userId, pageSize, channelId));
        appendCandidates(ranking, buildHotCandidates(pageSize * 3, channelId));
        appendCandidates(ranking, buildExploreCandidates(pageSize * 2, channelId));

        List<RankedContent> sorted = ranking.values().stream()
                .sorted(Comparator.comparing(RankedContent::getScore).reversed())
                .collect(Collectors.toList());
        int fromIndex = Math.max((page - 1) * pageSize, 0);
        int toIndex = Math.min(fromIndex + pageSize, sorted.size());
        List<RankedContent> pageData = fromIndex >= sorted.size()
                ? Collections.emptyList()
                : sorted.subList(fromIndex, toIndex);

        RecommendResult result = new RecommendResult();
        result.setRankedContents(pageData);
        result.setTotalCount((long) sorted.size());
        result.setRecommendStrategy(buildStrategySummary(pageData));
        return result;
    }

    public RecommendResult getHotContent(int page, int pageSize) {
        List<RankedContent> hotRank = buildHotCandidates(pageSize * page, null);
        int fromIndex = Math.max((page - 1) * pageSize, 0);
        int toIndex = Math.min(fromIndex + pageSize, hotRank.size());
        List<RankedContent> pageData = fromIndex >= hotRank.size()
                ? Collections.emptyList()
                : hotRank.subList(fromIndex, toIndex);
        RecommendResult result = new RecommendResult();
        result.setRankedContents(pageData);
        result.setTotalCount((long) hotRank.size());
        result.setRecommendStrategy("hot-basic");
        return result;
    }

    public void recordUserBehavior(Long userId, Long contentId, String actionType) {
        log.debug("Record behavior userId={}, contentId={}, action={}", userId, contentId, actionType);
        // 行为用于热度算法，本次实现仅记录日志，后续可扩展写入 Redis/ClickHouse。
    }

    public void updateContentHotScore(Long contentId,
                                      Long viewCount,
                                      Long likeCount,
                                      Long commentCount,
                                      Long shareCount) {
        log.debug("Hot score update request contentId={} view={} like={} comment={} share={}",
                contentId, viewCount, likeCount, commentCount, shareCount);
    }

    private List<RankedContent> buildTagCandidates(Long userId, int limit) {
        return buildTagCandidates(userId, limit, null);
    }

    private List<RankedContent> buildTagCandidates(Long userId, int limit, Long channelId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        Set<String> interestTags = new LinkedHashSet<>();
        for (ContentFavoriteRecordDO record : contentFavoriteRecordMapper.selectByUser(userId)) {
            if (CollUtil.isNotEmpty(record.getTags())) {
                interestTags.addAll(record.getTags());
            }
        }
        if (interestTags.isEmpty()) {
            List<ContentInteractionDO> likes = contentInteractionMapper.selectRecent(userId, mapBehaviorType(LIKE), 30);
            for (ContentInteractionDO interaction : likes) {
                ContentDO content = contentMapper.selectById(interaction.getContentId());
                if (content != null && CollUtil.isNotEmpty(content.getTags())) {
                    interestTags.addAll(content.getTags());
                }
            }
        }
        if (interestTags.isEmpty()) {
            return Collections.emptyList();
        }
        List<ContentDO> pool = channelId != null
                ? contentMapper.selectLatestPublishedByChannelId(channelId, RECENT_CONTENT_POOL_SIZE)
                : contentMapper.selectRecentPublic(RECENT_CONTENT_POOL_SIZE);
        List<RankedContent> result = new ArrayList<>();
        for (ContentDO content : pool) {
            if (CollUtil.isEmpty(content.getTags())) {
                continue;
            }
            long matchCount = content.getTags().stream().filter(interestTags::contains).count();
            if (matchCount == 0) {
                continue;
            }
            double score = TAG_WEIGHT + 0.08 * matchCount + normalizeHot(content) + recencyBoost(content);
            result.add(createRanked(content.getId(), score, "tag_match"));
        }
        return limit(result, limit);
    }

    private List<RankedContent> buildFollowCandidates(Long userId, int limit) {
        return buildFollowCandidates(userId, limit, null);
    }

    private List<RankedContent> buildFollowCandidates(Long userId, int limit, Long channelId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        List<Long> followingUserIds = followService.getFollowingUserIds(userId);
        if (CollUtil.isEmpty(followingUserIds)) {
            return Collections.emptyList();
        }
        List<ContentDO> list = contentMapper.selectLatestByUserIds(followingUserIds, 3);
        if (channelId != null) {
            list = list.stream()
                    .filter(content -> Objects.equals(channelId, content.getChannelId()))
                    .collect(Collectors.toList());
        }
        List<RankedContent> result = new ArrayList<>();
        for (ContentDO content : list) {
            double score = FOLLOW_WEIGHT + recencyBoost(content) + normalizeHot(content);
            result.add(createRanked(content.getId(), score, "follow"));
        }
        return limit(result, limit);
    }

    private List<RankedContent> buildHotCandidates(int limit) {
        return buildHotCandidates(limit, null);
    }

    private List<RankedContent> buildHotCandidates(int limit, Long channelId) {
        List<ContentDO> pool = channelId != null
                ? contentMapper.selectLatestPublishedByChannelId(channelId, RECENT_CONTENT_POOL_SIZE)
                : contentMapper.selectRecentPublic(RECENT_CONTENT_POOL_SIZE);
        return limit(pool.stream()
                .sorted(Comparator.comparing(ContentDO::getHotScore, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(content -> createRanked(content.getId(),
                        HOT_WEIGHT + normalizeHot(content) + recencyBoost(content),
                        "hot"))
                .collect(Collectors.toList()), limit);
    }

    private List<RankedContent> buildExploreCandidates(int limit) {
        return buildExploreCandidates(limit, null);
    }

    private List<RankedContent> buildExploreCandidates(int limit, Long channelId) {
        List<ContentDO> pool = channelId != null
                ? contentMapper.selectLatestPublishedByChannelId(channelId, RECENT_CONTENT_POOL_SIZE / 2)
                : contentMapper.selectRecentPublic(RECENT_CONTENT_POOL_SIZE / 2);
        Collections.shuffle(pool, ThreadLocalRandom.current());
        List<RankedContent> result = new ArrayList<>();
        for (ContentDO content : pool) {
            double score = EXPLORE_WEIGHT + 0.05 * ThreadLocalRandom.current().nextDouble();
            result.add(createRanked(content.getId(), score, "explore"));
        }
        return limit(result, limit);
    }

    private void appendCandidates(Map<Long, RankedContent> ranking, List<RankedContent> candidates) {
        for (RankedContent candidate : candidates) {
            if (candidate == null || candidate.getContentId() == null) {
                continue;
            }
            ranking.merge(candidate.getContentId(), candidate, (oldValue, newValue) -> {
                if (newValue.getScore() > oldValue.getScore()) {
                    return newValue;
                }
                if (!StrUtil.contains(oldValue.getStrategy(), newValue.getStrategy())) {
                    oldValue.setStrategy(oldValue.getStrategy() + "+" + newValue.getStrategy());
                }
                oldValue.setScore(Math.max(oldValue.getScore(), newValue.getScore()));
                return oldValue;
            });
        }
    }

    private List<RankedContent> limit(List<RankedContent> source, int limit) {
        if (limit <= 0 || source.size() <= limit) {
            return source;
        }
        return source.subList(0, limit);
    }

    private double normalizeHot(ContentDO content) {
        if (content == null || content.getHotScore() == null) {
            return 0;
        }
        return Math.min(content.getHotScore() / 1000.0, 0.5);
    }

    private double recencyBoost(ContentDO content) {
        LocalDateTime publishTime = content.getPublishTime();
        if (publishTime == null) {
            return 0;
        }
        long hours = Math.max(Duration.between(publishTime, LocalDateTime.now()).toHours(), 1);
        return 0.3 / hours;
    }

    private RankedContent createRanked(Long contentId, double score, String strategy) {
        RankedContent rankedContent = new RankedContent();
        rankedContent.setContentId(contentId);
        rankedContent.setScore(score);
        rankedContent.setStrategy(strategy);
        return rankedContent;
    }

    private String buildStrategySummary(List<RankedContent> rankedContents) {
        if (CollUtil.isEmpty(rankedContents)) {
            return "empty";
        }
        Map<String, Long> summary = rankedContents.stream()
                .collect(Collectors.groupingBy(RankedContent::getStrategy, LinkedHashMap::new, Collectors.counting()));
        return summary.entrySet().stream()
                .map(entry -> entry.getKey() + ":" + entry.getValue())
                .collect(Collectors.joining("|"));
    }

    private int mapBehaviorType(String behavior) {
        return switch (behavior) {
            case VIEW -> 1;
            case LIKE -> 2;
            case "collect" -> 3;
            default -> 0;
        };
    }

    public static class RecommendResult {
        private List<RankedContent> rankedContents = Collections.emptyList();
        private Long totalCount = 0L;
        private String recommendStrategy = "unknown";

        public List<Long> getContentIds() {
            return rankedContents.stream()
                    .map(RankedContent::getContentId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }

        public List<RankedContent> getRankedContents() {
            return rankedContents;
        }

        public void setRankedContents(List<RankedContent> rankedContents) {
            this.rankedContents = rankedContents == null ? Collections.emptyList() : rankedContents;
        }

        public Long getTotalCount() {
            return totalCount;
        }

        public void setTotalCount(Long totalCount) {
            this.totalCount = totalCount == null ? 0L : totalCount;
        }

        public String getRecommendStrategy() {
            return recommendStrategy;
        }

        public void setRecommendStrategy(String recommendStrategy) {
            this.recommendStrategy = recommendStrategy;
        }
    }

    public static class RankedContent {
        private Long contentId;
        private double score;
        private String strategy;

        public Long getContentId() {
            return contentId;
        }

        public void setContentId(Long contentId) {
            this.contentId = contentId;
        }

        public double getScore() {
            return score;
        }

        public void setScore(double score) {
            this.score = score;
        }

        public String getStrategy() {
            return strategy;
        }

        public void setStrategy(String strategy) {
            this.strategy = strategy;
        }
    }
}
