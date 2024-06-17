package cn.hollis.nft.turbo.pay.domain.event;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Hollis
 */
@Getter
@Setter
public class PayRefundEvent {

    /**
     * 支付订单号
     */
    private String payOrderId;

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
}
