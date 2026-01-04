package cn.iocoder.yudao.module.content.controller.app.ad.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "APP - 更新广告 Request")
public class AppAdUpdateReqVO extends AppAdCreateReqVO {

    @Schema(description = "广告ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "广告ID不能为空")
    private Long id;

    // 允许更新的附加字段（覆盖父类的可选字段即可）
    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "扩展样式信息")
    private Map<String, Object> styleMeta;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public LocalDateTime getStartTime() {
        return startTime;
    }

    @Override
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    @Override
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public Map<String, Object> getStyleMeta() {
        return styleMeta;
    }

    @Override
    public void setStyleMeta(Map<String, Object> styleMeta) {
        this.styleMeta = styleMeta;
    }
}
