package cn.iocoder.yudao.module.content.framework.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;

import static cn.iocoder.yudao.module.content.constants.ContentConstants.KafkaTopic.*;

/**
 * Kafka 配置类
 *
 * @author 阳光团宠
 */
@EnableKafka
@Configuration
public class KafkaConfiguration {

    /**
     * 创建用户行为主题
     */
    @Bean
    public NewTopic userBehaviorTopic() {
        return TopicBuilder.name(USER_BEHAVIOR)
                .partitions(3) // 3个分区
                .replicas(1) // 1个副本（开发环境）
                .build();
    }

    /**
     * 创建内容互动主题
     */
    @Bean
    public NewTopic contentInteractionTopic() {
        return TopicBuilder.name(CONTENT_INTERACTION)
                .partitions(3)
                .replicas(1)
                .build();
    }

    /**
     * 创建内容举报主题
     */
    @Bean
    public NewTopic contentReportTopic() {
        return TopicBuilder.name(CONTENT_REPORT)
                .partitions(2)
                .replicas(1)
                .build();
    }
}
