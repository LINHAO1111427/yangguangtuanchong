package cn.iocoder.yudao.module.member.service.visitor;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.member.controller.app.visitor.vo.AppMemberVisitorPageReqVO;
import cn.iocoder.yudao.module.member.controller.app.visitor.vo.AppMemberVisitorStatsRespVO;
import cn.iocoder.yudao.module.member.dal.dataobject.visitor.MemberVisitorLogDO;
import cn.iocoder.yudao.module.member.dal.mysql.visitor.MemberVisitorLogMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Validated
@Slf4j
public class MemberVisitorServiceImpl implements MemberVisitorService {

    private static final int DEFAULT_DAYS = 7;

    @Resource
    private MemberVisitorLogMapper visitorLogMapper;

    @Override
    public void recordVisit(Long ownerId, Long visitorId, Integer visitType, Long targetId) {
        if (ownerId == null || visitorId == null) {
            return;
        }
        if (ownerId.equals(visitorId)) {
            return;
        }

        MemberVisitorLogDO logDO = new MemberVisitorLogDO();
        logDO.setUserId(ownerId);
        logDO.setVisitorId(visitorId);
        logDO.setVisitType(visitType != null ? visitType : 1);
        logDO.setTargetId(targetId);
        logDO.setIsPaid(false);
        logDO.setPayAmount(null);
        visitorLogMapper.insert(logDO);
    }

    @Override
    public AppMemberVisitorStatsRespVO getMyVisitorStats(Long ownerId, Integer days) {
        int rangeDays = (days != null && days > 0 && days <= 31) ? days : DEFAULT_DAYS;
        LocalDate today = LocalDate.now();
        LocalDate beginDate = today.minusDays(rangeDays - 1L);
        AppMemberVisitorStatsRespVO resp = new AppMemberVisitorStatsRespVO();
        resp.setDays(rangeDays);
        try {
            LocalDateTime beginTime = LocalDateTime.of(beginDate, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(today.plusDays(1), LocalTime.MIN);

            LocalDate prevBeginDate = beginDate.minusDays(rangeDays);
            LocalDate prevEndDate = beginDate.minusDays(1L);
            LocalDateTime prevBeginTime = LocalDateTime.of(prevBeginDate, LocalTime.MIN);
            LocalDateTime prevEndTime = LocalDateTime.of(prevEndDate.plusDays(1), LocalTime.MIN);

            long total = visitorLogMapper.selectDistinctVisitorCount(ownerId, beginTime, endTime);
            long compareTotal = visitorLogMapper.selectDistinctVisitorCount(ownerId, prevBeginTime, prevEndTime);

            Integer changePercent = null;
            if (compareTotal == 0) {
                if (total > 0) {
                    changePercent = 100;
                }
            } else {
                changePercent = (int) Math.round(((total - compareTotal) * 100.0d) / compareTotal);
            }

            List<MemberVisitorLogMapper.MemberVisitorDayRow> rows =
                    visitorLogMapper.selectDailyDistinctVisitorCount(ownerId, beginTime, endTime);

            Map<String, Long> dayCountMap = new HashMap<>();
            if (CollUtil.isNotEmpty(rows)) {
                for (MemberVisitorLogMapper.MemberVisitorDayRow r : rows) {
                    if (r == null || r.getDay() == null) continue;
                    dayCountMap.put(r.getDay(), r.getCnt() == null ? 0L : r.getCnt());
                }
            }

            List<AppMemberVisitorStatsRespVO.Day> daily = new ArrayList<>(rangeDays);
            for (int i = 0; i < rangeDays; i++) {
                LocalDate d = beginDate.plusDays(i);
                String key = d.toString(); // yyyy-MM-dd
                daily.add(new AppMemberVisitorStatsRespVO.Day(key, dayCountMap.getOrDefault(key, 0L)));
            }

            resp.setTotal(total);
            resp.setCompareTotal(compareTotal);
            resp.setChangePercent(changePercent);
            resp.setDaily(daily);
        } catch (Exception e) {
            log.warn("[getMyVisitorStats] load stats failed, ownerId={}, days={}", ownerId, days, e);
            List<AppMemberVisitorStatsRespVO.Day> daily = new ArrayList<>(rangeDays);
            for (int i = 0; i < rangeDays; i++) {
                LocalDate d = beginDate.plusDays(i);
                daily.add(new AppMemberVisitorStatsRespVO.Day(d.toString(), 0L));
            }
            resp.setTotal(0L);
            resp.setCompareTotal(0L);
            resp.setChangePercent(null);
            resp.setDaily(daily);
        }
        return resp;
    }

    @Override
    public PageResult<MemberVisitorLogDO> getVisitorPage(Long userId, AppMemberVisitorPageReqVO pageReqVO) {
        if (userId == null) {
            return PageResult.empty();
        }
        Page<MemberVisitorLogDO> page = new Page<>(pageReqVO.getPageNo(), pageReqVO.getPageSize());
        IPage<MemberVisitorLogDO> result;
        if (Boolean.TRUE.equals(pageReqVO.getAsVisitor())) {
            result = visitorLogMapper.selectPageByVisitor(page, userId, pageReqVO.getVisitType());
        } else {
            result = visitorLogMapper.selectPageByUser(page, userId, pageReqVO.getVisitType());
        }
        return new PageResult<>(result.getRecords(), result.getTotal());
    }

    @Override
    public void clearVisitorLogs(Long userId, Boolean asVisitor) {
        if (userId == null) {
            return;
        }
        LambdaUpdateWrapper<MemberVisitorLogDO> wrapper = new LambdaUpdateWrapper<>();
        if (Boolean.TRUE.equals(asVisitor)) {
            wrapper.eq(MemberVisitorLogDO::getVisitorId, userId);
        } else {
            wrapper.eq(MemberVisitorLogDO::getUserId, userId);
        }
        MemberVisitorLogDO update = new MemberVisitorLogDO();
        update.setDeleted(1);
        visitorLogMapper.update(update, wrapper);
    }
}
