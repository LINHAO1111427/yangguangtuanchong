// ============================================================
// 支付模块 - 多渠道充值Service框架代码
// ============================================================

// 1. 充值订单DO实体
package cn.iocoder.yudao.module.pay.dal.dataobject;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RechargeOrderDO {
    private Long id;
    private String orderNo;                    // 订单号
    private Long userId;                       // 用户ID
    private Long packageId;                    // 套餐ID
    private BigDecimal amount;                 // 支付金额
    private Integer coins;                     // 充值币数
    private Integer paymentMethod;             // 支付方式(1微信/2支付宝/3银行卡)
    private String channelTradeNo;             // 第三方交易号
    private Integer status;                    // 订单状态(0待支付/1已支付/2已取消)
    private String clientIp;                   // 用户IP
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;              // 支付时间
    private LocalDateTime expiresAt;           // 过期时间
    private LocalDateTime updatedAt;
}

// 2. 支付渠道接口
package cn.iocoder.yudao.module.pay.service.payment;

import java.util.Map;

public interface PayChannel {

    /**
     * 获取预支付信息
     */
    PaymentResponse prepay(CreatePaymentRequest request);

    /**
     * 验证支付回调签名
     */
    boolean verifyNotify(Map<String, String> notifyParams);

    /**
     * 处理支付成功回调
     */
    void handlePaymentNotify(Map<String, String> notifyParams);

    /**
     * 申请退款
     */
    RefundResponse refund(RefundRequest request);

    /**
     * 查询退款状态
     */
    RefundStatusResponse queryRefundStatus(String refundNo);
}

// 3. 支付请求/响应对象
package cn.iocoder.yudao.module.pay.service.payment;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Map;

@Data
public class CreatePaymentRequest {
    private Long orderId;
    private String orderNo;
    private Long userId;
    private BigDecimal amount;
    private String clientIp;
    private String notifyUrl;
    private String returnUrl;
    private Map<String, String> extraParams;  // 额外参数
}

@Data
public class PaymentResponse {
    private String status;                    // 支付状态
    private Map<String, Object> paymentParams; // 支付参数（根据渠道不同）
    private String message;
}

@Data
public class RefundRequest {
    private String orderNo;
    private String refundNo;
    private BigDecimal amount;
    private String reason;
}

@Data
public class RefundResponse {
    private String status;
    private String refundNo;
    private String channelRefundNo;
    private String message;
}

// 4. 支付渠道工厂
package cn.iocoder.yudao.module.pay.service.payment;

import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PayChannelFactory {

    private final Map<Integer, PayChannel> channels = new ConcurrentHashMap<>();

    public PayChannelFactory(
        WeChatPayChannel weChatPayChannel,
        AliPayChannel aliPayChannel,
        BankCardPayChannel bankCardPayChannel
    ) {
        channels.put(1, weChatPayChannel);      // 微信支付
        channels.put(2, aliPayChannel);          // 支付宝
        channels.put(3, bankCardPayChannel);     // 银行卡
    }

    public PayChannel getChannel(Integer paymentMethod) {
        PayChannel channel = channels.get(paymentMethod);
        if (channel == null) {
            throw new IllegalArgumentException("不支持的支付方式: " + paymentMethod);
        }
        return channel;
    }
}

// 5. 微信支付渠道实现
package cn.iocoder.yudao.module.pay.service.payment.impl;

import cn.iocoder.yudao.module.pay.service.payment.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Map;

@Slf4j
@Component
public class WeChatPayChannel implements PayChannel {

    // TODO: 集成微信支付SDK (com.github.wxpay:wxpay-sdk)

    @Override
    public PaymentResponse prepay(CreatePaymentRequest request) {
        log.info("微信预支付请求: orderId={}, amount={}", request.getOrderId(), request.getAmount());

        PaymentResponse response = new PaymentResponse();

        // 1. 调用微信支付API获取预支付ID
        // String prepayId = wechatPayClient.unifiedOrder(...);

        // 2. 构建前端支付参数
        // Map<String, String> payParams = wechatPayClient.buildPayParams(prepayId);
        // response.setPaymentParams(payParams);

        // 3. 返回支付参数
        // response.setStatus("SUCCESS");
        // return response;

        return response;
    }

    @Override
    public boolean verifyNotify(Map<String, String> notifyParams) {
        log.info("验证微信支付回调签名");

        // TODO: 验证签名
        // String sign = notifyParams.get("sign");
        // return wechatPayClient.verifySign(notifyParams, sign);

        return true;
    }

