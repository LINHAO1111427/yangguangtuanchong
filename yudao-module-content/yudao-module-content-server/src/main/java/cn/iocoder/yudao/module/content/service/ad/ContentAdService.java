package cn.iocoder.yudao.module.content.service.ad;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.content.controller.admin.ad.vo.ContentAdPageReqVO;
import cn.iocoder.yudao.module.content.dal.dataobject.ContentAdDO;
import cn.iocoder.yudao.module.content.dal.mysql.ContentAdMapper;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内容流广告服务，负责广告的读取与频控。
 */
@Service
public class ContentAdService {

    private static final Logger log = LoggerFactory.getLogger(ContentAdService.class);
    private static final int SCENE_CAP_MULTIPLIER = 10;
    private static final int MIN_SCENE_DAILY_CAP = 20;
    private static final int VIDEO_SCENE_DAILY_CAP = 12;
    private static final double VIDEO_MEDIA_BOOST = 1.5;
    private static final double FRESHNESS_BOOST = 1.2;
    private static final int FRESHNESS_HOURS = 24;

    private final Map<String, Integer> exposureCounter = new ConcurrentHashMap<>();
    private final Map<String, Integer> sceneExposureCounter = new ConcurrentHashMap<>();

    @Resource
    private ContentAdMapper contentAdMapper;
    @Resource
    private ContentAdEventService contentAdEventService;

