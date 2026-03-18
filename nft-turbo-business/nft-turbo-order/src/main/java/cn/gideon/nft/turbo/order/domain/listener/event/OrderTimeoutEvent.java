package cn.gideon.nft.turbo.order.domain.listener.event;

import cn.gideon.nft.turbo.api.order.request.BaseOrderRequest;
import org.springframework.context.ApplicationEvent;

/**
 * @author Gideon
 */
public class OrderTimeoutEvent extends ApplicationEvent {

    public OrderTimeoutEvent(BaseOrderRequest baseOrderRequest) {
        super(baseOrderRequest);
    }
}
