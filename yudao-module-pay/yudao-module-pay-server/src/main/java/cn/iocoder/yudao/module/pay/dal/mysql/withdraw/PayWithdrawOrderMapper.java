package cn.iocoder.yudao.module.pay.dal.mysql.withdraw;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.pay.controller.admin.withdraw.vo.PayWithdrawPageReqVO;
import cn.iocoder.yudao.module.pay.dal.dataobject.withdraw.PayWithdrawOrderDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.util.List;

/**
 * 提现订单 Mapper
 *
 * @author xiaolvshu
 */
@Mapper
public interface PayWithdrawOrderMapper extends BaseMapperX<PayWithdrawOrderDO> {

    /**
     * 查询用户的提现记录列表
     *
     * @param userId 用户ID
     * @return 提现记录列表
     */
    default List<PayWithdrawOrderDO> selectListByUserId(Long userId) {
        return selectList(new LambdaQueryWrapperX<PayWithdrawOrderDO>()
                .eq(PayWithdrawOrderDO::getUserId, userId)
                .orderByDesc(PayWithdrawOrderDO::getCreateTime));
    }

    /**
     * 查询用户指定状态的提现记录
     *
     * @param userId 用户ID
     * @param status 状态
     * @return 提现记录列表
     */
    default List<PayWithdrawOrderDO> selectListByUserIdAndStatus(Long userId, Integer status) {
        return selectList(new LambdaQueryWrapperX<PayWithdrawOrderDO>()
                .eq(PayWithdrawOrderDO::getUserId, userId)
                .eq(PayWithdrawOrderDO::getStatus, status)
                .orderByDesc(PayWithdrawOrderDO::getCreateTime));
    }

    /**
     * 统计用户今日提现次数
     *
     * @param userId 用户ID
     * @param date   日期
     * @return 提现次数
     */
    default Long selectTodayCountByUserId(Long userId, LocalDate date) {
        return selectCount(new LambdaQueryWrapperX<PayWithdrawOrderDO>()
                .eq(PayWithdrawOrderDO::getUserId, userId)
                .ge(PayWithdrawOrderDO::getCreateTime, date.atStartOfDay())
                .lt(PayWithdrawOrderDO::getCreateTime, date.plusDays(1).atStartOfDay()));
    }

    /**
     * 查询待审核的提现订单列表
     *
     * @return 待审核订单列表
     */
    default List<PayWithdrawOrderDO> selectPendingList() {
        return selectList(new LambdaQueryWrapperX<PayWithdrawOrderDO>()
                .eq(PayWithdrawOrderDO::getStatus, 0) // 0-待审核
                .orderByAsc(PayWithdrawOrderDO::getCreateTime));
    }

    /**
     * 统计用户提现成功总金额
     *
     * @param userId 用户ID
     * @return 提现总金额（分）
     */
    default Integer selectTotalAmountByUserId(Long userId) {
        return selectList(new LambdaQueryWrapperX<PayWithdrawOrderDO>()
                        .eq(PayWithdrawOrderDO::getUserId, userId)
                        .eq(PayWithdrawOrderDO::getStatus, 4)) // 4-提现成功
                .stream()
                .mapToInt(PayWithdrawOrderDO::getAmount)
                .sum();
    }

    /**
     * 查询用户进行中的提现订单（待审核+审核通过+提现中）
     *
     * @param userId 用户ID
     * @return 进行中订单列表
     */
    default List<PayWithdrawOrderDO> selectInProgressByUserId(Long userId) {
        return selectList(new LambdaQueryWrapperX<PayWithdrawOrderDO>()
                .eq(PayWithdrawOrderDO::getUserId, userId)
                .in(PayWithdrawOrderDO::getStatus, 0, 1, 3) // 0-待审核 1-审核通过 3-提现中
                .orderByDesc(PayWithdrawOrderDO::getCreateTime));
    }