    /**
     * 读取有效广告列表。
     *
     * @param scene 展示场景
     * @return 可用广告
     */
    public List<ContentAdDO> getActiveAds(Integer scene) {
        List<ContentAdDO> ads = contentAdMapper.selectActiveAds(scene, LocalDateTime.now());
        if (CollUtil.isEmpty(ads)) {
            return List.of();
        }
        ads.sort(Comparator.comparing(ContentAdDO::getPriority, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(ContentAdDO::getId));
        return ads;
    }

    public List<ContentAdDO> pickAds(Long userId, Integer scene, int maxCount) {
        long seed = System.currentTimeMillis() ^ (userId == null ? 0L : userId);
        return pickAds(userId, scene, maxCount, seed, false);
    }

    /**
     * 为用户挑选广告，自动处理频控。
     *
     * @param userId   用户
     * @param scene    场景
     * @param maxCount 最大返回条数
     */
    public List<ContentAdDO> pickAds(Long userId, Integer scene, int maxCount, long seed, boolean videoScene) {
        List<ContentAdDO> ads = getActiveAds(scene);
        if (CollUtil.isEmpty(ads) || maxCount <= 0) {
            return List.of();
        }
        int sceneCap = resolveSceneCap(scene, maxCount, videoScene);
        if (!canExposeScene(userId, scene, sceneCap)) {
            return List.of();
        }
        java.util.Random random = new java.util.Random(seed);
        List<AdCandidate> candidates = new ArrayList<>();
        for (ContentAdDO ad : ads) {
            if (!canExpose(userId, ad, random.nextInt(100))) {
                continue;
            }
            double weight = calcWeight(ad, videoScene);
            if (weight > 0) {
                candidates.add(new AdCandidate(ad, weight));
            }
        }
        if (candidates.isEmpty()) {
            return List.of();
        }
        List<ContentAdDO> result = new ArrayList<>();
        for (int i = 0; i < maxCount && !candidates.isEmpty(); i++) {
            ContentAdDO ad = pickWeighted(candidates, random);
            if (ad == null) {
                break;
            }
            recordExposure(userId, ad, scene);
            result.add(ad);
        }
        return result;
    }

    private boolean canExpose(Long userId, ContentAdDO ad, int randomSeed) {
        if (ad == null || Objects.equals(ad.getStatus(), 0)) {
            return false;
        }
        Integer freq = ad.getFrequencyCap();
        if (freq == null || freq <= 0 || userId == null) {
            return true;
        }
        String key = buildExposureKey(userId, ad.getId());
        int shown = exposureCounter.getOrDefault(key, 0);
        if (shown >= freq) {
            return false;
        }
        // 简单的随机衰减，让同一用户的广告更均衡
        return randomSeed % (shown + 1) == 0;
    }

    private void recordExposure(Long userId, ContentAdDO ad, Integer scene) {
        if (ad == null || ad.getId() == null) {
            return;
        }
        if (userId != null) {
            String key = buildExposureKey(userId, ad.getId());
            exposureCounter.merge(key, 1, Integer::sum);
            if (scene != null) {
                String sceneKey = buildSceneExposureKey(userId, scene);
                sceneExposureCounter.merge(sceneKey, 1, Integer::sum);
            }
        }
        contentAdEventService.recordImpression(ad.getId(), userId, scene);
    }

    private String buildExposureKey(Long userId, Long adId) {
        return LocalDate.now() + ":" + userId + ":" + adId;
    }

    private boolean canExposeScene(Long userId, Integer scene, int sceneCap) {
        if (userId == null || scene == null || sceneCap <= 0) {
            return true;
        }
        String key = buildSceneExposureKey(userId, scene);
        int shown = sceneExposureCounter.getOrDefault(key, 0);
        return shown < sceneCap;
    }

    private String buildSceneExposureKey(Long userId, Integer scene) {
        return LocalDate.now() + ":" + userId + ":scene:" + scene;
    }

    private int resolveSceneCap(Integer scene, int maxCount, boolean videoScene) {
        if (videoScene) {
            return VIDEO_SCENE_DAILY_CAP;
        }
        int base = Math.max(maxCount * SCENE_CAP_MULTIPLIER, MIN_SCENE_DAILY_CAP);
        return Math.max(base, maxCount);
    }

    private double calcWeight(ContentAdDO ad, boolean videoScene) {
        double weight = ad.getPriority() != null && ad.getPriority() > 0 ? ad.getPriority() : 1;
        if (videoScene && "video".equalsIgnoreCase(ad.getMediaType())) {
            weight *= VIDEO_MEDIA_BOOST;
        }
        if (ad.getStartTime() != null) {
            long hours = java.time.Duration.between(ad.getStartTime(), LocalDateTime.now()).toHours();
            if (hours >= 0 && hours <= FRESHNESS_HOURS) {
                weight *= FRESHNESS_BOOST;
            }
        }
        return weight;
    }

    private ContentAdDO pickWeighted(List<AdCandidate> candidates, java.util.Random random) {
        double total = 0;
        for (AdCandidate candidate : candidates) {
            total += candidate.weight;
        }
        if (total <= 0) {
            return null;
        }
        double hit = random.nextDouble() * total;
        double current = 0;
        for (int i = 0; i < candidates.size(); i++) {
            AdCandidate candidate = candidates.get(i);
            current += candidate.weight;
            if (hit <= current) {
                candidates.remove(i);
                return candidate.ad;
            }
        }
        return null;
    }

    private static final class AdCandidate {
        private final ContentAdDO ad;
        private final double weight;

        private AdCandidate(ContentAdDO ad, double weight) {
            this.ad = ad;
            this.weight = weight;
        }
    }

    public ContentAdDO getAd(Long id) {
        if (id == null) {
            return null;
        }
        return contentAdMapper.selectById(id);
    }

    public Long createAd(ContentAdDO ad) {
        if (ad == null) {
            return null;
        }
        contentAdMapper.insert(ad);
        return ad.getId();
    }

    public void updateAd(ContentAdDO ad) {
        if (ad == null || ad.getId() == null) {
            return;
        }
        contentAdMapper.updateById(ad);
    }

    public void deleteAd(Long id) {
        if (id == null) {
            return;
        }
        contentAdMapper.deleteById(id);
    }

    public PageResult<ContentAdDO> getAdPage(ContentAdPageReqVO reqVO) {
        return contentAdMapper.selectPage(reqVO);
    }

    public void updateAdStatus(Long id, Integer status) {
        if (id == null || status == null) {
            return;
        }
        ContentAdDO ad = contentAdMapper.selectById(id);
        if (ad == null) {
            return;
        }
        ad.setStatus(status);
        contentAdMapper.updateById(ad);
    }
}
