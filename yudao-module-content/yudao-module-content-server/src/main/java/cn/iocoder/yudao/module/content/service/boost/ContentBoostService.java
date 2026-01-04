package cn.iocoder.yudao.module.content.service.boost;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.content.controller.app.vo.boost.ContentBoostConfigRespVO;
import cn.iocoder.yudao.module.content.controller.app.vo.boost.ContentBoostRecordRespVO;
import cn.iocoder.yudao.module.content.controller.app.vo.boost.ContentBoostStartReqVO;
import cn.iocoder.yudao.module.content.controller.app.vo.boost.ContentBoostStatRespVO;

import java.util.List;

public interface ContentBoostService {

    Long startBoost(ContentBoostStartReqVO reqVO);

    boolean cancelBoost(Long boostRecordId, Long userId);

    PageResult<ContentBoostRecordRespVO> getMyBoostRecords(Long userId, Integer pageNo, Integer pageSize);

    ContentBoostRecordRespVO getBoostRecordDetail(Long boostRecordId, Long userId);

    List<ContentBoostStatRespVO> getBoostStats(Long boostRecordId, Long userId);

    List<ContentBoostConfigRespVO> getBoostConfigs();
}