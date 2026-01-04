package cn.iocoder.yudao.module.pay.service.reward;

import cn.iocoder.yudao.module.pay.controller.app.reward.vo.AppRewardCreateReqVO;
import cn.iocoder.yudao.module.pay.dal.dataobject.reward.PayRewardOrderDO;

import java.util.List;

/**
 * 打赏服务 Service 接口
 *
 * @author xiaolvshu
 */
public interface PayRewardService {

    /**
     * 创建打赏订单
     *
     * @param userId   用户ID
     * @param createReqVO 创建请求
     * @return 打赏订单ID
     */
    Long createRewardOrder(Long userId, AppRewardCreateReqVO createReqVO);

    /**
     * 查询用户的打赏记录
     *
     * @param userId 用户ID
     * @return 打赏记录列表
     */
    List<PayRewardOrderDO> getMyRewardList(Long userId);

    /**
     * 查询收到的打赏记录
     *
     * @param authorId 作者ID
     * @return 打赏记录列表
     */
    List<PayRewardOrderDO> getReceivedRewardList(Long authorId);

    /**
     * 查询打赏订单详情
     *
     * @param orderId 订单ID
     * @return 打赏订单
     */
    PayRewardOrderDO getRewardOrder(Long orderId);

    /**
     * 统计用户打赏总金额
     *
     * @param userId 用户ID
     * @return 打赏总金额（分）
     */
    Integer getTotalRewardAmount(Long userId);

    /**
     * 统计作者收益总金额
     *
     * @param authorId 作者ID
     * @return 收益总金额（分）
     */
    Integer getTotalIncomeAmount(Long authorId);

    /**
     * 统计指定内容的打赏总金额
     *
     * @param postId 作品ID (targetId)
     * @return 打赏总金额（分）
     */
    Integer getTotalRewardAmountByPost(Long postId);

}
