package cn.iocoder.yudao.module.content.service.vo;

/**
 * 聚合作者作品数据的简单结构，供作者主页等场景使用。
 */
public class ContentAuthorStats {

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

    public static ContentAuthorStats empty(Long userId) {
        ContentAuthorStats stats = new ContentAuthorStats();
        stats.setUserId(userId);
        stats.setWorkCount(0L);
        stats.setTotalLikeCount(0L);
        stats.setTotalCommentCount(0L);
        stats.setTotalCollectCount(0L);
        stats.setTotalViewCount(0L);
        return stats;
    }
}
