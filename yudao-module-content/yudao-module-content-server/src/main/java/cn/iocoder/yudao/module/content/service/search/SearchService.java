package cn.iocoder.yudao.module.content.service.search;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.content.controller.app.vo.ContentListRespVO;
import cn.iocoder.yudao.module.content.controller.app.vo.ContentPageReqVO;
import cn.iocoder.yudao.module.content.controller.app.vo.SearchAdRespVO;
import cn.iocoder.yudao.module.content.controller.app.vo.SearchAllRespVO;
import cn.iocoder.yudao.module.content.controller.app.vo.TopicListRespVO;
import cn.iocoder.yudao.module.content.dal.dataobject.ContentAdDO;
import cn.iocoder.yudao.module.content.service.ContentService;
import cn.iocoder.yudao.module.content.service.TopicService;
import cn.iocoder.yudao.module.content.service.ad.ContentAdService;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.member.api.user.MemberUserSearchApi;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 内容搜索服务：负责内容/用户/话题/广告的统一检索，以及热词与历史记录。
 */
@Service
public class SearchService {

    private static final int MAX_HISTORY = 50;
    private static final int DEFAULT_LIMIT = 20;
    private static final String HOT_KEY = "content:search:hot";
    private static final String HISTORY_KEY_PREFIX = "content:search:history:";

    @Resource
    private ContentService contentService;
    @Resource
    private TopicService topicService;
    @Resource
    private ContentAdService contentAdService;
    @Resource
    private MemberUserApi memberUserApi;
    @Resource
    private MemberUserSearchApi memberUserSearchApi;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private final ConcurrentMap<Long, Deque<String>> userHistory = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, AtomicInteger> keywordCounter = new ConcurrentHashMap<>();

    public PageResult<ContentListRespVO> searchContents(String keyword, Integer page, Integer size, Long currentUserId) {
        recordKeyword(keyword, currentUserId);
        return doSearchContents(keyword, page, size, currentUserId);
    }

    public SearchAllRespVO searchAll(String keyword, String type, Integer page, Integer size, Long currentUserId) {
        recordKeyword(keyword, currentUserId);
        String normalizedType = StrUtil.blankToDefault(type, "all").toLowerCase();
        SearchAllRespVO resp = new SearchAllRespVO();
        resp.setKeyword(keyword);
        resp.setType(normalizedType);
        boolean fetchAll = "all".equals(normalizedType);
        if (fetchAll || "content".equals(normalizedType)) {
            resp.setContents(doSearchContents(keyword, page, size, currentUserId));
        }
        if (fetchAll || "user".equals(normalizedType)) {
            resp.setUsers(searchUsers(keyword));
        }
        if (fetchAll || "topic".equals(normalizedType)) {
            int limit = ObjectUtil.defaultIfNull(size, DEFAULT_LIMIT);
            resp.setTopics(searchTopics(keyword, limit));
        }
        if (fetchAll || "ad".equals(normalizedType)) {
            resp.setAds(mapAds(contentAdService.pickAds(currentUserId, 3, 5)));
        }
        return resp;
    }

    public List<SearchUserRespVO> searchUsers(String keyword) {
        if (StrUtil.isBlank(keyword)) {
            return Collections.emptyList();
        }
        List<MemberUserRespDTO> users = memberUserSearchApi.searchUsers(keyword, DEFAULT_LIMIT).getCheckedData();
        if (CollUtil.isEmpty(users)) {
            return Collections.emptyList();
        }
        return users.stream().map(u -> {
            SearchUserRespVO vo = new SearchUserRespVO();
            vo.setId(u.getId());
            vo.setNickname(u.getNickname());
            vo.setAvatar(u.getAvatar());
            return vo;
        }).collect(Collectors.toList());
    }

    public List<TopicListRespVO> searchTopics(String keyword, Integer limit) {
        if (StrUtil.isBlank(keyword)) {
            return Collections.emptyList();
        }
        int searchLimit = ObjectUtil.defaultIfNull(limit, DEFAULT_LIMIT);
        return topicService.searchTopics(keyword, searchLimit);
    }

