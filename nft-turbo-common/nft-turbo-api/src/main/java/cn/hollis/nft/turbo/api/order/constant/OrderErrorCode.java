package cn.hollis.nft.turbo.api.order.constant;

import cn.hollis.nft.turbo.base.exception.ErrorCode;

/**
 * @author Hollis
 */
public enum OrderErrorCode implements ErrorCode {
    /**
     * 订单不存在
     */
    ORDER_NOT_EXIST("ORDER_NOT_EXIST", "订单不存在"),

    /**
     * 无权限操作
     */
    PERMISSION_DENIED("PERMISSION_DENIED", "无权限操作"),

    /**
     * 更新订单失败
     */
    UPDATE_ORDER_FAILED("UPDATE_ORDER_FAILED", "更新订单失败"),

    /**
     * 订单已支付
     */
    ORDER_ALREADY_PAID("ORDER_ALREADY_PAID", "订单已支付"),

    /**
     * 订单状态转移非法
     */
    ORDER_STATE_TRANSFER_ILLEGAL("ORDER_STATE_TRANSFER_ILLEGAL", "订单状态转移非法"),

    /**
     * 库存扣件失败
     */
    INVENTORY_DEDUCT_FAILED("INVENTORY_DEDUCT_FAILED", "库存扣减失败"),

    /**
     * 订单创建校验失败
     */
    ORDER_CREATE_VALID_FAILED("ORDER_CREATE_VALID_FAILED", "订单创建校验失败"),
    /**
     * 订单已过期
     */
    ORDER_IS_EXPIRED("OEDER_IS_EXPIRED", "订单已过期");

    private String code;

    private String message;

    OrderErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
