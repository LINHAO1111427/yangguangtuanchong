package cn.iocoder.yudao.module.content.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "APP 内容模块 - 媒体文件上传返回")
public class ContentMediaUploadRespVO {

    @Schema(description = "文件访问地址", example = "http://localhost:9000/xiaolvshu-dev/content/video/xxx.mp4")
    private String url;

    @Schema(description = "原始文件名", example = "sample.mp4")
    private String fileName;

    @Schema(description = "MIME 类型", example = "video/mp4")
    private String contentType;

    @Schema(description = "文件大小，单位字节", example = "10485760")
    private long size;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}
