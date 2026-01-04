package cn.iocoder.yudao.module.content.controller.app.vo.boost;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Boost statistic response")
public class ContentBoostStatRespVO {

    private String metric;
    private Long value;

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }
}
