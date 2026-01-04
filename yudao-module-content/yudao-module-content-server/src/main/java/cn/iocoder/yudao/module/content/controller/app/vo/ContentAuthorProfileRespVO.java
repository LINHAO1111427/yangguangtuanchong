package cn.iocoder.yudao.module.content.controller.app.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "APP - 内容作者主页信息")
public class ContentAuthorProfileRespVO {

    @Schema(description = "作者用户编号", example = "1024")
    private Long userId;

    @Schema(description = "作者昵称", example = "小绿书官方账号")
    private String nickname;

    @Schema(description = "作者头像")
    private String avatar;

    @Schema(description = "作者积分", example = "520")
    private Integer point;

    @Schema(description = "作品数量", example = "32")
    private Long workCount;

    @Schema(description = "关注数量", example = "128")
    private Long followingCount;

    @Schema(description = "粉丝数量", example = "3456")
    private Long followersCount;

    @Schema(description = "当前用户是否已关注该作者", example = "false")
    private Boolean isFollowed;

    @Schema(description = "累计点赞数", example = "999")
    private Long totalLikeCount;

    @Schema(description = "累计评论数", example = "123")
    private Long totalCommentCount;

    @Schema(description = "累计收藏数", example = "666")
    private Long totalCollectCount;

    @Schema(description = "累计浏览数", example = "18888")
    private Long totalViewCount;

    @Schema(description = "是否本人主页", example = "false")
    private Boolean mine;

    @Schema(description = "入驻时间")
    private LocalDateTime joinTime;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Integer getPoint() {
        return point;
    }

    public void setPoint(Integer point) {
        this.point = point;
    }

    public Long getWorkCount() {
        return workCount;
    }

    public void setWorkCount(Long workCount) {
        this.workCount = workCount;
    }

    public Long getFollowingCount() {
        return followingCount;
    }

    public void setFollowingCount(Long followingCount) {
        this.followingCount = followingCount;
    }

    public Long getFollowersCount() {
        return followersCount;
    }

    public void setFollowersCount(Long followersCount) {
        this.followersCount = followersCount;
    }

    public Boolean getIsFollowed() {
        return isFollowed;
    }

    public void setIsFollowed(Boolean followed) {
        isFollowed = followed;
    }

    public Long getTotalLikeCount() {
        return totalLikeCount;
    }

    public void setTotalLikeCount(Long totalLikeCount) {
        this.totalLikeCount = totalLikeCount;
    }

    public Long getTotalCommentCount() {
        return totalCommentCount;
    }

    public void setTotalCommentCount(Long totalCommentCount) {
        this.totalCommentCount = totalCommentCount;
    }

    public Long getTotalCollectCount() {
        return totalCollectCount;
    }

    public void setTotalCollectCount(Long totalCollectCount) {
        this.totalCollectCount = totalCollectCount;
    }

    public Long getTotalViewCount() {
        return totalViewCount;
    }

    public void setTotalViewCount(Long totalViewCount) {
        this.totalViewCount = totalViewCount;
    }

    public Boolean getMine() {
        return mine;
    }

    public void setMine(Boolean mine) {
        this.mine = mine;
    }

    public LocalDateTime getJoinTime() {
        return joinTime;
    }

    public void setJoinTime(LocalDateTime joinTime) {
        this.joinTime = joinTime;
    }
}
