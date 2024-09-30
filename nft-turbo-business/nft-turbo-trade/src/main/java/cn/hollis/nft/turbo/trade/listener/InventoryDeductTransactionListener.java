package cn.hollis.nft.turbo.trade.listener;

import cn.hollis.nft.turbo.api.collection.request.InventoryRequest;
import cn.hollis.nft.turbo.api.collection.service.CollectionFacadeService;
import cn.hollis.nft.turbo.api.order.request.OrderCreateRequest;
import cn.hollis.nft.turbo.base.response.SingleResponse;
import com.alibaba.fastjson2.JSON;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Hollis
 */
@Component
public class InventoryDeductTransactionListener implements TransactionListener {

    private static final Logger logger = LoggerFactory.getLogger(InventoryDeductTransactionListener.class);

    @Autowired
    private CollectionFacadeService collectionFacadeService;

    @Override
    public LocalTransactionState executeLocalTransaction(Message message, Object o) {
        try {
            OrderCreateRequest orderCreateRequest = JSON.parseObject(JSON.parseObject(message.getBody()).getString("body"), OrderCreateRequest.class);

            InventoryRequest inventoryRequest = new InventoryRequest(orderCreateRequest);

            //预扣减
            SingleResponse<Boolean> response = collectionFacadeService.preInventoryDeduct(inventoryRequest);

            if (response.getSuccess()) {
                //todo 旁路验证，解决MQ或者应用挂了，导致消息丢失的问题。
                return LocalTransactionState.COMMIT_MESSAGE;
            } else {
                return LocalTransactionState.ROLLBACK_MESSAGE;
            }
        } catch (Exception e) {
            logger.error("executeLocalTransaction error, message = {}", message, e);
            return LocalTransactionState.ROLLBACK_MESSAGE;
        }
    }

    @Override
    public LocalTransactionState checkLocalTransaction(MessageExt messageExt) {
        OrderCreateRequest orderCreateRequest = JSON.parseObject(JSON.parseObject(new String(messageExt.getBody())).getString("body"), OrderCreateRequest.class);

        InventoryRequest inventoryRequest = new InventoryRequest(orderCreateRequest);
        SingleResponse<String> response = collectionFacadeService.getInventoryDecreaseLog(inventoryRequest);
        return response.getSuccess() && response.getData() != null ? LocalTransactionState.COMMIT_MESSAGE : LocalTransactionState.ROLLBACK_MESSAGE;
    }
}
