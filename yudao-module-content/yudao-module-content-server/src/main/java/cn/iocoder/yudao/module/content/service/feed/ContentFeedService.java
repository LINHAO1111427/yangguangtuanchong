package cn.iocoder.yudao.module.content.service.feed;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.content.controller.app.vo.ContentListRespVO;
import cn.iocoder.yudao.module.content.dal.dataobject.ContentAdDO;
import cn.iocoder.yudao.module.content.dal.dataobject.ContentChannelDO;
import cn.iocoder.yudao.module.content.dal.dataobject.ContentDO;
import cn.iocoder.yudao.module.content.dal.dataobject.ContentInteractionDO;
import cn.iocoder.yudao.module.content.dal.mysql.ContentInteractionMapper;
import cn.iocoder.yudao.module.content.service.ContentService;
import cn.iocoder.yudao.module.content.service.ad.ContentAdService;
import cn.iocoder.yudao.module.content.service.channel.ContentChannelService;
import cn.iocoder.yudao.module.content.service.favorite.ContentFavoriteService;
import cn.iocoder.yudao.module.content.service.feed.bo.FeedCardBO;
import cn.iocoder.yudao.module.content.service.feed.bo.FeedStreamRequestBO;
import cn.iocoder.yudao.module.content.service.feed.bo.FeedStreamResultBO;
import cn.iocoder.yudao.module.content.service.favorite.bo.FavoriteActionReqBO;
import cn.iocoder.yudao.module.content.service.recommend.ContentRecommendService;
import cn.iocoder.yudao.module.content.service.recommend.ContentRecommendService.RecommendResult;
import cn.iocoder.yudao.module.content.service.recommend.ContentRecommendService.RankedContent;
import cn.iocoder.yudao.module.pay.api.reward.PayRewardApi;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static cn.iocoder.yudao.module.content.constants.ContentConstants.BehaviorType.VIEW;

/**
 * 内容 Feed 服务，拼装推荐内容、广告、互动信息。
 */
@Service
public class ContentFeedService {

    private static final Logger log = LoggerFactory.getLogger(ContentFeedService.class);
    private static final int DEFAULT_HISTORY_LIMIT = 30;

    @Resource
    private ContentService contentService;
    @Resource
    private ContentRecommendService contentRecommendService;
    @Resource
    private ContentAdService contentAdService;
    @Resource
    private ContentFavoriteService contentFavoriteService;
    @Resource
    private ContentChannelService contentChannelService;
    @Resource
    private PayRewardApi payRewardApi;
    @Resource
    private ContentInteractionMapper contentInteractionMapper;

    private final Map<Long, RewardSnapshot> rewardCache = new ConcurrentHashMap<>();

    public FeedStreamResultBO getHomeFeed(Long userId, FeedStreamRequestBO requestBO) {
        String scene = StrUtil.blankToDefault(requestBO.getScene(), "home");
        boolean videoOnly = "video".equalsIgnoreCase(scene);

        Long channelId = resolveChannelId(scene);
        int pageSize = Math.max(1, requestBO.getPageSize());
        int fetchSize = videoOnly ? Math.min(50, Math.max(pageSize, pageSize * 5)) : pageSize;

        RecommendResult recommend = contentRecommendService.getRecommendFeed(userId,
                requestBO.getPageNo(), fetchSize, channelId);

        List<Long> orderedContentIds = recommend.getContentIds();
        List<ContentListRespVO> contentList = contentService.getContentListByIds(orderedContentIds, userId);
        Map<Long, ContentListRespVO> contentMapped = CollectionUtils.convertMap(contentList, ContentListRespVO::getId);

        List<FeedCardBO> cards = new ArrayList<>();
        for (RankedContent ranked : recommend.getRankedContents()) {
            ContentListRespVO content = contentMapped.get(ranked.getContentId());
            if (content == null) {
                continue;
            }
            if (videoOnly && !isVideoContent(content)) {
                continue;
            }
            FeedCardBO card = new FeedCardBO();
            card.setCardType(FeedCardBO.CardType.CONTENT);
            card.setContent(content);
            card.setLayout(resolveLayout(content));
            card.setStrategy(ranked.getStrategy());
            card.setScore(ranked.getScore());
            card.setRewardAmount(loadRewardAmount(content.getId()));
            cards.add(card);
            if (videoOnly && cards.size() >= pageSize) {
                break;
            }
        }

        if (requestBO.isIncludeAds()) {
            injectAds(cards, userId, requestBO);
        }

        FeedStreamResultBO result = new FeedStreamResultBO();
        result.setCards(cards);
        if (videoOnly) {
            // videoOnly 是二次过滤后的结果，总数以本次返回为准（避免前端分页计算出错）
            result.setTotal((long) cards.size());
        } else {
            result.setTotal(recommend.getTotalCount() != null ? recommend.getTotalCount() : cards.size());
        }
        result.setStrategySummary(recommend.getRecommendStrategy());
        return result;
    }

