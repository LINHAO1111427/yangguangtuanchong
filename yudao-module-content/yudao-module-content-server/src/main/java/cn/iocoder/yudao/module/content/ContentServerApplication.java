package cn.iocoder.yudao.module.content;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 项目的启动类
 *
 * 如果你碰到启动的问题，请认真阅读 https://cloud.iocoder.cn/quick-start/ 文章
 *
 * @author 阳光团宠
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {
        "cn.iocoder.yudao.module.member.api",
        "cn.iocoder.yudao.module.system.api",
        "cn.iocoder.yudao.module.infra.api",
        "cn.iocoder.yudao.module.pay.api"
})
public class ContentServerApplication {

    public static void main(String[] args) {
        // 如果你碰到启动的问题,请认真阅读 https://cloud.iocoder.cn/quick-start/ 文章
        SpringApplication.run(ContentServerApplication.class, args);
    }

}
