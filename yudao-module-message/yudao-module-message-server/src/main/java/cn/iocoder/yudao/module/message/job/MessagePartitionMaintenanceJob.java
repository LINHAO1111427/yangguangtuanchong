package cn.iocoder.yudao.module.message.job;

import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Message Module Partition Maintenance Job
 *
 * Purpose:
 * Auto-create future partitions for message_detail (monthly)
 *
 * Schedule: Monthly execution (1st day of month at 2:00 AM)
 *
 * @author xiaolvshu
 */
@Component
@Slf4j
public class MessagePartitionMaintenanceJob {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Create future partitions to prevent "partition not found" errors
     */
    @XxlJob("messagePartitionMaintenanceJob")
    public void execute() {
        log.info("[messagePartitionMaintenanceJob] Starting partition maintenance...");

        try {
            // Create 6 months ahead partitions for message_detail
            jdbcTemplate.execute("SELECT create_monthly_partitions('message_detail', 6)");
            log.info("[messagePartitionMaintenanceJob] Created future partitions for message_detail (6 months)");

            log.info("[messagePartitionMaintenanceJob] Partition maintenance completed successfully");

        } catch (Exception e) {
            log.error("[messagePartitionMaintenanceJob] Partition maintenance failed", e);
            throw e;
        }
    }
}
