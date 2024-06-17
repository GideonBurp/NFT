package cn.hollis.nft.turbo.order.domain.validator;

import cn.hollis.nft.turbo.api.order.request.OrderCreateRequest;

/**
 * 订单校验
 *
 * @author Hollis
 */
public interface OrderCreateValidator {
    /**
     * 设置下一个校验器
     *
     * @param nextValidator
     */
    void setNext(OrderCreateValidator nextValidator);

    /**
     * 校验
     *
     * @param request
     * @throws Exception
     */
    void validate(OrderCreateRequest request) throws Exception;
}
