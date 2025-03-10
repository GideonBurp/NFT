package cn.hollis.nft.turbo.order.facade;

import cn.hollis.nft.turbo.api.order.OrderTransactionFacadeService;
import cn.hollis.nft.turbo.api.order.constant.OrderErrorCode;
import cn.hollis.nft.turbo.api.order.request.OrderCancelRequest;
import cn.hollis.nft.turbo.api.order.request.OrderConfirmRequest;
import cn.hollis.nft.turbo.api.order.request.OrderCreateRequest;
import cn.hollis.nft.turbo.api.order.request.OrderDiscardRequest;
import cn.hollis.nft.turbo.api.order.response.OrderResponse;
import cn.hollis.nft.turbo.base.exception.BizException;
import cn.hollis.nft.turbo.order.domain.service.OrderManageService;
import cn.hollis.nft.turbo.rpc.facade.Facade;
import cn.hollis.nft.turbo.tcc.entity.TransCancelSuccessType;
import cn.hollis.nft.turbo.tcc.request.TccRequest;
import cn.hollis.nft.turbo.tcc.response.TransactionCancelResponse;
import cn.hollis.nft.turbo.tcc.service.TransactionLogService;
import cn.hutool.core.lang.Assert;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Hollis
 */
@DubboService(version = "1.0.0")
public class OrderTransactionFacadeServiceImpl implements OrderTransactionFacadeService {

    @Autowired
    private OrderManageService orderManageService;

    @Autowired
    private TransactionLogService transactionLogService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Facade
    public OrderResponse tryOrder(OrderCreateRequest orderCreateRequest) {
        OrderResponse orderResponse = orderManageService.create(orderCreateRequest);
        Assert.isTrue(orderResponse.getSuccess(), () -> new BizException(OrderErrorCode.CREATE_ORDER_FAILED));

        Boolean result = transactionLogService.tryTransaction(new TccRequest(orderResponse.getOrderId(), "normalBuy", "ORDER"));
        Assert.isTrue(result, "transaction log failed");
        return orderResponse;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Facade
    public OrderResponse confirmOrder(OrderConfirmRequest orderConfirmRequest) {
        OrderResponse orderResponse = orderManageService.confirm(orderConfirmRequest);
        Assert.isTrue(orderResponse.getSuccess(), () -> new BizException(OrderErrorCode.CREATE_ORDER_FAILED));


        Boolean result = transactionLogService.confirmTransaction(new TccRequest(orderResponse.getOrderId(), "normalBuy", "ORDER"));
        Assert.isTrue(result, "transaction log failed");
        return orderResponse;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Facade
    public OrderResponse cancelOrder(OrderDiscardRequest orderDiscardRequest) {

        TransactionCancelResponse transactionCancelResponse = transactionLogService.cancelTransaction(new TccRequest(orderDiscardRequest.getOrderId(), "normalBuy", "ORDER"));
        Assert.isTrue(transactionCancelResponse.getSuccess(), "transaction log failed");

        //如果发生空回滚，或者回滚幂等，则不进行废弃订单操作
        if(transactionCancelResponse.getTransCancelSuccessType() == TransCancelSuccessType.CANCEL_AFTER_TRY_SUCCESS
                || transactionCancelResponse.getTransCancelSuccessType() == TransCancelSuccessType.CANCEL_AFTER_CONFIRM_SUCCESS){
            OrderResponse orderResponse = orderManageService.discard(orderDiscardRequest);
            Assert.isTrue(orderResponse.getSuccess(), () -> new BizException(OrderErrorCode.UPDATE_ORDER_FAILED));
            return orderResponse;
        }

        return new OrderResponse.OrderResponseBuilder().buildSuccess();
    }
}
