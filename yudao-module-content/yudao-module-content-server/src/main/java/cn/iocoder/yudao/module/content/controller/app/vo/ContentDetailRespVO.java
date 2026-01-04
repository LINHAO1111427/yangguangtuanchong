package cn.iocoder.yudao.module.content.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

/**
 * Detailed content response for the app client.
 */
@Schema(description = "APP content detail response VO")
public class ContentDetailRespVO extends ContentListRespVO {

    @Schema(description = "Full content body in rich text or Markdown")
    private String content;

    @Schema(description = "Location metadata (latitude/longitude/address)")
    private Map<String, Object> location;

    @Schema(description = "Extended metadata map")
    private Map<String, Object> extension;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Map<String, Object> getLocation() {
        return location;
    }

    public void setLocation(Map<String, Object> location) {
        this.location = location;
    }

    public Map<String, Object> getExtension() {
        return extension;
    }

    public void setExtension(Map<String, Object> extension) {
        this.extension = extension;
    }
}