    /**
     * 管理端分页查询提现订单
     * 注意：nickname字段需要在Service层联查member_user表获取
     *
     * @param reqVO 分页查询条件
     * @return 分页结果
     */
    default PageResult<PayWithdrawOrderDO> selectPage(PayWithdrawPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<PayWithdrawOrderDO>()
                .eqIfPresent(PayWithdrawOrderDO::getUserId, reqVO.getUserId())
                .eqIfPresent(PayWithdrawOrderDO::getStatus, reqVO.getStatus())
                .likeIfPresent(PayWithdrawOrderDO::getBankCardNo, reqVO.getBankCardNo())
                .likeIfPresent(PayWithdrawOrderDO::getAccountName, reqVO.getAccountName())
                .geIfPresent(PayWithdrawOrderDO::getAmount, reqVO.getMinAmount())
                .leIfPresent(PayWithdrawOrderDO::getAmount, reqVO.getMaxAmount())
                .betweenIfPresent(PayWithdrawOrderDO::getCreateTime, reqVO.getCreateTime())
                .betweenIfPresent(PayWithdrawOrderDO::getAuditTime, reqVO.getAuditTime())
                .orderByDesc(PayWithdrawOrderDO::getCreateTime));
    }

    /**
     * 统计今日待审核数量
     *
     * @param date 日期
     * @return 待审核数量
     */
    default Long selectTodayPendingCount(LocalDate date) {
        return selectCount(new LambdaQueryWrapperX<PayWithdrawOrderDO>()
                .eq(PayWithdrawOrderDO::getStatus, 0) // 0-待审核
                .ge(PayWithdrawOrderDO::getCreateTime, date.atStartOfDay())
                .lt(PayWithdrawOrderDO::getCreateTime, date.plusDays(1).atStartOfDay()));
    }

    /**
     * 统计今日已审核数量
     *
     * @param date 日期
     * @return 已审核数量
     */
    default Long selectTodayAuditedCount(LocalDate date) {
        return selectCount(new LambdaQueryWrapperX<PayWithdrawOrderDO>()
                .in(PayWithdrawOrderDO::getStatus, 1, 2) // 1-审核通过 2-审核拒绝
                .ge(PayWithdrawOrderDO::getAuditTime, date.atStartOfDay())
                .lt(PayWithdrawOrderDO::getAuditTime, date.plusDays(1).atStartOfDay()));
    }

    /**
     * 统计今日审核通过数量
     *
     * @param date 日期
     * @return 审核通过数量
     */
    default Long selectTodayApprovedCount(LocalDate date) {
        return selectCount(new LambdaQueryWrapperX<PayWithdrawOrderDO>()
                .eq(PayWithdrawOrderDO::getStatus, 1) // 1-审核通过
                .ge(PayWithdrawOrderDO::getAuditTime, date.atStartOfDay())
                .lt(PayWithdrawOrderDO::getAuditTime, date.plusDays(1).atStartOfDay()));
    }

    /**
     * 统计今日审核拒绝数量
     *
     * @param date 日期
     * @return 审核拒绝数量
     */
    default Long selectTodayRejectedCount(LocalDate date) {
        return selectCount(new LambdaQueryWrapperX<PayWithdrawOrderDO>()
                .eq(PayWithdrawOrderDO::getStatus, 2) // 2-审核拒绝
                .ge(PayWithdrawOrderDO::getAuditTime, date.atStartOfDay())
                .lt(PayWithdrawOrderDO::getAuditTime, date.plusDays(1).atStartOfDay()));
    }

    /**
     * 统计今日审核通过金额
     *
     * @param date 日期
     * @return 审核通过金额（分）
     */
    default Long selectTodayApprovedAmount(LocalDate date) {
        List<PayWithdrawOrderDO> list = selectList(new LambdaQueryWrapperX<PayWithdrawOrderDO>()
                .eq(PayWithdrawOrderDO::getStatus, 1) // 1-审核通过
                .ge(PayWithdrawOrderDO::getAuditTime, date.atStartOfDay())
                .lt(PayWithdrawOrderDO::getAuditTime, date.plusDays(1).atStartOfDay()));
        return list.stream()
                .mapToLong(PayWithdrawOrderDO::getAmount)
                .sum();
    }

    /**
     * 统计历史待审核数量
     *
     * @return 待审核数量
     */
    default Long selectTotalPendingCount() {
        return selectCount(new LambdaQueryWrapperX<PayWithdrawOrderDO>()
                .eq(PayWithdrawOrderDO::getStatus, 0)); // 0-待审核
    }

    /**
     * 统计历史待审核金额
     *
     * @return 待审核金额（分）
     */
    default Long selectTotalPendingAmount() {
        List<PayWithdrawOrderDO> list = selectList(new LambdaQueryWrapperX<PayWithdrawOrderDO>()
                .eq(PayWithdrawOrderDO::getStatus, 0)); // 0-待审核
        return list.stream()
                .mapToLong(PayWithdrawOrderDO::getAmount)
                .sum();
    }

}
