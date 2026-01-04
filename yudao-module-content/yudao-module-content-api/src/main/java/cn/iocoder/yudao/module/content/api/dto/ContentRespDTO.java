package cn.iocoder.yudao.module.content.api.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * RPC 服务 - 内容信息 Response DTO
 *
 * @author xiaolvshu
 */
@Data
public class ContentRespDTO {

    /**
     * 内容ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 内容类型（1图文 2视频 3动态）
     */
    private Integer contentType;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 图片列表
     */
    private List<String> images;

    /**
     * 视频URL
     */
    private String videoUrl;

    /**
     * 视频封面
     */
    private String videoCover;

    /**
     * 视频时长（秒）
     */
    private Integer videoDuration;

    /**
     * 视频宽度
     */
    private Integer videoWidth;

    /**
     * 视频高度
     */
    private Integer videoHeight;

    /**
     * 视频文件大小（字节）
     */
    private Long videoFileSize;

    /**
     * 视频格式（mp4/mov/avi）
     */
    private String videoFormat;

    /**
     * 视频质量（1-低/2-标清/3-高清/4-超清）
     */
    private Integer videoQuality;

    /**
     * 是否公开（0草稿 1公开）
     */
    private Integer isPublic;

    /**
     * 审核状态（0待审核 1通过 2拒绝）
     */
    private Integer auditStatus;

    /**
     * 审核备注
     */
    private String auditRemark;

    /**
     * 浏览次数
     */
    private Integer viewCount;

    /**
     * 点赞次数
     */
    private Integer likeCount;

    /**
     * 评论次数
     */
    private Integer commentCount;

    /**
     * 分享次数
     */
    private Integer shareCount;

    /**
     * 收藏次数
     */
    private Integer collectCount;

    /**
     * 转发次数
     */
    private Integer forwardCount;

    /**
     * 完播率（百分比）
     */
    private java.math.BigDecimal completionRate;

    /**
     * 平均观看时长（秒）
     */
    private java.math.BigDecimal avgWatchTime;

    /**
     * 最后播放时间
     */
    private LocalDateTime lastPlayTime;

    /**
     * 是否推荐
     */
    private Boolean isRecommend;

    /**
     * 是否热门
     */
    private Boolean isHot;

    /**
     * 是否置顶
     */
    private Boolean isTop;

    /**
     * 是否允许下载
     */
    private Boolean allowDownload;

    /**
     * 发布时间
     */
    private LocalDateTime publishTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

}