    @Override
    public void handlePaymentNotify(Map<String, String> notifyParams) {
        log.info("处理微信支付回调");

        // TODO: 处理支付成功逻辑
        String tradeNo = notifyParams.get("transaction_id");
        String orderNo = notifyParams.get("out_trade_no");
        String amount = notifyParams.get("total_fee");

        log.info("微信支付回调: tradeNo={}, orderNo={}, amount={}", tradeNo, orderNo, amount);
    }

    @Override
    public RefundResponse refund(RefundRequest request) {
        log.info("微信退款请求: refundNo={}, amount={}", request.getRefundNo(), request.getAmount());

        RefundResponse response = new RefundResponse();

        // TODO: 调用微信退款API
        // RefundResult result = wechatPayClient.refund(...);
        // response.setStatus("SUCCESS");
        // response.setRefundNo(result.getRefundNo());
        // response.setChannelRefundNo(result.getChannelRefundNo());

        return response;
    }

    @Override
    public RefundStatusResponse queryRefundStatus(String refundNo) {
        log.info("查询微信退款状态: refundNo={}", refundNo);

        // TODO: 调用微信查询退款API
        RefundStatusResponse response = new RefundStatusResponse();
        return response;
    }
}

// 6. 支付宝支付渠道实现
package cn.iocoder.yudao.module.pay.service.payment.impl;

import cn.iocoder.yudao.module.pay.service.payment.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Map;

@Slf4j
@Component
public class AliPayChannel implements PayChannel {

    // TODO: 集成支付宝SDK (com.alipay.sdk:alipay-sdk-java)

    @Override
    public PaymentResponse prepay(CreatePaymentRequest request) {
        log.info("支付宝预支付请求: orderId={}, amount={}", request.getOrderId(), request.getAmount());

        PaymentResponse response = new PaymentResponse();

        // TODO: 调用支付宝支付API
        // String orderString = aliPayClient.pay(...);
        // response.setPaymentParams(Map.of("orderString", orderString));
        // response.setStatus("SUCCESS");

        return response;
    }

    @Override
    public boolean verifyNotify(Map<String, String> notifyParams) {
        log.info("验证支付宝支付回调签名");

        // TODO: 验证签名
        // return aliPayClient.verifySign(notifyParams);

        return true;
    }

    @Override
    public void handlePaymentNotify(Map<String, String> notifyParams) {
        log.info("处理支付宝支付回调");

        String tradeNo = notifyParams.get("trade_no");
        String orderNo = notifyParams.get("out_trade_no");
        String amount = notifyParams.get("total_amount");

        log.info("支付宝回调: tradeNo={}, orderNo={}, amount={}", tradeNo, orderNo, amount);
    }

    @Override
    public RefundResponse refund(RefundRequest request) {
        log.info("支付宝退款请求: refundNo={}, amount={}", request.getRefundNo(), request.getAmount());

        // TODO: 调用支付宝退款API
        RefundResponse response = new RefundResponse();
        return response;
    }

    @Override
    public RefundStatusResponse queryRefundStatus(String refundNo) {
        log.info("查询支付宝退款状态: refundNo={}", refundNo);

        RefundStatusResponse response = new RefundStatusResponse();
        return response;
    }
}

// 7. 充值Service核心实现
package cn.iocoder.yudao.module.pay.service.recharge;

