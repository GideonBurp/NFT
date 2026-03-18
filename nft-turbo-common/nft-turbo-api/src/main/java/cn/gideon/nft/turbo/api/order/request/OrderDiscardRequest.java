package cn.gideon.nft.turbo.api.order.request;

import cn.gideon.nft.turbo.api.order.constant.TradeOrderEvent;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Gideon
 */
@Getter
@Setter
public class OrderDiscardRequest extends BaseOrderUpdateRequest {

    @Override
    public TradeOrderEvent getOrderEvent() {
        return TradeOrderEvent.DISCARD;
    }
}

