package cn.hollis.nft.turbo.order.domain.listener.event;

import cn.hollis.nft.turbo.api.order.request.BaseOrderRequest;
import cn.hollis.nft.turbo.order.domain.entity.TradeOrder;
import org.springframework.context.ApplicationEvent;

/**
 * @author Hollis
 */
public class OrderCreateEvent extends ApplicationEvent {

    public OrderCreateEvent(TradeOrder tradeOrder) {
        super(tradeOrder);
    }
}
