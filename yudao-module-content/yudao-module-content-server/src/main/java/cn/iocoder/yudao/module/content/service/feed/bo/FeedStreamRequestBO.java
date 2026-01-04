package cn.iocoder.yudao.module.content.service.feed.bo;

public class FeedStreamRequestBO {

    private int pageNo = 1;
    private int pageSize = 20;
    private String scene = "home";
    private boolean includeAds = true;
    private int adInterval = 5;

    public int getPageNo() {
        return pageNo;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = Math.max(1, pageNo);
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = Math.min(Math.max(pageSize, 1), 50);
    }

    public String getScene() {
        return scene;
    }

    public void setScene(String scene) {
        this.scene = scene;
    }

    public boolean isIncludeAds() {
        return includeAds;
    }

    public void setIncludeAds(boolean includeAds) {
        this.includeAds = includeAds;
    }

    public int getAdInterval() {
        return adInterval;
    }

    public void setAdInterval(int adInterval) {
        this.adInterval = Math.max(3, adInterval);
    }
}
