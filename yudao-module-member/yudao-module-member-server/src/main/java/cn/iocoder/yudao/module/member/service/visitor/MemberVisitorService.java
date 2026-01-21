package cn.iocoder.yudao.module.member.service.visitor;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.member.controller.app.visitor.vo.AppMemberVisitorPageReqVO;
import cn.iocoder.yudao.module.member.controller.app.visitor.vo.AppMemberVisitorStatsRespVO;
import cn.iocoder.yudao.module.member.dal.dataobject.visitor.MemberVisitorLogDO;

public interface MemberVisitorService {

    void recordVisit(Long ownerId, Long visitorId, Integer visitType, Long targetId);

    AppMemberVisitorStatsRespVO getMyVisitorStats(Long ownerId, Integer days);

    PageResult<MemberVisitorLogDO> getVisitorPage(Long userId, AppMemberVisitorPageReqVO pageReqVO);

    void clearVisitorLogs(Long userId, Boolean asVisitor);
}
