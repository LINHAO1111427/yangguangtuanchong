package cn.iocoder.yudao.module.content.controller.app.vo;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.content.service.search.SearchService.SearchUserRespVO;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Collections;
import java.util.List;

/**
 * 搜索结果聚合返回对象，支持内容/用户/话题/广告分 Tab。
 */
@Schema(description = "APP - 搜索结果聚合响应")
public class SearchAllRespVO {

    @Schema(description = "搜索关键词")
    private String keyword;

    @Schema(description = "请求的类型 all/content/user/topic/ad")
    private String type;

    @Schema(description = "内容列表")
    private PageResult<ContentListRespVO> contents = PageResult.empty();

    @Schema(description = "用户列表")
    private List<SearchUserRespVO> users = Collections.emptyList();

    @Schema(description = "话题列表")
    private List<TopicListRespVO> topics = Collections.emptyList();

    @Schema(description = "广告列表")
    private List<SearchAdRespVO> ads = Collections.emptyList();

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public PageResult<ContentListRespVO> getContents() {
        return contents;
    }

    public void setContents(PageResult<ContentListRespVO> contents) {
        this.contents = contents;
    }

    public List<SearchUserRespVO> getUsers() {
        return users;
    }

    public void setUsers(List<SearchUserRespVO> users) {
        this.users = users;
    }

    public List<TopicListRespVO> getTopics() {
        return topics;
    }

    public void setTopics(List<TopicListRespVO> topics) {
        this.topics = topics;
    }

    public List<SearchAdRespVO> getAds() {
        return ads;
    }

    public void setAds(List<SearchAdRespVO> ads) {
        this.ads = ads;
    }
}
