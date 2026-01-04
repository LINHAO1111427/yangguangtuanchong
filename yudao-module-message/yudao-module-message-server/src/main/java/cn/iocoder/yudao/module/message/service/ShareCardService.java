package cn.iocoder.yudao.module.message.service;

import cn.iocoder.yudao.module.message.controller.app.vo.AppShareCardRespVO;

public interface ShareCardService {

    /**
     * 获取分享作品卡片信息
     *
     * @param userId 当前用户ID
     * @param contentId 内容ID
     */
    AppShareCardRespVO getShareCard(Long userId, Long contentId);
}

