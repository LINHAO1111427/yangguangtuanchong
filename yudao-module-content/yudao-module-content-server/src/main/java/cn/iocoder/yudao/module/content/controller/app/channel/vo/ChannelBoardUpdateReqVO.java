package cn.iocoder.yudao.module.content.controller.app.channel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Schema(description = "APP - 更新我的频道请求")
public class ChannelBoardUpdateReqVO {

    @Schema(description = "频道ID列表，按照期望顺序排列", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "频道列表不能为空")
    private List<Long> channelIds;

    public List<Long> getChannelIds() {
        return channelIds;
    }

    public void setChannelIds(List<Long> channelIds) {
        this.channelIds = channelIds;
    }
}
