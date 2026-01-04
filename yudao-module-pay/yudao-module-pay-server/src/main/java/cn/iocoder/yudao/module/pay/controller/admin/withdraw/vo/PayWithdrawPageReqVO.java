package cn.iocoder.yudao.module.pay.controller.admin.withdraw.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 管理后台 - 提现订单分页 Request VO
 *
 * @author xiaolvshu
 */
@Schema(description = "管理后台 - 提现订单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PayWithdrawPageReqVO extends PageParam {

    @Schema(description = "用户ID", example = "123456")
    private Long userId;

    @Schema(description = "用户昵称（模糊查询）", example = "张三")
    private String nickname;

    @Schema(description = "提现状态 0-待审核 1-审核通过 2-审核拒绝 3-提现中 4-提现成功 5-提现失败", example = "0")
    private Integer status;

    @Schema(description = "银行卡号（模糊查询）", example = "6222")
    private String bankCardNo;

    @Schema(description = "开户名（模糊查询）", example = "张三")
    private String accountName;

    @Schema(description = "最小金额（分）", example = "1000")
    private Integer minAmount;

    @Schema(description = "最大金额（分）", example = "100000")
    private Integer maxAmount;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @Schema(description = "创建时间")
    private LocalDateTime[] createTime;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @Schema(description = "审核时间")
    private LocalDateTime[] auditTime;

}
