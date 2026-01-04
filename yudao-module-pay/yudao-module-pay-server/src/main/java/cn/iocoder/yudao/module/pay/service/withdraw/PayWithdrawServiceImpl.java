package cn.iocoder.yudao.module.pay.service.withdraw;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import cn.iocoder.yudao.module.pay.controller.admin.withdraw.vo.PayWithdrawPageReqVO;
import cn.iocoder.yudao.module.pay.controller.admin.withdraw.vo.PayWithdrawRespVO;
import cn.iocoder.yudao.module.pay.controller.admin.withdraw.vo.PayWithdrawStatisticsRespVO;
import cn.iocoder.yudao.module.pay.controller.app.withdraw.vo.AppWithdrawApplyReqVO;
import cn.iocoder.yudao.module.pay.dal.dataobject.wallet.PayWalletDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.withdraw.PayWithdrawOrderDO;
import cn.iocoder.yudao.module.pay.dal.mysql.withdraw.PayWithdrawOrderMapper;
import cn.iocoder.yudao.module.pay.enums.wallet.PayWalletBizTypeEnum;
import cn.iocoder.yudao.module.pay.enums.withdraw.PayWithdrawStatusEnum;
import cn.iocoder.yudao.module.pay.service.wallet.PayWalletService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.pay.enums.ErrorCodeConstants.*;

/**
 * 提现服务 Service 实现类
 *
 * @author xiaolvshu
 */
@Service
@Validated
@Slf4j
public class PayWithdrawServiceImpl implements PayWithdrawService {

    @Resource
    private PayWithdrawOrderMapper withdrawOrderMapper;
    @Resource
    private PayWalletService walletService;
    @Resource
    private MemberUserApi memberUserApi;
    @Resource
    private AdminUserApi adminUserApi;

    /**
     * 提现手续费率（0.1%）
     */
    private static final BigDecimal WITHDRAW_FEE_RATE = new BigDecimal("0.1");

    /**
     * 每日提现次数限制
     */
    private static final int DAILY_WITHDRAW_LIMIT = 3;

    /**
     * 最低提现金额（10元 = 1000分）
     */
    private static final int MIN_WITHDRAW_AMOUNT = 1000;

    /**
     * 自动审核阈值（1000元 = 100000分）
     */
    private static final int AUTO_AUDIT_THRESHOLD = 100000;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long applyWithdraw(Long userId, AppWithdrawApplyReqVO applyReqVO) {
        // 1. 校验提现金额
        if (applyReqVO.getAmount() < MIN_WITHDRAW_AMOUNT) {
            throw exception(WITHDRAW_AMOUNT_TOO_LOW);
        }

        // 2. 校验每日提现次数
        Long todayCount = withdrawOrderMapper.selectTodayCountByUserId(userId, LocalDate.now());
        if (todayCount >= DAILY_WITHDRAW_LIMIT) {
            throw exception(WITHDRAW_DAILY_LIMIT_EXCEEDED);
        }

        // 3. 检查是否有进行中的提现订单
        List<PayWithdrawOrderDO> inProgressOrders = withdrawOrderMapper.selectInProgressByUserId(userId);
        if (!inProgressOrders.isEmpty()) {
            throw exception(WITHDRAW_IN_PROGRESS_EXISTS);
        }

        // 4. 获取用户钱包并校验余额
        PayWalletDO wallet = walletService.getOrCreateWallet(userId, 1);
        if (wallet.getBalance() < applyReqVO.getAmount()) {
            throw exception(WALLET_BALANCE_NOT_ENOUGH);
        }

        // 5. 计算手续费和实际到账金额
        BigDecimal amount = new BigDecimal(applyReqVO.getAmount());
        Integer fee = amount.multiply(WITHDRAW_FEE_RATE)
                .divide(new BigDecimal("100"), 0, RoundingMode.HALF_UP)
                .intValue();
        Integer realAmount = applyReqVO.getAmount() - fee;

        // 6. 创建提现订单
        PayWithdrawOrderDO order = PayWithdrawOrderDO.builder()
                .userId(userId)
                .walletId(wallet.getId())
                .amount(applyReqVO.getAmount())
                .fee(fee)
                .realAmount(realAmount)
                .bankCardNo(applyReqVO.getBankCardNo())
                .bankName(applyReqVO.getBankName())
                .accountName(applyReqVO.getAccountName())
                .status(PayWithdrawStatusEnum.PENDING.getStatus()) // 待审核
                .remark(applyReqVO.getRemark())
                .build();
        withdrawOrderMapper.insert(order);

        // 7. 冻结提现金额
        walletService.freezePrice(wallet.getId(), applyReqVO.getAmount());

        log.info("[applyWithdraw][用户({}) 申请提现 金额({})分，手续费({})分，实际到账({})分，订单ID({})]",
                userId, applyReqVO.getAmount(), fee, realAmount, order.getId());

        // 8. 自动审核（金额<1000元）
        if (applyReqVO.getAmount() < AUTO_AUDIT_THRESHOLD) {
            auditWithdraw(order.getId(), true, "自动审核通过", 0L);
        }

        return order.getId();
    }

