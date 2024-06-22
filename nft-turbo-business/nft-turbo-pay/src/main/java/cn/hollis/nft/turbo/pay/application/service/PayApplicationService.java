package cn.hollis.nft.turbo.pay.application.service;

import cn.hollis.nft.turbo.api.collection.constant.CollectionSaleBizType;
import cn.hollis.nft.turbo.api.collection.request.CollectionSaleRequest;
import cn.hollis.nft.turbo.api.collection.response.CollectionSaleResponse;
import cn.hollis.nft.turbo.api.collection.service.CollectionFacadeService;
import cn.hollis.nft.turbo.api.order.OrderFacadeService;
import cn.hollis.nft.turbo.api.order.constant.OrderErrorCode;
import cn.hollis.nft.turbo.api.order.model.TradeOrderVO;
import cn.hollis.nft.turbo.api.order.request.OrderPayRequest;
import cn.hollis.nft.turbo.api.order.response.OrderResponse;
import cn.hollis.nft.turbo.base.response.SingleResponse;
import cn.hollis.nft.turbo.base.utils.RemoteCallWrapper;
import cn.hollis.nft.turbo.pay.domain.entity.PayOrder;
import cn.hollis.nft.turbo.pay.domain.event.PaySuccessEvent;
import cn.hollis.nft.turbo.pay.domain.service.PayOrderService;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Hollis
 */
@Service
@Slf4j
public class PayApplicationService {

    @Autowired
    private PayOrderService payOrderService;

    @Autowired
    private OrderFacadeService orderFacadeService;

    @Autowired
    private CollectionFacadeService collectionFacadeService;

    /**
     * 支付成功
     * <pre>
     *     正常支付成功：
     *     1、查询订单状态
     *     2、推进订单状态到支付成功
     *     3、推进支付状态到支付成功
     *
     *     支付幂等成功：
     *      1、查询订单状态
     *      2、推进支付状态到支付成功
     *
     *      重复支付：
     *      1、查询订单状态
     *      2、创建退款单
     *      3、重试退款直到成功
     * </pre>
     */
    public boolean paySuccess(PaySuccessEvent paySuccessEvent) {

        //todo 使用Seata 改造成分布式事务
        PayOrder payOrder = payOrderService.queryByOrderId(paySuccessEvent.getPayOrderId());
        if (payOrder.isPaid()) {
            return true;
        }

        SingleResponse<TradeOrderVO> response = orderFacadeService.getTradeOrder(payOrder.getBizNo());
        TradeOrderVO tradeOrderVO = response.getData();

        OrderPayRequest orderPayRequest = getOrderPayRequest(paySuccessEvent, payOrder);
        OrderResponse orderResponse = RemoteCallWrapper.call(req -> orderFacadeService.pay(req), orderPayRequest, "orderFacadeService.pay");

        if (orderResponse.getResponseCode() != null && orderResponse.getResponseCode().equals(OrderErrorCode.ORDER_ALREADY_PAID.getCode())) {
            doChargeBack(paySuccessEvent);
            return true;
        }

        if (!orderResponse.getSuccess()) {
            log.error("orderFacadeService.pay error, response = {}", JSON.toJSONString(orderResponse));
            return false;
        }

        CollectionSaleRequest collectionSaleRequest = getCollectionSaleRequest(tradeOrderVO);
        CollectionSaleResponse collectionSaleResponse = RemoteCallWrapper.call(req -> collectionFacadeService.confirmSale(req), collectionSaleRequest, "collectionFacadeService.sale");

        if (collectionSaleResponse.getSuccess()) {
            return payOrderService.paySuccess(paySuccessEvent);
        }

        return false;
    }

    private static CollectionSaleRequest getCollectionSaleRequest(TradeOrderVO tradeOrderVO) {
        CollectionSaleRequest collectionSaleRequest = new CollectionSaleRequest();
        collectionSaleRequest.setCollectionId(Long.valueOf(tradeOrderVO.getGoodsId()));
        collectionSaleRequest.setIdentifier(tradeOrderVO.getOrderId());
        collectionSaleRequest.setUserId(tradeOrderVO.getBuyerId());
        collectionSaleRequest.setQuantity((long) tradeOrderVO.getItemCount());
        collectionSaleRequest.setBizNo(tradeOrderVO.getOrderId());
        collectionSaleRequest.setBizType(CollectionSaleBizType.PRIMARY_TRADE.name());
        collectionSaleRequest.setName(tradeOrderVO.getGoodsName());
        collectionSaleRequest.setCover(tradeOrderVO.getGoodsPicUrl());
        collectionSaleRequest.setPurchasePrice(tradeOrderVO.getItemPrice());

        return collectionSaleRequest;
    }

    private static OrderPayRequest getOrderPayRequest(PaySuccessEvent paySuccessEvent, PayOrder payOrder) {
        OrderPayRequest orderPayRequest = new OrderPayRequest();
        orderPayRequest.setOperateTime(paySuccessEvent.getPaySucceedTime());
        orderPayRequest.setPayChannel(paySuccessEvent.getPayChannel());
        orderPayRequest.setPayStreamId(payOrder.getPayOrderId());
        orderPayRequest.setAmount(paySuccessEvent.getPaidAmount());
        orderPayRequest.setOrderId(payOrder.getBizNo());
        orderPayRequest.setOperatorType(payOrder.getPayerType());
        orderPayRequest.setOperator(payOrder.getPayerId());
        orderPayRequest.setIdentifier(payOrder.getBizNo());
        return orderPayRequest;
    }

    private void doChargeBack(PaySuccessEvent paySuccessEvent) {
        //todo
    }
}
