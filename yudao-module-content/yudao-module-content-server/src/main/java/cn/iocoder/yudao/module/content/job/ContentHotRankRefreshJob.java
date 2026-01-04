package cn.iocoder.yudao.module.content.job;

import cn.iocoder.yudao.framework.tenant.core.job.TenantJob;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Content Hot Rank Refresh Job
 */
@Component
@Slf4j
public class ContentHotRankRefreshJob {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @XxlJob("contentHotRankRefreshJob")
    @TenantJob
    public void execute() {
        log.info("[contentHotRankRefreshJob] Starting hot rank refresh...");
        try {
            jdbcTemplate.execute("REFRESH MATERIALIZED VIEW CONCURRENTLY mv_content_hot_rank");
            log.info("[contentHotRankRefreshJob] Hot rank refresh completed");
        } catch (Exception e) {
            log.error("[contentHotRankRefreshJob] Hot rank refresh failed", e);
            throw e;
        }
    }
}
