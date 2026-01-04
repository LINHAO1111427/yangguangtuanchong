package cn.iocoder.yudao.module.content.controller.app.channel.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "APP - 频道看板响应")
public class ChannelBoardRespVO {

    @Schema(description = "我的频道列表")
    private List<ChannelSimpleRespVO> myChannels;

    @Schema(description = "推荐频道列表")
    private List<ChannelSimpleRespVO> recommendChannels;

    public List<ChannelSimpleRespVO> getMyChannels() {
        return myChannels;
    }

    public void setMyChannels(List<ChannelSimpleRespVO> myChannels) {
        this.myChannels = myChannels;
    }

    public List<ChannelSimpleRespVO> getRecommendChannels() {
        return recommendChannels;
    }

    public void setRecommendChannels(List<ChannelSimpleRespVO> recommendChannels) {
        this.recommendChannels = recommendChannels;
    }
}
