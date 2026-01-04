package cn.iocoder.yudao.module.content.framework.rpc.config;

import cn.iocoder.yudao.module.infra.api.file.FileApi;
import cn.iocoder.yudao.module.pay.api.reward.PayRewardApi;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

/**
 * RPC client configuration for the content service.
 */
@Configuration(value = "contentRpcConfiguration", proxyBeanMethods = false)
@EnableFeignClients(clients = {
        FileApi.class,
        PayRewardApi.class
})
public class RpcConfiguration {
}
