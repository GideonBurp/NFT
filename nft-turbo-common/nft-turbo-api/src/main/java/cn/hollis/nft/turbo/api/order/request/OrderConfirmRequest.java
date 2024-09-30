package cn.hollis.nft.turbo.api.order.request;

import cn.hollis.nft.turbo.api.order.constant.TradeOrderEvent;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Hollis
 */
@Getter
@Setter
public class OrderConfirmRequest extends BaseOrderUpdateRequest {

    /**
     * 买家Id
     */
    private String buyerId;

    /**
     * 藏品Id
     */
    private Long collectionId;

    /**
     * 数量
     */
    private Long itemCount;

    @Override
    public TradeOrderEvent getOrderEvent() {
        return TradeOrderEvent.CONFIRM;
    }
}

