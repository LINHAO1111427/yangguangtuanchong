package cn.iocoder.yudao.module.pay.service.withdraw;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.pay.controller.admin.withdraw.vo.PayWithdrawPageReqVO;
import cn.iocoder.yudao.module.pay.controller.admin.withdraw.vo.PayWithdrawRespVO;
import cn.iocoder.yudao.module.pay.controller.admin.withdraw.vo.PayWithdrawStatisticsRespVO;
import cn.iocoder.yudao.module.pay.controller.app.withdraw.vo.AppWithdrawApplyReqVO;
import cn.iocoder.yudao.module.pay.dal.dataobject.withdraw.PayWithdrawOrderDO;

import java.util.List;

/**
 * 提现服务 Service 接口
 *
 * @author xiaolvshu
 */
public interface PayWithdrawService {

    /**
     * 申请提现
     *
     * @param userId   用户ID
     * @param applyReqVO 申请请求
     * @return 提现订单ID
     */
    Long applyWithdraw(Long userId, AppWithdrawApplyReqVO applyReqVO);

    /**
     * 查询用户的提现记录
     *
     * @param userId 用户ID
     * @return 提现记录列表
     */
    List<PayWithdrawOrderDO> getWithdrawList(Long userId);

    /**
     * 查询用户指定状态的提现记录
     *
     * @param userId 用户ID
     * @param status 状态
     * @return 提现记录列表
     */
    List<PayWithdrawOrderDO> getWithdrawListByStatus(Long userId, Integer status);

    /**
     * 查询提现订单详情
     *
     * @param orderId 订单ID
     * @param userId  用户ID
     * @return 提现订单
     */
    PayWithdrawOrderDO getWithdrawOrder(Long orderId, Long userId);

    /**
     * 取消提现（仅待审核状态可取消）
     *
     * @param orderId 订单ID
     * @param userId  用户ID
     */
    void cancelWithdraw(Long orderId, Long userId);

    /**
     * 统计用户提现成功总金额
     *
     * @param userId 用户ID
     * @return 提现总金额（分）
     */
    Integer getTotalWithdrawAmount(Long userId);

    /**
     * 审核提现订单（管理员操作）
     *
     * @param orderId      订单ID
     * @param approved     是否通过
     * @param auditRemark  审核备注
     * @param auditorId    审核人ID
     */
    void auditWithdraw(Long orderId, Boolean approved, String auditRemark, Long auditorId);

    /**
     * 提现完成（系统调用）
     *
     * @param orderId 订单ID
     * @param success 是否成功
     * @param failReason 失败原因
     */
    void completeWithdraw(Long orderId, Boolean success, String failReason);

    // ==================== 管理端方法 ====================

    /**
     * 管理端分页查询提现订单
     *
     * @param pageReqVO 分页查询条件
     * @return 分页结果（包含用户信息）
     */
    PageResult<PayWithdrawRespVO> getWithdrawPage(PayWithdrawPageReqVO pageReqVO);

    /**
     * 管理端获取提现订单详情
     *
     * @param orderId 订单ID
     * @return 提现订单详情（包含用户信息）
     */
    PayWithdrawRespVO getWithdrawDetail(Long orderId);

    /**
     * 批量审核提现订单（管理员操作）
     *
     * @param orderIds     订单ID列表
     * @param approved     是否通过
     * @param auditRemark  审核备注
     * @param auditorId    审核人ID
     */
    void batchAuditWithdraw(List<Long> orderIds, Boolean approved, String auditRemark, Long auditorId);

    /**
     * 获取提现审核统计数据
     *
     * @return 统计数据
     */
    PayWithdrawStatisticsRespVO getAuditStatistics();

}
