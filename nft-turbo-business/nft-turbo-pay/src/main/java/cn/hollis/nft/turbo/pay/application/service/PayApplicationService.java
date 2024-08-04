package cn.hollis.nft.turbo.pay.application.service;

import cn.hollis.nft.turbo.api.collection.constant.CollectionSaleBizType;
import cn.hollis.nft.turbo.api.collection.request.CollectionSaleRequest;
import cn.hollis.nft.turbo.api.collection.response.CollectionSaleResponse;
import cn.hollis.nft.turbo.api.collection.service.CollectionFacadeService;
import cn.hollis.nft.turbo.api.collection.request.CollectionCreateRequest;
import cn.hollis.nft.turbo.api.collection.service.CollectionManageFacadeService;
import cn.hollis.nft.turbo.api.common.constant.BizOrderType;
import cn.hollis.nft.turbo.api.goods.constant.GoodsType;
import cn.hollis.nft.turbo.api.order.OrderFacadeService;
import cn.hollis.nft.turbo.api.order.constant.OrderErrorCode;
import cn.hollis.nft.turbo.api.order.model.TradeOrderVO;
import cn.hollis.nft.turbo.api.order.request.OrderCreateRequest;
import cn.hollis.nft.turbo.api.order.request.OrderPayRequest;
import cn.hollis.nft.turbo.api.order.response.OrderResponse;
import cn.hollis.nft.turbo.api.pay.constant.PayChannel;
import cn.hollis.nft.turbo.api.pay.request.PayCreateRequest;
import cn.hollis.nft.turbo.base.response.SingleResponse;
import cn.hollis.nft.turbo.base.utils.RemoteCallWrapper;
import cn.hollis.nft.turbo.pay.domain.entity.PayOrder;
import cn.hollis.nft.turbo.pay.domain.event.PaySuccessEvent;
import cn.hollis.nft.turbo.pay.domain.service.PayOrderService;
import com.alibaba.fastjson.JSON;
import io.seata.spring.annotation.GlobalTransactional;
import io.seata.tm.api.transaction.TransactionHookManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

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

    @Autowired
    private CollectionManageFacadeService collectionManageFacadeService;

    /**
     * 用于测试Seata+ShardingJDBC
     */
    @GlobalTransactional(rollbackFor = Exception.class)
    public void test() {
        CollectionCreateRequest request = new CollectionCreateRequest();
        request.setIdentifier(String.valueOf(System.currentTimeMillis()));
        request.setName("测试藏品");
        request.setQuantity(100L);
        request.setSaleTime(new Date());
        request.setPrice(BigDecimal.TEN);
        request.setCover("https://t7.baidu.com/it/u=1595072465,3644073269&fm=193&f=GIF");
        collectionManageFacadeService.create(request);

        OrderCreateRequest orderCreateRequest = new OrderCreateRequest();
        orderCreateRequest.setBuyerId("25");
        orderCreateRequest.setSellerId("123321111");
        orderCreateRequest.setGoodsId("10018");
        orderCreateRequest.setGoodsName(UUID.randomUUID().toString());
        orderCreateRequest.setGoodsType(GoodsType.COLLECTION);
        orderCreateRequest.setOrderAmount(new BigDecimal("10.000000"));
        orderCreateRequest.setIdentifier(UUID.randomUUID().toString());
        orderCreateRequest.setItemPrice(new BigDecimal("10.000000"));
        orderCreateRequest.setItemCount(1);

        OrderResponse response = orderFacadeService.create(orderCreateRequest);
        Assert.isTrue(response.getSuccess(), "orderFacadeService.create failed");

        PayCreateRequest payCreateRequest = new PayCreateRequest();
        payCreateRequest.setOrderAmount(orderCreateRequest.getOrderAmount());
        payCreateRequest.setBizNo(response.getOrderId());
        payCreateRequest.setBizType(BizOrderType.TRADE_ORDER);
        payCreateRequest.setMemo(orderCreateRequest.getGoodsName());
        payCreateRequest.setPayChannel(PayChannel.MOCK);
        payCreateRequest.setPayerId(orderCreateRequest.getBuyerId());
        payCreateRequest.setPayerType(orderCreateRequest.getBuyerType());
        payCreateRequest.setPayeeId(orderCreateRequest.getSellerId());
        payCreateRequest.setPayeeType(orderCreateRequest.getSellerType());
        PayOrder payOrder = payOrderService.create(payCreateRequest);
        Assert.notNull(payOrder, "payOrder create failed");
        throw new RuntimeException();
    }

    /**
     * 支付成功
     * <pre>
     *     正常支付成功：
     *     1、查询订单状态
     *     2、推进订单状态到支付成功
     *     3、藏品库存真正扣减
     *     4、创建持有的藏品
     *     5、推进支付状态到支付成功
     *     6、持有的藏品上链
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
    @GlobalTransactional(rollbackFor = Exception.class)
    public boolean paySuccess(PaySuccessEvent paySuccessEvent) {

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
        CollectionSaleResponse collectionSaleResponse = RemoteCallWrapper.call(req -> collectionFacadeService.confirmSale(req), collectionSaleRequest, "collectionFacadeService.confirmSale");

        TransactionHookManager.registerHook(new PaySuccessTransactionHook(collectionSaleResponse.getHeldCollectionId()));

        Boolean result = payOrderService.paySuccess(paySuccessEvent);
        Assert.isTrue(result, "payOrderService.paySuccess failed");

        return true;
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