    public List<String> getHotKeywords(int topN) {
        int limit = Math.max(topN, 1);
        try {
            Set<String> hot = stringRedisTemplate.opsForZSet()
                    .reverseRange(HOT_KEY, 0, limit - 1);
            if (CollUtil.isNotEmpty(hot)) {
                return new ArrayList<>(hot);
            }
        } catch (Exception ignore) {
            // fallback to memory
        }
        if (keywordCounter.isEmpty()) {
            return Collections.emptyList();
        }
        return keywordCounter.entrySet().stream()
                .sorted(Map.Entry.<String, AtomicInteger>comparingByValue(
                        Comparator.comparingInt(AtomicInteger::get)).reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public List<String> getHistory(Long userId, int limit) {
        if (userId == null) {
            return Collections.emptyList();
        }
        int size = Math.max(limit, 1);
        try {
            String key = HISTORY_KEY_PREFIX + userId;
            List<String> list = stringRedisTemplate.opsForList().range(key, 0, size - 1);
            if (CollUtil.isNotEmpty(list)) {
                return list;
            }
        } catch (Exception ignore) {
        }
        Deque<String> deque = userHistory.getOrDefault(userId, new LinkedList<>());
        if (deque.isEmpty()) {
            return Collections.emptyList();
        }
        return deque.stream().limit(size).collect(Collectors.toList());
    }

    public boolean clearHistory(Long userId) {
        if (userId == null) {
            return true;
        }
        try {
            stringRedisTemplate.delete(HISTORY_KEY_PREFIX + userId);
        } catch (Exception ignore) {
        }
        userHistory.remove(userId);
        return true;
    }

    private PageResult<ContentListRespVO> doSearchContents(String keyword, Integer page, Integer size, Long currentUserId) {
        ContentPageReqVO pageReqVO = new ContentPageReqVO();
        pageReqVO.setKeyword(keyword);
        pageReqVO.setPageNo(page);
        pageReqVO.setPageSize(size);
        return contentService.searchContents(pageReqVO, currentUserId);
    }

    private void recordKeyword(String keyword, Long userId) {
        String normalized = StrUtil.trim(keyword);
        if (StrUtil.isBlank(normalized)) {
            return;
        }
        keywordCounter.computeIfAbsent(normalized, key -> new AtomicInteger(0)).incrementAndGet();
        try {
            stringRedisTemplate.opsForZSet().incrementScore(HOT_KEY, normalized, 1D);
        } catch (Exception ignore) {
        }
        if (userId == null) {
            return;
        }
        try {
            String key = HISTORY_KEY_PREFIX + userId;
            stringRedisTemplate.opsForList().remove(key, 0, normalized);
            stringRedisTemplate.opsForList().leftPush(key, normalized);
            stringRedisTemplate.opsForList().trim(key, 0, MAX_HISTORY - 1);
        } catch (Exception ignore) {
            Deque<String> deque = userHistory.computeIfAbsent(userId, key -> new LinkedList<>());
            synchronized (deque) {
                deque.remove(normalized);
                deque.addFirst(normalized);
                while (deque.size() > MAX_HISTORY) {
                    deque.removeLast();
                }
            }
        }
    }

    private List<SearchAdRespVO> mapAds(List<ContentAdDO> ads) {
        if (CollUtil.isEmpty(ads)) {
            return Collections.emptyList();
        }
        return ads.stream()
                .map(ad -> {
                    SearchAdRespVO vo = new SearchAdRespVO();
                    vo.setId(ad.getId());
                    vo.setTitle(ad.getTitle());
                    vo.setCoverImage(ad.getCoverImage());
                    vo.setJumpUrl(ad.getJumpUrl());
                    vo.setCardType(ad.getCardType());
                    vo.setMediaType(ad.getMediaType());
                    vo.setCallToAction(ad.getCallToAction());
                    return vo;
                })
                .collect(Collectors.toList());
    }

    public static class SearchUserRespVO {
        private Long id;
        private String nickname;
        private String avatar;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public String getAvatar() {
            return avatar;
        }

        public void setAvatar(String avatar) {
            this.avatar = avatar;
        }
    }
}
