package cn.iocoder.yudao.module.content.api.dto;

import java.io.Serializable;

/**
 * 内容作者在全站的聚合统计信息.
 *
 * @author Lin
 */
public class ContentUserStatsRespDTO implements Serializable {

    private Long userId;
    private Long workCount;
    private Long totalLikeCount;
    private Long totalCommentCount;
    private Long totalCollectCount;
    private Long totalViewCount;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getWorkCount() {
        return workCount;
    }

    public void setWorkCount(Long workCount) {
        this.workCount = workCount;
    }

    public Long getTotalLikeCount() {
        return totalLikeCount;
    }

    public void setTotalLikeCount(Long totalLikeCount) {
        this.totalLikeCount = totalLikeCount;
    }

    public Long getTotalCollectCount() {
        return totalCollectCount;
    }

    public void setTotalCollectCount(Long totalCollectCount) {
        this.totalCollectCount = totalCollectCount;
    }

    public Long getTotalCommentCount() {
        return totalCommentCount;
    }

    public void setTotalCommentCount(Long totalCommentCount) {
        this.totalCommentCount = totalCommentCount;
    }

    public Long getTotalViewCount() {
        return totalViewCount;
    }

    public void setTotalViewCount(Long totalViewCount) {
        this.totalViewCount = totalViewCount;
    }
}
