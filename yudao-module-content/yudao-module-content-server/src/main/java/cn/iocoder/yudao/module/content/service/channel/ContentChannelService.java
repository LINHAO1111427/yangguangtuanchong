package cn.iocoder.yudao.module.content.service.channel;

import cn.iocoder.yudao.module.content.dal.dataobject.ContentChannelDO;
import cn.iocoder.yudao.module.content.service.channel.bo.ChannelVisitInsight;

import java.util.List;

public interface ContentChannelService {

    /**
     * 获取用户配置的频道。如果用户尚未配置，则返回默认频道列表
     *
     * @param userId 用户ID（允许为 null，代表游客）
     * @return 频道列表
     */
    List<ContentChannelDO> getUserChannels(Long userId);

    /**
     * 获取推荐频道（当前用户未订阅的频道）
     *
     * @param userId 用户ID（允许为 null）
     * @return 推荐频道列表
     */
    List<ContentChannelDO> getRecommendChannels(Long userId);

    /**
     * 更新用户的频道顺序与集合
     *
     * @param userId     用户ID
     * @param channelIds 按顺序排列的频道ID集合
     */
    void updateUserChannels(Long userId, List<Long> channelIds);

    /**
     * 为用户新增频道（追加到末尾，自动去重/校验）
     */
    void addUserChannel(Long userId, Long channelId);

    /**
     * 为用户移除频道（必选频道不可移除）
     */
    void removeUserChannel(Long userId, Long channelId);

    /**
     * 根据内容标题、正文与标签匹配最适合的频道
     *
     * @param title   标题
     * @param content 正文
     * @param tags    标签
     * @return 匹配到的频道，可能为 null
     */
    ContentChannelDO matchChannel(String title, String content, List<String> tags);

    /**
     * 根据ID获取频道信息
     */
    ContentChannelDO getChannel(Long channelId);

    /**
     * 根据频道 code 获取频道信息（大小写不敏感）
     *
     * @param code 频道编码
     * @return 频道信息，可能为 null
     */
    ContentChannelDO getChannelByCode(String code);

    /**
     * 获取用户常访问的频道摘要
     *
     * @param userId      用户ID
     * @param limit       返回数量
     * @param previewSize 每个频道返回的内容预览数
     * @return 频道摘要列表
     */
    List<ChannelVisitInsight> getFrequentChannelInsights(Long userId, Integer limit, Integer previewSize);
}