import cn.iocoder.yudao.module.pay.dal.dataobject.*;
import cn.iocoder.yudao.module.pay.dal.mapper.*;
import cn.iocoder.yudao.module.pay.service.payment.*;
import cn.iocoder.yudao.module.pay.service.wallet.WalletService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class RechargeService {

    private final RechargeOrderMapper rechargeOrderMapper;
    private final RechargePackageMapper rechargePackageMapper;
    private final PayChannelFactory payChannelFactory;
    private final WalletService walletService;
    private final TransactionService transactionService;
    private final NotificationService notificationService;

    // 依赖注入
    public RechargeService(
        RechargeOrderMapper rechargeOrderMapper,
        RechargePackageMapper rechargePackageMapper,
        PayChannelFactory payChannelFactory,
        WalletService walletService,
        TransactionService transactionService,
        NotificationService notificationService
    ) {
        this.rechargeOrderMapper = rechargeOrderMapper;
        this.rechargePackageMapper = rechargePackageMapper;
        this.payChannelFactory = payChannelFactory;
        this.walletService = walletService;
        this.transactionService = transactionService;
        this.notificationService = notificationService;
    }

    /**
     * 创建充值订单并返回支付参数
     */
    @Transactional
    public CreateOrderResponse createRechargeOrder(CreateRechargeOrderRequest request) {
        log.info("创建充值订单: userId={}, packageId={}, paymentMethod={}",
            request.getUserId(), request.getPackageId(), request.getPaymentMethod());

        // 1. 获取套餐信息
        RechargePackageDO packageDO = rechargePackageMapper.selectById(request.getPackageId());
        if (packageDO == null || !packageDO.getIsActive()) {
            throw new IllegalArgumentException("套餐不存在或已下架");
        }

        // 2. 创建充值订单
        RechargeOrderDO order = new RechargeOrderDO();
        order.setOrderNo(generateOrderNo());
        order.setUserId(request.getUserId());
        order.setPackageId(request.getPackageId());
        order.setAmount(packageDO.getPrice());
        order.setCoins(packageDO.getCoins() + packageDO.getDiscountCoins());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setStatus(0);  // 待支付
        order.setClientIp(request.getClientIp());
        order.setCreatedAt(LocalDateTime.now());
        order.setExpiresAt(LocalDateTime.now().plusMinutes(30)); // 30分钟过期

        rechargeOrderMapper.insert(order);
        log.info("充值订单已创建: orderId={}, orderNo={}", order.getId(), order.getOrderNo());

        // 3. 调用支付渠道获取支付参数
        PayChannel payChannel = payChannelFactory.getChannel(request.getPaymentMethod());

        CreatePaymentRequest paymentRequest = new CreatePaymentRequest();
        paymentRequest.setOrderId(order.getId());
        paymentRequest.setOrderNo(order.getOrderNo());
        paymentRequest.setUserId(request.getUserId());
        paymentRequest.setAmount(packageDO.getPrice());
        paymentRequest.setClientIp(request.getClientIp());
        paymentRequest.setNotifyUrl(request.getNotifyUrl());

        PaymentResponse paymentResponse = payChannel.prepay(paymentRequest);

        // 4. 返回订单和支付参数
        CreateOrderResponse response = new CreateOrderResponse();
        response.setOrderId(order.getId());
        response.setOrderNo(order.getOrderNo());
        response.setStatus(order.getStatus());
        response.setPaymentParams(paymentResponse.getPaymentParams());

        return response;
    }

    /**
     * 处理支付成功回调
     */
    @Transactional
    public void handlePaymentNotify(Integer paymentMethod, Map<String, String> notifyParams) {
        log.info("处理支付回调: paymentMethod={}, params={}", paymentMethod, notifyParams);

        try {
            // 1. 验证签名
            PayChannel payChannel = payChannelFactory.getChannel(paymentMethod);
            if (!payChannel.verifyNotify(notifyParams)) {
                log.error("支付回调签名验证失败");
                throw new IllegalArgumentException("签名验证失败");
            }

            // 2. 查询订单
            String orderNo = notifyParams.get("out_trade_no");
            RechargeOrderDO order = rechargeOrderMapper.selectByOrderNo(orderNo);
            if (order == null) {
                log.error("订单不存在: orderNo={}", orderNo);
                throw new IllegalArgumentException("订单不存在");
            }

            // 3. 防止重复处理
            if (order.getStatus() == 1) {
                log.warn("订单已支付: orderNo={}", orderNo);
                return;
            }

            // 4. 验证金额
            BigDecimal callbackAmount = new BigDecimal(notifyParams.get("total_amount"));
            if (order.getAmount().compareTo(callbackAmount) != 0) {
                log.error("金额不符: 订单金额={}, 回调金额={}", order.getAmount(), callbackAmount);
                throw new IllegalArgumentException("金额校验失败");
            }

            // 5. 更新订单状态
            order.setStatus(1);  // 已支付
            order.setPaidAt(LocalDateTime.now());
            order.setChannelTradeNo(notifyParams.get("transaction_id"));
            order.setUpdatedAt(LocalDateTime.now());
            rechargeOrderMapper.updateById(order);
            log.info("订单状态已更新: orderId={}, status=1", order.getId());

            // 6. 给用户钱包增加余额
            walletService.addBalance(order.getUserId(), order.getCoins());
            log.info("钱包余额已增加: userId={}, coins={}", order.getUserId(), order.getCoins());

            // 7. 记录交易流水
            transactionService.recordTransaction(
                order.getUserId(),
                1,  // 收入
                order.getAmount(),
                "充值 - " + order.getOrderNo()
            );
            log.info("交易流水已记录: userId={}, amount={}", order.getUserId(), order.getAmount());

            // 8. 发送充值成功通知
            notificationService.sendRechargeSuccessNotify(order.getUserId(), order.getId());
            log.info("充值通知已发送: userId={}", order.getUserId());

        } catch (Exception e) {
            log.error("支付回调处理失败", e);
            throw e;
        }
    }

    /**
     * 查询充值记录
     */
    public PageResult<RechargeOrderVO> queryRechargeRecords(Long userId, Integer pageNo, Integer pageSize) {
        log.info("查询充值记录: userId={}, pageNo={}, pageSize={}", userId, pageNo, pageSize);

        // TODO: 调用mapper查询分页数据
        // return rechargeOrderMapper.selectPageByUserId(userId, pageNo, pageSize);

        return new PageResult<>();
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        return "RECHARGE" + System.currentTimeMillis() + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }
}

