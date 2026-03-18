package cn.gideon.nft.turbo.api.pay.constant;

/**
 * @author Gideon
 */
public enum PayRefundOrderState {

    /**
     * 待退款
     */
    TO_REFUND,

    /**
     * 退款中
     */
    REFUNDING,

    /**
     * 已退款
     */
    REFUNDED;
}
