package cn.iocoder.yudao.module.content.service.ad;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.content.controller.admin.ad.vo.ContentAdStatPageReqVO;
import cn.iocoder.yudao.module.content.controller.admin.ad.vo.ContentAdStatRespVO;
import cn.iocoder.yudao.module.content.controller.admin.ad.vo.ContentAdStatSummaryRespVO;
import cn.iocoder.yudao.module.content.dal.dataobject.ContentAdEventDO;
import cn.iocoder.yudao.module.content.dal.mysql.ContentAdEventMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ContentAdEventService {

    private static final int EVENT_IMPRESSION = 1;
    private static final int EVENT_CLICK = 2;
    private static final BigDecimal REVENUE_DIVISOR = new BigDecimal("10");

    @Resource
    private ContentAdEventMapper contentAdEventMapper;

    public void recordImpression(Long adId, Long userId, Integer scene) {
        recordEvent(adId, userId, scene, EVENT_IMPRESSION);
    }

    public void recordClick(Long adId, Long userId, Integer scene) {
        recordEvent(adId, userId, scene, EVENT_CLICK);
    }

    public PageResult<ContentAdStatRespVO> getStatPage(ContentAdStatPageReqVO reqVO) {
        int pageNo = reqVO.getPageNo() == null ? 1 : Math.max(reqVO.getPageNo(), 1);
        int pageSize = reqVO.getPageSize() == null ? 20 : Math.max(reqVO.getPageSize(), 1);
        int offset = (pageNo - 1) * pageSize;
        LocalDateTime start = reqVO.getStartTime();
        LocalDateTime end = reqVO.getEndTime();
        List<ContentAdStatRespVO> list = contentAdEventMapper.selectStatPage(
                reqVO.getAdId(), reqVO.getScene(), start, end, pageSize, offset);
        list.forEach(this::fillStatRow);
        Long total = contentAdEventMapper.selectStatCount(
                reqVO.getAdId(), reqVO.getScene(), start, end);
        return new PageResult<>(list, total == null ? 0L : total);
    }

    public ContentAdStatSummaryRespVO getStatSummary(Long adId, Integer scene, LocalDateTime startTime, LocalDateTime endTime) {
        ContentAdStatSummaryRespVO summary = contentAdEventMapper.selectStatSummary(adId, scene, startTime, endTime);
        if (summary == null) {
            summary = new ContentAdStatSummaryRespVO();
        }
        if (summary.getImpressionCount() == null) {
            summary.setImpressionCount(0L);
        }
        if (summary.getClickCount() == null) {
            summary.setClickCount(0L);
        }
        if (summary.getUniqueImpressionCount() == null) {
            summary.setUniqueImpressionCount(0L);
        }
        if (summary.getUniqueClickCount() == null) {
            summary.setUniqueClickCount(0L);
        }
        summary.setRevenue(calcRevenue(summary.getImpressionCount()));
        return summary;
    }

    private void recordEvent(Long adId, Long userId, Integer scene, int eventType) {
        if (adId == null) {
            return;
        }
        ContentAdEventDO event = new ContentAdEventDO();
        event.setAdId(adId);
        event.setUserId(userId);
        event.setEventType(eventType);
        event.setScene(scene);
        contentAdEventMapper.insert(event);
    }

    private void fillStatRow(ContentAdStatRespVO row) {
        if (row == null) {
            return;
        }
        if (row.getImpressionCount() == null) {
            row.setImpressionCount(0L);
        }
        if (row.getClickCount() == null) {
            row.setClickCount(0L);
        }
        if (row.getUniqueImpressionCount() == null) {
            row.setUniqueImpressionCount(0L);
        }
        if (row.getUniqueClickCount() == null) {
            row.setUniqueClickCount(0L);
        }
        row.setRevenue(calcRevenue(row.getImpressionCount()));
    }

    private BigDecimal calcRevenue(Long impressionCount) {
        if (impressionCount == null || impressionCount <= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(impressionCount)
                .divide(REVENUE_DIVISOR, 2, RoundingMode.DOWN);
    }
}
