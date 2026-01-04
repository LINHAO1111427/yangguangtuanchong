package cn.iocoder.yudao.module.content.service.favorite.bo;

public class FavoriteGroupUpdateReqBO extends FavoriteGroupCreateReqBO {

    private Long groupId;

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }
}