    @Override
    public List<PayWithdrawOrderDO> getWithdrawList(Long userId) {
        return withdrawOrderMapper.selectListByUserId(userId);
    }

    @Override
    public List<PayWithdrawOrderDO> getWithdrawListByStatus(Long userId, Integer status) {
        return withdrawOrderMapper.selectListByUserIdAndStatus(userId, status);
    }

    @Override
    public PayWithdrawOrderDO getWithdrawOrder(Long orderId, Long userId) {
        PayWithdrawOrderDO order = withdrawOrderMapper.selectById(orderId);
        if (order == null) {
            throw exception(WITHDRAW_ORDER_NOT_FOUND);
        }
        if (!order.getUserId().equals(userId)) {
            throw exception(WITHDRAW_ORDER_NOT_BELONG_TO_USER);
        }
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelWithdraw(Long orderId, Long userId) {
        // 1. 查询订单
        PayWithdrawOrderDO order = getWithdrawOrder(orderId, userId);

        // 2. 只有待审核状态可以取消
        if (!order.getStatus().equals(PayWithdrawStatusEnum.PENDING.getStatus())) {
            throw exception(WITHDRAW_CANNOT_CANCEL);
        }

        // 3. 更新订单状态为审核拒绝
        order.setStatus(PayWithdrawStatusEnum.REJECTED.getStatus());
        order.setAuditRemark("用户取消");
        order.setAuditTime(LocalDateTime.now());
        withdrawOrderMapper.updateById(order);

        // 4. 解冻金额
        walletService.unfreezePrice(order.getWalletId(), order.getAmount());

        log.info("[cancelWithdraw][用户({}) 取消提现订单({})]", userId, orderId);
    }

    @Override
    public Integer getTotalWithdrawAmount(Long userId) {
        Integer total = withdrawOrderMapper.selectTotalAmountByUserId(userId);
        return total != null ? total : 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditWithdraw(Long orderId, Boolean approved, String auditRemark, Long auditorId) {
        // 1. 查询订单
        PayWithdrawOrderDO order = withdrawOrderMapper.selectById(orderId);
        if (order == null) {
            throw exception(WITHDRAW_ORDER_NOT_FOUND);
        }

        // 2. 只有待审核状态可以审核
        if (!order.getStatus().equals(PayWithdrawStatusEnum.PENDING.getStatus())) {
            throw exception(WITHDRAW_ALREADY_AUDITED);
        }

        // 3. 更新订单状态
        if (approved) {
            // 审核通过，进入提现中状态
            order.setStatus(PayWithdrawStatusEnum.PROCESSING.getStatus());
            order.setAuditRemark(auditRemark);
            order.setAuditorId(auditorId);
            order.setAuditTime(LocalDateTime.now());
            withdrawOrderMapper.updateById(order);

            // TODO: 调用银行打款接口（这里模拟自动成功）
            completeWithdraw(orderId, true, null);

        } else {
            // 审核拒绝
            order.setStatus(PayWithdrawStatusEnum.REJECTED.getStatus());
            order.setAuditRemark(auditRemark);
            order.setAuditorId(auditorId);
            order.setAuditTime(LocalDateTime.now());
            withdrawOrderMapper.updateById(order);

            // 解冻金额
            walletService.unfreezePrice(order.getWalletId(), order.getAmount());
        }

        log.info("[auditWithdraw][审核提现订单({}) 结果({}) 审核人({})]", orderId, approved, auditorId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeWithdraw(Long orderId, Boolean success, String failReason) {
        // 1. 查询订单
        PayWithdrawOrderDO order = withdrawOrderMapper.selectById(orderId);
        if (order == null) {
            throw exception(WITHDRAW_ORDER_NOT_FOUND);
        }

        // 2. 必须是审核通过或提现中状态
        if (!order.getStatus().equals(PayWithdrawStatusEnum.APPROVED.getStatus())
                && !order.getStatus().equals(PayWithdrawStatusEnum.PROCESSING.getStatus())) {
            throw exception(WITHDRAW_STATUS_INVALID);
        }

        // 3. 更新订单状态
        if (success) {
            // 提现成功
            order.setStatus(PayWithdrawStatusEnum.SUCCESS.getStatus());
            order.setCompleteTime(LocalDateTime.now());
            withdrawOrderMapper.updateById(order);

            // 扣减余额（解冻+扣款）
            walletService.unfreezePrice(order.getWalletId(), order.getAmount());
            walletService.reduceWalletBalance(order.getWalletId(), order.getId(),
                    PayWalletBizTypeEnum.WITHDRAW, order.getAmount());

            log.info("[completeWithdraw][提现成功 订单({}) 金额({})分]", orderId, order.getAmount());

        } else {
            // 提现失败
            order.setStatus(PayWithdrawStatusEnum.FAILED.getStatus());
            order.setFailReason(failReason);
            order.setCompleteTime(LocalDateTime.now());
            withdrawOrderMapper.updateById(order);

            // 解冻金额
            walletService.unfreezePrice(order.getWalletId(), order.getAmount());

            log.warn("[completeWithdraw][提现失败 订单({}) 原因({})]", orderId, failReason);
        }
    }

    // ==================== 管理端方法实现 ====================

    @Override
    public PageResult<PayWithdrawRespVO> getWithdrawPage(PayWithdrawPageReqVO pageReqVO) {
        // 1. 如果按昵称搜索，先获取匹配的用户ID列表
        if (pageReqVO.getNickname() != null && !pageReqVO.getNickname().isEmpty()) {
            List<MemberUserRespDTO> users = memberUserApi.getUserListByNickname(pageReqVO.getNickname()).getCheckedData();
            if (CollUtil.isEmpty(users)) {
                // 没有匹配的用户，返回空结果
                return PageResult.empty();
            }
            // 只查询这些用户的提现订单
            List<Long> userIds = users.stream().map(MemberUserRespDTO::getId).collect(Collectors.toList());
            // 临时设置userId过滤（如果pageReqVO.getUserId()为null）
            if (pageReqVO.getUserId() == null && userIds.size() == 1) {
                pageReqVO.setUserId(userIds.get(0));
            }
        }

        // 2. 分页查询提现订单
        PageResult<PayWithdrawOrderDO> pageResult = withdrawOrderMapper.selectPage(pageReqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return PageResult.empty(pageResult.getTotal());
        }

        // 3. 获取用户信息和审核人信息
        List<Long> userIds = pageResult.getList().stream()
                .map(PayWithdrawOrderDO::getUserId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, MemberUserRespDTO> userMap = memberUserApi.getUserMap(userIds);

        List<Long> auditorIds = pageResult.getList().stream()
                .map(PayWithdrawOrderDO::getAuditorId)
                .filter(id -> id != null && id > 0)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, AdminUserRespDTO> auditorMap = auditorIds.isEmpty() ?
                Map.of() : adminUserApi.getUserMap(auditorIds);

        // 4. 获取钱包余额信息
        List<Long> walletIds = pageResult.getList().stream()
                .map(PayWithdrawOrderDO::getWalletId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, PayWalletDO> walletMap = walletIds.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> walletService.getWallet(id)
                ));

        // 5. 转换为VO并补充信息
        List<PayWithdrawRespVO> respList = new ArrayList<>();
        for (PayWithdrawOrderDO order : pageResult.getList()) {
            PayWithdrawRespVO respVO = BeanUtils.toBean(order, PayWithdrawRespVO.class);

            // 补充用户信息
            MemberUserRespDTO user = userMap.get(order.getUserId());
            if (user != null) {
                respVO.setNickname(user.getNickname());
                respVO.setMobile(user.getMobile());
            }

            // 补充审核人信息
            if (order.getAuditorId() != null && order.getAuditorId() > 0) {
                AdminUserRespDTO auditor = auditorMap.get(order.getAuditorId());
                if (auditor != null) {
                    respVO.setAuditorName(auditor.getNickname());
                }
            }

            // 补充钱包余额
            PayWalletDO wallet = walletMap.get(order.getWalletId());
            if (wallet != null) {
                respVO.setWalletBalance(wallet.getBalance());
            }

            respList.add(respVO);
        }

        return new PageResult<>(respList, pageResult.getTotal());
    }

    @Override
    public PayWithdrawRespVO getWithdrawDetail(Long orderId) {
        // 1. 查询订单
        PayWithdrawOrderDO order = withdrawOrderMapper.selectById(orderId);
        if (order == null) {
            throw exception(WITHDRAW_ORDER_NOT_FOUND);
        }

        // 2. 转换为VO
        PayWithdrawRespVO respVO = BeanUtils.toBean(order, PayWithdrawRespVO.class);

        // 3. 补充用户信息
        MemberUserRespDTO user = memberUserApi.getUser(order.getUserId()).getCheckedData();
        if (user != null) {
            respVO.setNickname(user.getNickname());
            respVO.setMobile(user.getMobile());
        }

        // 4. 补充审核人信息
        if (order.getAuditorId() != null && order.getAuditorId() > 0) {
            AdminUserRespDTO auditor = adminUserApi.getUser(order.getAuditorId()).getCheckedData();
            if (auditor != null) {
                respVO.setAuditorName(auditor.getNickname());
            }
        }

        // 5. 补充钱包余额
        PayWalletDO wallet = walletService.getWallet(order.getWalletId());
        if (wallet != null) {
            respVO.setWalletBalance(wallet.getBalance());
        }

        return respVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchAuditWithdraw(List<Long> orderIds, Boolean approved, String auditRemark, Long auditorId) {
        if (CollUtil.isEmpty(orderIds)) {
            return;
        }

        // 批量审核，逐个处理
        for (Long orderId : orderIds) {
            try {
                auditWithdraw(orderId, approved, auditRemark, auditorId);
            } catch (Exception e) {
                log.error("[batchAuditWithdraw][审核订单({})失败]", orderId, e);
                // 继续处理下一个订单，不中断整个批量操作
            }
        }

        log.info("[batchAuditWithdraw][批量审核 数量({}) 结果({}) 审核人({})]",
                orderIds.size(), approved, auditorId);
    }

    @Override
    public PayWithdrawStatisticsRespVO getAuditStatistics() {
        LocalDate today = LocalDate.now();

        PayWithdrawStatisticsRespVO statistics = new PayWithdrawStatisticsRespVO();

        // 今日统计
        statistics.setTodayPendingCount(withdrawOrderMapper.selectTodayPendingCount(today));
        statistics.setTodayAuditedCount(withdrawOrderMapper.selectTodayAuditedCount(today));
        statistics.setTodayApprovedCount(withdrawOrderMapper.selectTodayApprovedCount(today));
        statistics.setTodayRejectedCount(withdrawOrderMapper.selectTodayRejectedCount(today));
        statistics.setTodayApprovedAmount(withdrawOrderMapper.selectTodayApprovedAmount(today));

        // 历史统计
        statistics.setTotalPendingCount(withdrawOrderMapper.selectTotalPendingCount());
        statistics.setTotalPendingAmount(withdrawOrderMapper.selectTotalPendingAmount());

        return statistics;
    }

}

