package cn.iocoder.yudao.module.content.service.boost;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.content.controller.app.vo.boost.ContentBoostConfigRespVO;
import cn.iocoder.yudao.module.content.controller.app.vo.boost.ContentBoostRecordRespVO;
import cn.iocoder.yudao.module.content.controller.app.vo.boost.ContentBoostStartReqVO;
import cn.iocoder.yudao.module.content.controller.app.vo.boost.ContentBoostStatRespVO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class ContentBoostServiceImpl implements ContentBoostService {

    @Override
    public Long startBoost(ContentBoostStartReqVO reqVO) {
        // Return a mock id so the API remains functional.
        return System.currentTimeMillis();
    }

    @Override
    public boolean cancelBoost(Long boostRecordId, Long userId) {
        return true;
    }

    @Override
    public PageResult<ContentBoostRecordRespVO> getMyBoostRecords(Long userId, Integer pageNo, Integer pageSize) {
        return PageResult.empty();
    }

    @Override
    public ContentBoostRecordRespVO getBoostRecordDetail(Long boostRecordId, Long userId) {
        ContentBoostRecordRespVO vo = new ContentBoostRecordRespVO();
        vo.setId(boostRecordId);
        vo.setUserId(userId);
        vo.setStartTime(LocalDateTime.now());
        vo.setEndTime(LocalDateTime.now());
        vo.setStatus("NONE");
        return vo;
    }

    @Override
    public List<ContentBoostStatRespVO> getBoostStats(Long boostRecordId, Long userId) {
        return Collections.emptyList();
    }

    @Override
    public List<ContentBoostConfigRespVO> getBoostConfigs() {
        return Collections.emptyList();
    }
}
