package cn.iocoder.yudao.module.content.service.channel;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.content.dal.dataobject.ChannelVisitStatsDO;
import cn.iocoder.yudao.module.content.dal.dataobject.ContentChannelDO;
import cn.iocoder.yudao.module.content.dal.dataobject.ContentChannelUserDO;
import cn.iocoder.yudao.module.content.dal.dataobject.ContentDO;
import cn.iocoder.yudao.module.content.dal.mysql.ContentChannelMapper;
import cn.iocoder.yudao.module.content.dal.mysql.ContentChannelUserMapper;
import cn.iocoder.yudao.module.content.dal.mysql.ContentInteractionMapper;
import cn.iocoder.yudao.module.content.dal.mysql.ContentMapper;
import cn.iocoder.yudao.module.content.service.channel.bo.ChannelVisitInsight;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ContentChannelServiceImpl implements ContentChannelService {

    private static final int INTERACTION_VIEW = 1;

    @Resource
    private ContentChannelMapper channelMapper;
    @Resource
    private ContentChannelUserMapper channelUserMapper;
    @Resource
    private ContentInteractionMapper contentInteractionMapper;
    @Resource
    private ContentMapper contentMapper;

    @Override
    public List<ContentChannelDO> getUserChannels(Long userId) {
        if (userId == null) {
            List<ContentChannelDO> defaults = channelMapper.selectDefaultChannels();
            return defaults.isEmpty() ? channelMapper.selectEnabledChannels() : defaults;
        }
        List<ContentChannelUserDO> userSettings = channelUserMapper.selectListByUserId(userId);
        if (CollUtil.isEmpty(userSettings)) {
            List<ContentChannelDO> defaults = channelMapper.selectDefaultChannels();
            if (!defaults.isEmpty()) {
                updateUserChannels(userId, defaults.stream().map(ContentChannelDO::getId).collect(Collectors.toList()));
                return defaults;
            }
            List<ContentChannelDO> enabled = channelMapper.selectEnabledChannels();
            List<Long> fallbackIds = enabled.stream().map(ContentChannelDO::getId).collect(Collectors.toList());
            updateUserChannels(userId, fallbackIds);
            return enabled;
        }
        Map<Long, ContentChannelDO> channelMap = CollectionUtils.convertMap(channelMapper.selectEnabledChannels(), ContentChannelDO::getId);
        List<ContentChannelDO> result = new ArrayList<>();
        for (ContentChannelUserDO preference : userSettings) {
            ContentChannelDO channel = channelMap.get(preference.getChannelId());
            if (channel != null) {
                result.add(channel);
            }
        }
        if (result.isEmpty()) {
            return channelMapper.selectDefaultChannels();
        }
        return result;
    }

    @Override
    public List<ContentChannelDO> getRecommendChannels(Long userId) {
        List<ContentChannelDO> all = channelMapper.selectEnabledChannels();
        if (userId == null) {
            return all;
        }
        List<Long> current = getUserChannels(userId).stream()
                .map(ContentChannelDO::getId)
                .collect(Collectors.toList());
        Set<Long> used = new LinkedHashSet<>(current);
        return all.stream()
                .filter(channel -> !used.contains(channel.getId()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserChannels(Long userId, List<Long> channelIds) {
        if (userId == null) {
            return;
        }
        List<ContentChannelDO> enabledChannels = channelMapper.selectEnabledChannels();
        if (enabledChannels.isEmpty()) {
            return;
        }
        Map<Long, ContentChannelDO> channelMap = CollectionUtils.convertMap(enabledChannels, ContentChannelDO::getId);
        List<Long> sanitized = sanitizeChannelIds(channelIds, channelMap.values());
        if (sanitized.isEmpty()) {
            sanitized = channelMapper.selectDefaultChannels().stream()
                    .map(ContentChannelDO::getId)
                    .collect(Collectors.toList());
        }
        List<ContentChannelUserDO> existing = channelUserMapper.selectListByUserId(userId);
        Map<Long, ContentChannelUserDO> existingMap = CollectionUtils.convertMap(existing, ContentChannelUserDO::getChannelId);
        int order = 0;
        for (Long channelId : sanitized) {
            ContentChannelUserDO record = existingMap.getOrDefault(channelId, new ContentChannelUserDO());
            boolean isNew = record.getId() == null;
            record.setUserId(userId);
            record.setChannelId(channelId);
            record.setDisplayOrder(order++);
            ContentChannelDO channel = channelMap.get(channelId);
            record.setPinned(channel != null && channel.getIsRequired() != null && channel.getIsRequired() == 1 ? 1 : 0);
            if (isNew) {
                channelUserMapper.insert(record);
            } else {
                channelUserMapper.updateById(record);
            }
        }
        channelUserMapper.deleteByUserIdExcluding(userId, sanitized);
    }

    private List<Long> sanitizeChannelIds(Collection<Long> channelIds, Collection<ContentChannelDO> enabledChannels) {
        Map<Long, ContentChannelDO> enabledMap = CollectionUtils.convertMap(enabledChannels, ContentChannelDO::getId);
        List<Long> cleaned = new ArrayList<>();
        if (channelIds != null) {
            for (Long id : channelIds) {
                if (id == null || !enabledMap.containsKey(id)) {
                    continue;
                }
                if (!cleaned.contains(id)) {
                    cleaned.add(id);
                }
            }
        }
        List<ContentChannelDO> required = enabledChannels.stream()
                .filter(channel -> channel.getIsRequired() != null && channel.getIsRequired() == 1)
                .sorted(Comparator.comparing(ContentChannelDO::getSort, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(ContentChannelDO::getId))
                .collect(Collectors.toList());
        int index = 0;
        for (ContentChannelDO channel : required) {
            if (!cleaned.contains(channel.getId())) {
                cleaned.add(index, channel.getId());
                index++;
            }
        }
        return cleaned;
    }

    @Override
    public void addUserChannel(Long userId, Long channelId) {
        if (userId == null || channelId == null) {
            return;
        }
        List<ContentChannelDO> enabled = channelMapper.selectEnabledChannels();
        Map<Long, ContentChannelDO> enabledMap = CollectionUtils.convertMap(enabled, ContentChannelDO::getId);
        if (!enabledMap.containsKey(channelId)) {
            return;
        }
        List<Long> current = getUserChannels(userId).stream().map(ContentChannelDO::getId).collect(Collectors.toList());
        if (!current.contains(channelId)) {
            current.add(channelId);
            updateUserChannels(userId, current);
        }
    }

    @Override
    public void removeUserChannel(Long userId, Long channelId) {
        if (userId == null || channelId == null) {
            return;
        }
        ContentChannelDO channel = channelMapper.selectById(channelId);
        if (channel != null && channel.getIsRequired() != null && channel.getIsRequired() == 1) {
            return;
        }
        List<Long> current = getUserChannels(userId).stream().map(ContentChannelDO::getId).collect(Collectors.toList());
        if (current.remove(channelId)) {
            updateUserChannels(userId, current);
        }
    }

    @Override
    public ContentChannelDO matchChannel(String title, String content, List<String> tags) {
        List<ContentChannelDO> channels = channelMapper.selectEnabledChannels();
        if (channels.isEmpty()) {
            return null;
        }
        String keywords = buildSearchText(title, content, tags);
        int bestScore = -1;
        ContentChannelDO best = null;
        for (ContentChannelDO channel : channels) {
            List<String> hints = channel.getKeywordHints();
            if (CollUtil.isEmpty(hints)) {
                continue;
            }
            int score = calculateScore(keywords, hints);
            if (score > bestScore) {
                bestScore = score;
                best = channel;
            }
        }
        if (best == null) {
            best = channels.stream()
                    .filter(c -> c.getIsDefault() != null && c.getIsDefault() == 1)
                    .findFirst()
                    .orElse(channels.get(0));
        }
        return best;
    }

    private String buildSearchText(String title, String content, List<String> tags) {
        StringBuilder builder = new StringBuilder();
        if (StrUtil.isNotBlank(title)) {
            builder.append(title).append(' ');
        }
        if (StrUtil.isNotBlank(content)) {
            builder.append(content).append(' ');
        }
        if (CollUtil.isNotEmpty(tags)) {
            for (String tag : tags) {
                if (StrUtil.isNotBlank(tag)) {
                    builder.append(tag).append(' ');
                }
            }
        }
        return builder.toString().toLowerCase(Locale.ROOT);
    }

    private int calculateScore(String text, List<String> hints) {
        int score = 0;
        for (String hint : hints) {
            if (StrUtil.isBlank(hint)) {
                continue;
            }
            if (text.contains(hint.toLowerCase(Locale.ROOT))) {
                score++;
            }
        }
        return score;
    }

    @Override
    public ContentChannelDO getChannel(Long channelId) {
        if (channelId == null) {
            return null;
        }
        return channelMapper.selectById(channelId);
    }

    @Override
    public ContentChannelDO getChannelByCode(String code) {
        if (StrUtil.isBlank(code)) {
            return null;
        }
        List<ContentChannelDO> enabled = channelMapper.selectEnabledChannels();
        for (ContentChannelDO channel : enabled) {
            if (channel != null && StrUtil.equalsIgnoreCase(code, channel.getCode())) {
                return channel;
            }
        }
        return null;
    }

    @Override
    public List<ChannelVisitInsight> getFrequentChannelInsights(Long userId, Integer limit, Integer previewSize) {
        if (userId == null) {
            return Collections.emptyList();
        }
        int fetchLimit = limit == null ? 4 : Math.max(1, limit);
        int previewCount = previewSize == null ? 3 : Math.max(1, previewSize);
        List<ChannelVisitStatsDO> stats = contentInteractionMapper
                .selectChannelVisitStats(userId, INTERACTION_VIEW, fetchLimit);
        Map<Long, ContentChannelDO> channelMap =
                CollectionUtils.convertMap(channelMapper.selectEnabledChannels(), ContentChannelDO::getId);
        List<ChannelVisitInsight> result = new ArrayList<>();
        if (CollUtil.isNotEmpty(stats)) {
            for (ChannelVisitStatsDO stat : stats) {
                ContentChannelDO channel = channelMap.get(stat.getChannelId());
                if (channel == null) {
                    continue;
                }
                result.add(buildInsight(channel, stat.getVisitCount(), stat.getLastVisitTime(), previewCount));
            }
        }
        if (result.isEmpty()) {
            List<ContentChannelDO> fallback = getUserChannels(userId);
            for (int i = 0; i < Math.min(fetchLimit, fallback.size()); i++) {
                ContentChannelDO channel = fallback.get(i);
                result.add(buildInsight(channel, 0L, null, previewCount));
            }
        }
        return result;
    }

    private ChannelVisitInsight buildInsight(ContentChannelDO channel,
                                             Long visitCount,
                                             LocalDateTime lastVisitTime,
                                             int previewSize) {
        ChannelVisitInsight insight = new ChannelVisitInsight();
        insight.setChannelId(channel.getId());
        insight.setChannelName(channel.getName());
        insight.setChannelIcon(channel.getIcon());
        insight.setChannelColor(channel.getColor());
        insight.setChannelDescription(channel.getDescription());
        insight.setVisitCount(visitCount == null ? 0L : visitCount);
        insight.setLastVisitTime(lastVisitTime);
        List<ContentDO> contents = contentMapper.selectLatestPublishedByChannelId(channel.getId(), previewSize);
        List<ChannelVisitInsight.ContentPreview> previews = new ArrayList<>();
        for (ContentDO content : contents) {
            ChannelVisitInsight.ContentPreview preview = new ChannelVisitInsight.ContentPreview();
            preview.setContentId(content.getId());
            preview.setTitle(content.getTitle());
            preview.setCoverImage(resolveCover(content));
            preview.setVideoUrl(content.getVideoUrl());
            preview.setLikeCount(content.getLikeCount());
            preview.setViewCount(content.getViewCount());
            preview.setPublishTime(content.getPublishTime());
            previews.add(preview);
        }
        insight.setPreviews(previews);
        return insight;
    }

    private String resolveCover(ContentDO content) {
        if (StrUtil.isNotBlank(content.getCoverImage())) {
            return content.getCoverImage();
        }
        if (CollUtil.isNotEmpty(content.getImages())) {
            return content.getImages().get(0);
        }
        if (StrUtil.isNotBlank(content.getVideoCover())) {
            return content.getVideoCover();
        }
        return content.getVideoUrl();
    }
}
