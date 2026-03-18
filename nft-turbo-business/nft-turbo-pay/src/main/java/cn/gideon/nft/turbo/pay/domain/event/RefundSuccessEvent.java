package cn.gideon.nft.turbo.pay.domain.event;

import cn.gideon.nft.turbo.api.pay.constant.PayChannel;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Gideon
 */
@Getter
@Setter
public class RefundSuccessEvent {

    /**
     * 支付单号
     */
    private String payOrderId;

    /**
     * 退款单号
     */
    private String refundOrderId;

    /**
     * 退款成功时间
     */
    private Date refundedTime;

    /**
     * 渠道流水号
     */
    private String channelStreamId;

    /**
     * 退款金额
     */
    private BigDecimal refundedAmount;

    /**
     * 退款渠道
     */
    private PayChannel refundChannel;
}
