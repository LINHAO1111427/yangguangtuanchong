package cn.iocoder.yudao.module.message.service;

import cn.iocoder.yudao.module.message.controller.app.vo.AppSuggestedFriendRespVO;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class SuggestedFriendServiceImpl implements SuggestedFriendService {

    @Override
    public List<AppSuggestedFriendRespVO> getSuggestedFriends(Long userId, Integer limit,
                                                             String cityCode, String districtCode,
                                                             Double lat, Double lng,
                                                             String wifiHash, String ipHash,
                                                             Long schoolId, String grade) {
        return Collections.emptyList();
    }
}

