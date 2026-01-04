package cn.iocoder.yudao.module.message.service;

import cn.iocoder.yudao.module.message.controller.app.vo.AppSuggestedFriendRespVO;

import java.util.List;

public interface SuggestedFriendService {

    /**
     * 可能认识的人（算法占位）
     *
     * 依据：同一城市/区、100米距离50次、同连WiFi/IP 50次、同一学校年级等。
     * 当前实现返回空列表，后续由算法服务填充。
     */
    List<AppSuggestedFriendRespVO> getSuggestedFriends(Long userId, Integer limit,
                                                      String cityCode, String districtCode,
                                                      Double lat, Double lng,
                                                      String wifiHash, String ipHash,
                                                      Long schoolId, String grade);
}