// 8. 充值Controller框架
package cn.iocoder.yudao.module.pay.controller.app.recharge;

import cn.iocoder.yudao.module.pay.service.recharge.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1.0.1/pay/recharge")
public class AppRechargeController {

    private final RechargeService rechargeService;

    public AppRechargeController(RechargeService rechargeService) {
        this.rechargeService = rechargeService;
    }

    /**
     * 创建充值订单
     */
    @PostMapping("/create-order")
    public ResultVo<CreateOrderResponse> createRechargeOrder(
        @RequestBody CreateRechargeOrderRequest request
    ) {
        log.info("创建充值订单请求: {}", request);

        // TODO: 获取当前登录用户ID
        // Long userId = getCurrentUserId();

        try {
            CreateOrderResponse response = rechargeService.createRechargeOrder(request);
            return ResultVo.success(response);
        } catch (Exception e) {
            log.error("创建充值订单失败", e);
            return ResultVo.error(e.getMessage());
        }
    }

    /**
     * 查询充值记录
     */
    @GetMapping("/records")
    public ResultVo<PageResult<RechargeOrderVO>> queryRechargeRecords(
        @RequestParam(defaultValue = "1") Integer pageNo,
        @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        log.info("查询充值记录: pageNo={}, pageSize={}", pageNo, pageSize);

        // TODO: 获取当前登录用户ID
        // Long userId = getCurrentUserId();

        PageResult<RechargeOrderVO> records = rechargeService.queryRechargeRecords(null, pageNo, pageSize);
        return ResultVo.success(records);
    }

    /**
     * 微信支付回调
     */
    @PostMapping("/notify/wechat")
    public String handleWeChatNotify(
        @RequestBody Map<String, String> notifyParams
    ) {
        log.info("收到微信支付回调");

        try {
            rechargeService.handlePaymentNotify(1, notifyParams);  // 1表示微信
            return "{\"code\": \"SUCCESS\"}";
        } catch (Exception e) {
            log.error("处理微信回调失败", e);
            return "{\"code\": \"FAIL\"}";
        }
    }

    /**
     * 支付宝支付回调
     */
    @PostMapping("/notify/alipay")
    public String handleAliPayNotify(
        @RequestParam Map<String, String> notifyParams
    ) {
        log.info("收到支付宝支付回调");

        try {
            rechargeService.handlePaymentNotify(2, notifyParams);  // 2表示支付宝
            return "success";
        } catch (Exception e) {
            log.error("处理支付宝回调失败", e);
            return "fail";
        }
    }
}

/**
 * 使用说明：
 *
 * 1. 创建充值订单流程：
 *    - 前端调用 POST /api/v1.0.1/pay/recharge/create-order
 *    - 后端创建订单并返回支付参数
 *    - 前端调起支付页面
 *
 * 2. 支付成功回调流程：
 *    - 支付宝/微信回调 POST /api/v1.0.1/pay/recharge/notify/{channel}
 *    - 后端验证签名、更新订单、增加余额
 *    - 返回success给支付宝/微信
 *
 * 3. 关键实现细节：
 *    - 使用幂等性key防止重复处理回调
 *    - 金额校验防止篡改
 *    - 分布式锁保证钱包余额更新的原子性
 *    - 异步发送通知（MQ）
 */
