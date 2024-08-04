package cn.hollis.nft.turbo.collection.listener;

import cn.hollis.nft.turbo.api.order.OrderFacadeService;
import cn.hollis.nft.turbo.api.order.constant.TradeOrderEvent;
import cn.hollis.nft.turbo.api.order.constant.TradeOrderState;
import cn.hollis.nft.turbo.api.order.model.TradeOrderVO;
import cn.hollis.nft.turbo.api.order.request.BaseOrderUpdateRequest;
import cn.hollis.nft.turbo.api.order.request.OrderCancelRequest;
import cn.hollis.nft.turbo.api.order.request.OrderTimeoutRequest;
import cn.hollis.nft.turbo.base.response.SingleResponse;
import cn.hollis.nft.turbo.collection.domain.request.CollectionInventoryRequest;
import cn.hollis.nft.turbo.collection.domain.response.CollectionInventoryResponse;
import cn.hollis.nft.turbo.collection.domain.service.CollectionInventoryService;
import cn.hollis.nft.turbo.collection.domain.service.CollectionService;
import cn.hollis.nft.turbo.collection.exception.CollectionException;
import cn.hollis.nft.turbo.collection.facade.request.CollectionCancelSaleRequest;
import cn.hollis.turbo.stream.param.MessageBody;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

import static cn.hollis.nft.turbo.collection.exception.CollectionErrorCode.COLLECTION_INVENTORY_UPDATE_FAILED;

/**
 * 交易订单监听器
 *
 * @author hollis
 */
@Slf4j
@Component
public class TradeOrderListener {

    @Autowired
    private CollectionInventoryService collectionInventoryService;

    @Autowired
    private CollectionService collectionService;

    @Autowired
    private OrderFacadeService orderFacadeService;

    @Bean
    Consumer<Message<MessageBody>> orderClose() {
        log.info("orderClose consumer init");
        return msg -> {
            String messageId = msg.getHeaders().get("ROCKET_MQ_MESSAGE_ID", String.class);
            String closeType = msg.getHeaders().get("CLOSE_TYPE", String.class);

            BaseOrderUpdateRequest orderCloseRequest;
            if (TradeOrderEvent.CANCEL.name().equals(closeType)) {
                orderCloseRequest = JSON.parseObject(msg.getPayload().getBody(), OrderCancelRequest.class);
            } else if (TradeOrderEvent.TIME_OUT.name().equals(closeType)) {
                orderCloseRequest = JSON.parseObject(msg.getPayload().getBody(), OrderTimeoutRequest.class);
            } else {
                throw new UnsupportedOperationException("unsupported closeType " + closeType);
            }

            log.info("received messageId:{},orderCloseRequest:{}，closeType:{}", messageId, JSON.toJSONString(orderCloseRequest), closeType);

            SingleResponse<TradeOrderVO> response = orderFacadeService.getTradeOrder(orderCloseRequest.getOrderId());
            if (response.getSuccess()) {
                TradeOrderVO tradeOrderVO = response.getData();
                if (response.getData().getOrderState() == TradeOrderState.CLOSED) {
                    CollectionCancelSaleRequest collectionCancelSaleRequest = new CollectionCancelSaleRequest(orderCloseRequest.getOrderId(), Long.valueOf(tradeOrderVO.getGoodsId()), tradeOrderVO.getItemCount().longValue());
                    Boolean cancelSaleResult = collectionService.cancelSale(collectionCancelSaleRequest);
                    if (cancelSaleResult) {
                        CollectionInventoryRequest collectionInventoryRequest = new CollectionInventoryRequest();
                        collectionInventoryRequest.setCollectionId(tradeOrderVO.getGoodsId());
                        collectionInventoryRequest.setInventory(tradeOrderVO.getItemCount());
                        collectionInventoryRequest.setIdentifier(orderCloseRequest.getOrderId());
                        CollectionInventoryResponse decreaseResponse = collectionInventoryService.increase(collectionInventoryRequest);
                        if (decreaseResponse.getSuccess()) {
                            log.info("decrease success,collectionInventoryRequest:{}", collectionInventoryRequest);
                            return;
                        } else {
                            log.error("increase inventory failed,orderCloseRequest:{} , decreaseResponse : {}", JSON.toJSONString(orderCloseRequest), JSON.toJSONString(decreaseResponse));
                        }
                    } else {
                        log.error("cancelSale failed,orderCloseRequest:{} , collectionSaleResponse : {}", JSON.toJSONString(orderCloseRequest), JSON.toJSONString(cancelSaleResult));
                    }
                } else {
                    log.error("trade order state is illegal ,orderCloseRequest:{} , tradeOrderVO : {}", JSON.toJSONString(orderCloseRequest), JSON.toJSONString(tradeOrderVO));
                }
            } else {
                log.error("getTradeOrder failed,orderCloseRequest:{} , orderQueryResponse : {}", JSON.toJSONString(orderCloseRequest), JSON.toJSONString(response));
            }
            throw new CollectionException(COLLECTION_INVENTORY_UPDATE_FAILED);
        };
    }
}
