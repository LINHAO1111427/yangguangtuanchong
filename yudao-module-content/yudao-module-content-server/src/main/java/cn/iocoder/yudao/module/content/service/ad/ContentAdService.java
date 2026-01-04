package cn.iocoder.yudao.module.content.service.ad;

import cn.hutool.core.collection.CollUtil;
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
import java.util.concurrent.ThreadLocalRandom;

/**
 * 内容流广告服务，负责广告的读取与频控。
 */
@Service
public class ContentAdService {

    private static final Logger log = LoggerFactory.getLogger(ContentAdService.class);

    private final Map<String, Integer> exposureCounter = new ConcurrentHashMap<>();

    @Resource
    private ContentAdMapper contentAdMapper;

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

    /**
     * 为用户挑选广告，自动处理频控。
     *
     * @param userId   用户
     * @param scene    场景
     * @param maxCount 最大返回条数
     */
    public List<ContentAdDO> pickAds(Long userId, Integer scene, int maxCount) {
        List<ContentAdDO> ads = getActiveAds(scene);
        if (CollUtil.isEmpty(ads) || maxCount <= 0) {
            return List.of();
        }
        List<ContentAdDO> result = new ArrayList<>();
        int randomSeed = ThreadLocalRandom.current().nextInt(100);
        for (ContentAdDO ad : ads) {
            if (!canExpose(userId, ad, randomSeed)) {
                continue;
            }
            recordExposure(userId, ad);
            result.add(ad);
            if (result.size() >= maxCount) {
                break;
            }
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

    private void recordExposure(Long userId, ContentAdDO ad) {
        if (userId == null || ad == null) {
            return;
        }
        String key = buildExposureKey(userId, ad.getId());
        exposureCounter.merge(key, 1, Integer::sum);
    }

    private String buildExposureKey(Long userId, Long adId) {
        return LocalDate.now() + ":" + userId + ":" + adId;
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
}