    private Long resolveChannelId(String scene) {
        if (StrUtil.isBlank(scene)
                || "home".equalsIgnoreCase(scene)
                || "recommend".equalsIgnoreCase(scene)
                || "video".equalsIgnoreCase(scene)) {
            return null;
        }
        ContentChannelDO channel = contentChannelService.getChannelByCode(scene);
        return channel != null ? channel.getId() : null;
    }

    private boolean isVideoContent(ContentListRespVO content) {
        if (content == null) {
            return false;
        }
        return Objects.equals(content.getContentType(), 2) || StrUtil.isNotBlank(content.getVideoUrl());
    }

    public List<ContentListRespVO> getBrowseHistory(Long userId, int size) {
        List<ContentInteractionDO> interactions =
                contentInteractionMapper.selectRecent(userId, mapBehaviorType(VIEW), Math.max(size, DEFAULT_HISTORY_LIMIT));
        if (CollUtil.isEmpty(interactions)) {
            return List.of();
        }
        List<Long> contentIds = new ArrayList<>(interactions.size());
        for (ContentInteractionDO interaction : interactions) {
        if (interaction.getDeleted() != null && interaction.getDeleted() != 0) {
            continue;
        }
            contentIds.add(interaction.getContentId());
        }
        return contentService.getContentListByIds(contentIds, userId);
    }

    public boolean quickCollect(Long userId, Long contentId, Long groupId) {
        FavoriteActionReqBO reqBO = new FavoriteActionReqBO();
        reqBO.setContentId(contentId);
        reqBO.setGroupId(groupId);
        reqBO.setSource(1);
        boolean collected = contentFavoriteService.toggleFavorite(userId, reqBO);
        // 触发推荐算法记录
        contentRecommendService.recordUserBehavior(userId, contentId, collected ? "collect" : "collect_cancel");
        return collected;
    }

    private void injectAds(List<FeedCardBO> cards, Long userId, FeedStreamRequestBO requestBO) {
        List<ContentAdDO> ads = contentAdService.pickAds(userId,
                sceneToInt(requestBO.getScene()), Math.max(1, requestBO.getPageSize() / requestBO.getAdInterval()));
        if (CollUtil.isEmpty(ads)) {
            return;
        }
        int interval = Math.max(3, requestBO.getAdInterval());
        int insertIndex = interval;
        int adIndex = 0;
        while (insertIndex < cards.size() && adIndex < ads.size()) {
            ContentAdDO ad = ads.get(adIndex++);
            FeedCardBO adCard = new FeedCardBO();
            adCard.setCardType(FeedCardBO.CardType.AD);
            adCard.setAd(ad);
            adCard.setLayout(resolveAdLayout(ad));
            adCard.setStrategy("ad_slot");
            cards.add(insertIndex, adCard);
            insertIndex += interval + ThreadLocalDelta.randomDelta();
        }
        while (adIndex < ads.size()) {
            ContentAdDO ad = ads.get(adIndex++);
            FeedCardBO adCard = new FeedCardBO();
            adCard.setCardType(FeedCardBO.CardType.AD);
            adCard.setAd(ad);
            adCard.setLayout(resolveAdLayout(ad));
            adCard.setStrategy("ad_slot");
            cards.add(adCard);
        }
    }

