package cn.iocoder.yudao.module.system.controller.app.notice.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "APP - 公告已读请求")
public class NoticeReadReqVO {

    @Schema(description = "公告ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "公告ID不能为空")
    private Long noticeId;

    public Long getNoticeId() {
        return noticeId;
    }

    public void setNoticeId(Long noticeId) {
        this.noticeId = noticeId;
    }
}
