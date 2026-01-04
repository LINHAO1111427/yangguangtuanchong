package cn.iocoder.yudao.module.pay.dal.mysql.reward;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.pay.dal.dataobject.reward.PayRewardOrderDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 打赏订单 Mapper
 *
 * @author xiaolvshu
 */
@Mapper
public interface PayRewardOrderMapper extends BaseMapperX<PayRewardOrderDO> {

    /**
     * 查询用户的打赏记录列表
     *
     * @param userId 用户ID
     * @return 打赏记录列表
     */
    default List<PayRewardOrderDO> selectListByUserId(Long userId) {
        return selectList(new LambdaQueryWrapperX<PayRewardOrderDO>()
                .eq(PayRewardOrderDO::getUserId, userId)
                .eq(PayRewardOrderDO::getPayStatus, 1) // 只查询已支付的
                .orderByDesc(PayRewardOrderDO::getPayTime));
    }

    /**
     * 查询作者收到的打赏记录列表
     *
     * @param authorId 作者ID
     * @return 打赏记录列表
     */
    default List<PayRewardOrderDO> selectListByAuthorId(Long authorId) {
        return selectList(new LambdaQueryWrapperX<PayRewardOrderDO>()
                .eq(PayRewardOrderDO::getAuthorId, authorId)
                .eq(PayRewardOrderDO::getPayStatus, 1) // 只查询已支付的
                .orderByDesc(PayRewardOrderDO::getPayTime));
    }

    /**
     * 统计用户打赏总金额
     *
     * @param userId 用户ID
     * @return 打赏总金额（分）
     */
    default Integer selectTotalAmountByUserId(Long userId) {
        return selectList(new LambdaQueryWrapperX<PayRewardOrderDO>()
                        .eq(PayRewardOrderDO::getUserId, userId)
                        .eq(PayRewardOrderDO::getPayStatus, 1))
                .stream()
                .mapToInt(PayRewardOrderDO::getAmount)
                .sum();
    }

    /**
     * 统计作者收益总金额
     *
     * @param authorId 作者ID
     * @return 收益总金额（分）
     */
    default Integer selectTotalIncomeByAuthorId(Long authorId) {
        return selectList(new LambdaQueryWrapperX<PayRewardOrderDO>()
                        .eq(PayRewardOrderDO::getAuthorId, authorId)
                        .eq(PayRewardOrderDO::getPayStatus, 1))
                .stream()
                .mapToInt(PayRewardOrderDO::getIncomeAmount)
                .sum();
    }

    /**
     * 查询指定内容的打赏记录
     *
     * @param targetId   内容ID
     * @param rewardType 打赏类型
     * @return 打赏记录列表
     */
    default List<PayRewardOrderDO> selectListByTarget(Long targetId, Integer rewardType) {
        return selectList(new LambdaQueryWrapperX<PayRewardOrderDO>()
                .eq(PayRewardOrderDO::getTargetId, targetId)
                .eq(PayRewardOrderDO::getRewardType, rewardType)
                .eq(PayRewardOrderDO::getPayStatus, 1)
                .orderByDesc(PayRewardOrderDO::getPayTime));
    }

    /**
     * 统计指定内容的打赏总金额
     *
     * @param targetId   内容ID (postId)
     * @param rewardType 打赏类型 (1-内容打赏)
     * @return 打赏总金额（分）
     */
    default Integer selectTotalAmountByTarget(Long targetId, Integer rewardType) {
        return selectList(new LambdaQueryWrapperX<PayRewardOrderDO>()
                        .eq(PayRewardOrderDO::getTargetId, targetId)
                        .eq(PayRewardOrderDO::getRewardType, rewardType)
                        .eq(PayRewardOrderDO::getPayStatus, 1))
                .stream()
                .mapToInt(PayRewardOrderDO::getAmount)
                .sum();
    }

}
