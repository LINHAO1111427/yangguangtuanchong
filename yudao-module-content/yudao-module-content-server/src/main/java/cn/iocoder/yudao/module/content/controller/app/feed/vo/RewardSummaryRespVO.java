package cn.iocoder.yudao.module.content.controller.app.feed.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "APP - 内容打赏汇总响应")
public class RewardSummaryRespVO {

    @Schema(description = "作品总打赏金额（分）")
    private Integer postRewardAmount;

    @Schema(description = "作者总收益金额（分）")
    private Integer authorIncomeAmount;

    public Integer getPostRewardAmount() {
        return postRewardAmount;
    }

    public void setPostRewardAmount(Integer postRewardAmount) {
        this.postRewardAmount = postRewardAmount;
    }

    public Integer getAuthorIncomeAmount() {
        return authorIncomeAmount;
    }

    public void setAuthorIncomeAmount(Integer authorIncomeAmount) {
        this.authorIncomeAmount = authorIncomeAmount;
    }
}
