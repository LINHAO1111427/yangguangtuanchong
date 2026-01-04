package cn.iocoder.yudao.module.message;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Message 服务 启动类
 * 阳光团宠消息系统（群聊、私信、通知、WebSocket）
 *
 * @author xiaolvshu
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {
        "cn.iocoder.yudao.module.member.api",
        "cn.iocoder.yudao.module.system.api",
        "cn.iocoder.yudao.module.infra.api",
        "cn.iocoder.yudao.module.content.api"
})
public class MessageServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MessageServerApplication.class, args);
    }

}
