package cn.iocoder.yudao.module.content.job;

import cn.iocoder.yudao.framework.tenant.core.job.TenantJob;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Content Module Partition Maintenance Job
 */
@Component
@Slf4j
public class ContentPartitionMaintenanceJob {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @XxlJob("contentPartitionMaintenanceJob")
    @TenantJob
    public void execute() {
        log.info("[contentPartitionMaintenanceJob] Starting partition maintenance...");
        try {
            jdbcTemplate.execute("SELECT create_monthly_partitions('content_post', 6)");
            jdbcTemplate.execute("SELECT create_daily_partitions('content_comment', 30)");
            log.info("[contentPartitionMaintenanceJob] Partition maintenance completed");
        } catch (Exception e) {
            log.error("[contentPartitionMaintenanceJob] Partition maintenance failed", e);
            throw e;
        }
    }
}
