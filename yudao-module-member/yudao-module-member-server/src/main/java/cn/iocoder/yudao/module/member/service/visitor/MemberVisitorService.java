package cn.iocoder.yudao.module.member.service.visitor;

import cn.iocoder.yudao.module.member.controller.app.visitor.vo.AppMemberVisitorStatsRespVO;

public interface MemberVisitorService {

    void recordVisit(Long ownerId, Long visitorId, Integer visitType, Long targetId);

    AppMemberVisitorStatsRespVO getMyVisitorStats(Long ownerId, Integer days);
}