    private Integer loadRewardAmount(Long contentId) {
        if (contentId == null) {
            return null;
        }
        RewardSnapshot snapshot = rewardCache.get(contentId);
        long now = System.currentTimeMillis();
        if (snapshot != null && snapshot.expireAt > now) {
            return snapshot.amount;
        }
        try {
            CommonResult<Integer> result = payRewardApi.getTotalRewardAmountByPost(contentId);
            Integer amount = (result != null && result.getData() != null) ? result.getData() : 0;
            rewardCache.put(contentId, new RewardSnapshot(amount, now + 5 * 60 * 1000));
            return amount;
        } catch (Exception ex) {
            log.warn("Load reward amount failed for contentId={}", contentId, ex);
            return snapshot != null ? snapshot.amount : null;
        }
    }

    public RewardSummary getRewardSummary(Long contentId, Long authorId) {
        Integer postAmount = loadRewardAmount(contentId);
        Long targetAuthor = authorId;
        if (targetAuthor == null && contentId != null) {
            ContentDO content = contentService.getContent(contentId);
            if (content != null) {
                targetAuthor = content.getUserId();
            }
        }
        Integer authorAmount = null;
        if (targetAuthor != null) {
            try {
                CommonResult<Integer> result = payRewardApi.getTotalIncomeAmount(targetAuthor);
                authorAmount = result != null ? result.getData() : null;
            } catch (Exception ex) {
                log.warn("Load author reward failed authorId={}", targetAuthor, ex);
            }
        }
        return new RewardSummary(postAmount, authorAmount);
    }

    private String resolveLayout(ContentListRespVO content) {
        if (content == null) {
            return "unknown";
        }
        if (StrUtil.isNotBlank(content.getVideoUrl())) {
            if (content.getVideoWidth() != null && content.getVideoHeight() != null
                    && content.getVideoHeight() > content.getVideoWidth()) {
                return "vertical-video";
            }
            return "horizontal-video";
        }
        if (content.getImages() != null) {
            if (content.getImages().size() >= 3) {
                return "grid-gallery";
            }
            if (content.getImages().size() == 2) {
                return "double-card";
            }
            if (content.getImages().size() == 1) {
                return "full-cover";
            }
        }
        return "text-card";
    }

    private String resolveAdLayout(ContentAdDO ad) {
        if (ad == null) {
            return "ad";
        }
        if (StrUtil.equalsIgnoreCase(ad.getCardType(), "double_column")) {
            return "double-ad";
        }
        if (StrUtil.equalsIgnoreCase(ad.getMediaType(), "video")) {
            return "video-ad";
        }
        return "single-ad";
    }

    private int sceneToInt(String scene) {
        if ("topic".equalsIgnoreCase(scene)) {
            return 2;
        }
        if ("search".equalsIgnoreCase(scene)) {
            return 3;
        }
        return 1;
    }

    private int mapBehaviorType(String behavior) {
        return switch (behavior) {
            case VIEW -> 1;
            case "like" -> 2;
            case "collect" -> 3;
            case "share" -> 4;
            default -> 0;
        };
    }

    private static final class RewardSnapshot {
        private final Integer amount;
        private final long expireAt;

        private RewardSnapshot(Integer amount, long expireAt) {
            this.amount = amount;
            this.expireAt = expireAt;
        }
    }

    public static final class RewardSummary {
        private final Integer postAmount;
        private final Integer authorAmount;

        public RewardSummary(Integer postAmount, Integer authorAmount) {
            this.postAmount = postAmount;
            this.authorAmount = authorAmount;
        }

        public Integer getPostAmount() {
            return postAmount;
        }

        public Integer getAuthorAmount() {
            return authorAmount;
        }
    }

    private static final class ThreadLocalDelta {
        private static final java.util.Random RANDOM = new java.util.Random();

        private static int randomDelta() {
            return RANDOM.nextInt(2);
        }
    }
}
