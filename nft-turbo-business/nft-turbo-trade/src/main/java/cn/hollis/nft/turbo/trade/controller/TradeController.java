package cn.hollis.nft.turbo.trade.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hollis.nft.turbo.api.common.constant.BizOrderType;
import cn.hollis.nft.turbo.api.common.constant.BusinessCode;
import cn.hollis.nft.turbo.api.goods.constant.GoodsType;
import cn.hollis.nft.turbo.api.goods.model.BaseGoodsVO;
import cn.hollis.nft.turbo.api.goods.service.GoodsFacadeService;
import cn.hollis.nft.turbo.api.inventory.request.InventoryRequest;
import cn.hollis.nft.turbo.api.inventory.service.InventoryFacadeService;
import cn.hollis.nft.turbo.api.order.OrderFacadeService;
import cn.hollis.nft.turbo.api.order.constant.TradeOrderState;
import cn.hollis.nft.turbo.api.order.model.TradeOrderVO;
import cn.hollis.nft.turbo.api.order.request.OrderCancelRequest;
import cn.hollis.nft.turbo.api.order.request.OrderCreateAndConfirmRequest;
import cn.hollis.nft.turbo.api.order.request.OrderCreateRequest;
import cn.hollis.nft.turbo.api.order.request.OrderTimeoutRequest;
import cn.hollis.nft.turbo.api.order.response.OrderResponse;
import cn.hollis.nft.turbo.api.pay.model.PayOrderVO;
import cn.hollis.nft.turbo.api.pay.request.PayCreateRequest;
import cn.hollis.nft.turbo.api.pay.response.PayCreateResponse;
import cn.hollis.nft.turbo.api.pay.service.PayFacadeService;
import cn.hollis.nft.turbo.api.user.constant.UserType;
import cn.hollis.nft.turbo.base.response.SingleResponse;
import cn.hollis.nft.turbo.base.utils.RemoteCallWrapper;
import cn.hollis.nft.turbo.order.OrderException;
import cn.hollis.nft.turbo.order.sharding.id.DistributeID;
import cn.hollis.nft.turbo.order.sharding.id.WorkerIdHolder;
import cn.hollis.nft.turbo.order.validator.OrderCreateValidator;
import cn.hollis.nft.turbo.trade.exception.TradeErrorCode;
import cn.hollis.nft.turbo.trade.exception.TradeException;
import cn.hollis.nft.turbo.trade.param.BuyParam;
import cn.hollis.nft.turbo.trade.param.CancelParam;
import cn.hollis.nft.turbo.trade.param.PayParam;
import cn.hollis.nft.turbo.web.vo.Result;
import cn.hollis.turbo.stream.producer.StreamProducer;
import com.alibaba.fastjson.JSON;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

import static cn.hollis.nft.turbo.api.order.constant.OrderErrorCode.ORDER_CREATE_PRE_VALID_FAILED;
import static cn.hollis.nft.turbo.api.user.constant.UserType.PLATFORM;
import static cn.hollis.nft.turbo.web.filter.TokenFilter.tokenThreadLocal;

/**
 * @author Hollis
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("trade")
public class TradeController {

    @Autowired
    private OrderFacadeService orderFacadeService;

    @Autowired
    private PayFacadeService payFacadeService;

    @Autowired
    private GoodsFacadeService goodsFacadeService;

    @Autowired
    private StreamProducer streamProducer;

    @Autowired
    private InventoryFacadeService inventoryFacadeService;

    @Autowired
    private OrderCreateValidator orderPreValidatorChain;

    /**
     * 秒杀下单，热点商品
     *
     * @param
     * @return 订单号
     */
    @PostMapping("/buy")
    public Result<String> buy(@Valid @RequestBody BuyParam buyParam) {
        OrderCreateRequest orderCreateRequest = getOrderCreateRequest(buyParam);

        OrderResponse orderResponse = RemoteCallWrapper.call(req -> orderFacadeService.create(req), orderCreateRequest, "createOrder");

        if (orderResponse.getSuccess()) {
            return Result.success(orderCreateRequest.getOrderId());
        }

        throw new TradeException(TradeErrorCode.ORDER_CREATE_FAILED);
    }

    /**
     * 秒杀下单（不基于inventory hint的实现），热点商品
     *
     * @param
     * @return 幂等号
     */
    @PostMapping("/newBuy")
    public Result<String> newBuy(@Valid @RequestBody BuyParam buyParam) {
        OrderCreateRequest orderCreateRequest = getOrderCreateRequest(buyParam);

        try {
            orderPreValidatorChain.validate(orderCreateRequest);
        } catch (OrderException e) {
            throw new TradeException(e.getErrorCode().getMessage(), ORDER_CREATE_PRE_VALID_FAILED);
        }

        boolean result = streamProducer.send("newBuy-out-0", buyParam.getGoodsType(), JSON.toJSONString(orderCreateRequest));

        if (!result) {
            throw new TradeException(TradeErrorCode.ORDER_CREATE_FAILED);
        }

        //因为不管本地事务是否成功，只要一阶段消息发成功都会返回 true，所以这里需要确认是否成功
        InventoryRequest inventoryRequest = new InventoryRequest(orderCreateRequest);
        SingleResponse<String> response = inventoryFacadeService.getInventoryDecreaseLog(inventoryRequest);

        if (response.getSuccess() && response.getData() != null) {
            return Result.success(orderCreateRequest.getOrderId());
        }

        throw new TradeException(TradeErrorCode.ORDER_CREATE_FAILED);
    }

    /**
     * 普通下单，非热点商品
     *
     * @param
     * @return 订单号
     */
    @PostMapping("/normalBuy")
    public Result<String> normalBuy(@Valid @RequestBody BuyParam buyParam) {
        OrderCreateAndConfirmRequest orderCreateRequest = getOrderCreateAndConfirmRequest(buyParam);

        OrderResponse orderResponse = RemoteCallWrapper.call(req -> orderFacadeService.createAndConfirm(req), orderCreateRequest, "createOrder");

        if (orderResponse.getSuccess()) {
            //同步写redis，如果失败，不阻塞流程，靠binlog同步保障
            try {
                InventoryRequest inventoryRequest = new InventoryRequest(orderCreateRequest);
                inventoryFacadeService.decrease(inventoryRequest);
            } catch (Exception e) {
                log.error("decrease inventory from redis failed", e);
            }

            return Result.success(orderCreateRequest.getOrderId());
        }

        throw new TradeException(TradeErrorCode.ORDER_CREATE_FAILED);
    }

    @NotNull
    private OrderCreateRequest getOrderCreateRequest(BuyParam buyParam) {
        String userId = (String) StpUtil.getLoginId();
        String orderId = DistributeID.generateWithSnowflake(BusinessCode.TRADE_ORDER, WorkerIdHolder.WORKER_ID, userId);
        //创建订单
        OrderCreateRequest orderCreateRequest = new OrderCreateRequest();
        orderCreateRequest.setOrderId(orderId);
        orderCreateRequest.setIdentifier(tokenThreadLocal.get());
        orderCreateRequest.setBuyerId(userId);
        orderCreateRequest.setGoodsId(buyParam.getGoodsId());
        orderCreateRequest.setGoodsType(GoodsType.valueOf(buyParam.getGoodsType()));
        orderCreateRequest.setItemCount(buyParam.getItemCount());
        BaseGoodsVO goodsVO = goodsFacadeService.getGoods(buyParam.getGoodsId(), GoodsType.valueOf(buyParam.getGoodsType()));
        if (goodsVO == null || !goodsVO.available()) {
            throw new TradeException(TradeErrorCode.GOODS_NOT_FOR_SALE);
        }
        orderCreateRequest.setItemPrice(goodsVO.getPrice());
        orderCreateRequest.setSellerId(goodsVO.getSellerId());
        orderCreateRequest.setGoodsName(goodsVO.getGoodsName());
        orderCreateRequest.setGoodsPicUrl(goodsVO.getGoodsPicUrl());
        orderCreateRequest.setSnapshotVersion(goodsVO.getVersion());
        orderCreateRequest.setOrderAmount(orderCreateRequest.getItemPrice().multiply(new BigDecimal(orderCreateRequest.getItemCount())));

        return orderCreateRequest;
    }

    @NotNull
    private OrderCreateAndConfirmRequest getOrderCreateAndConfirmRequest(BuyParam buyParam) {
        String userId = (String) StpUtil.getLoginId();
        String orderId = DistributeID.generateWithSnowflake(BusinessCode.TRADE_ORDER, WorkerIdHolder.WORKER_ID, userId);
        //创建订单
        OrderCreateAndConfirmRequest orderCreateAndConfirmRequest = new OrderCreateAndConfirmRequest();
        orderCreateAndConfirmRequest.setOrderId(orderId);
        orderCreateAndConfirmRequest.setIdentifier(tokenThreadLocal.get());
        orderCreateAndConfirmRequest.setBuyerId(userId);
        orderCreateAndConfirmRequest.setGoodsId(buyParam.getGoodsId());
        orderCreateAndConfirmRequest.setGoodsType(GoodsType.valueOf(buyParam.getGoodsType()));
        orderCreateAndConfirmRequest.setItemCount(buyParam.getItemCount());
        BaseGoodsVO goodsVO = goodsFacadeService.getGoods(buyParam.getGoodsId(), GoodsType.valueOf(buyParam.getGoodsType()));
        if (goodsVO == null || !goodsVO.available()) {
            throw new TradeException(TradeErrorCode.GOODS_NOT_FOR_SALE);
        }
        orderCreateAndConfirmRequest.setItemPrice(goodsVO.getPrice());
        orderCreateAndConfirmRequest.setSellerId(goodsVO.getSellerId());
        orderCreateAndConfirmRequest.setGoodsName(goodsVO.getGoodsName());
        orderCreateAndConfirmRequest.setGoodsPicUrl(goodsVO.getGoodsPicUrl());
        orderCreateAndConfirmRequest.setSnapshotVersion(goodsVO.getVersion());
        orderCreateAndConfirmRequest.setOrderAmount(orderCreateAndConfirmRequest.getItemPrice().multiply(new BigDecimal(orderCreateAndConfirmRequest.getItemCount())));
        orderCreateAndConfirmRequest.setOperator(UserType.PLATFORM.name());
        orderCreateAndConfirmRequest.setOperatorType(UserType.PLATFORM);
        orderCreateAndConfirmRequest.setOperateTime(new Date());
        return orderCreateAndConfirmRequest;
    }

    /**
     * 支付
     *
     * @param
     * @return 支付链接地址
     */
    @PostMapping("/pay")
    public Result<PayOrderVO> pay(@Valid @RequestBody PayParam payParam) {
        String userId = (String) StpUtil.getLoginId();
        SingleResponse<TradeOrderVO> singleResponse = orderFacadeService.getTradeOrder(payParam.getOrderId(), userId);

        TradeOrderVO tradeOrderVO = singleResponse.getData();

        if (tradeOrderVO == null) {
            throw new TradeException(TradeErrorCode.GOODS_NOT_EXIST);
        }

        if (tradeOrderVO.getOrderState() != TradeOrderState.CONFIRM) {
            throw new TradeException(TradeErrorCode.ORDER_IS_CANNOT_PAY);
        }

        if (tradeOrderVO.getTimeout()) {
            doAsyncTimeoutOrder(tradeOrderVO);
            throw new TradeException(TradeErrorCode.ORDER_IS_CANNOT_PAY);
        }

        if (!tradeOrderVO.getBuyerId().equals(userId)) {
            throw new TradeException(TradeErrorCode.PAY_PERMISSION_DENIED);
        }

        PayCreateRequest payCreateRequest = new PayCreateRequest();
        payCreateRequest.setOrderAmount(tradeOrderVO.getOrderAmount());
        payCreateRequest.setBizNo(tradeOrderVO.getOrderId());
        payCreateRequest.setBizType(BizOrderType.TRADE_ORDER);
        payCreateRequest.setMemo(tradeOrderVO.getGoodsName());
        payCreateRequest.setPayChannel(payParam.getPayChannel());
        payCreateRequest.setPayerId(tradeOrderVO.getBuyerId());
        payCreateRequest.setPayerType(tradeOrderVO.getBuyerType());
        payCreateRequest.setPayeeId(tradeOrderVO.getSellerId());
        payCreateRequest.setPayeeType(tradeOrderVO.getSellerType());

        PayCreateResponse payCreateResponse = RemoteCallWrapper.call(req -> payFacadeService.generatePayUrl(req), payCreateRequest, "generatePayUrl");

        if (payCreateResponse.getSuccess()) {
            PayOrderVO payOrderVO = new PayOrderVO();
            payOrderVO.setPayOrderId(payCreateResponse.getPayOrderId());
            payOrderVO.setPayUrl(payCreateResponse.getPayUrl());
            return Result.success(payOrderVO);
        }

        throw new TradeException(TradeErrorCode.PAY_CREATE_FAILED);
    }

    private void doAsyncTimeoutOrder(TradeOrderVO tradeOrderVO) {
        if (tradeOrderVO.getOrderState() != TradeOrderState.CLOSED) {
            Thread.ofVirtual().start(() -> {
                OrderTimeoutRequest cancelRequest = new OrderTimeoutRequest();
                cancelRequest.setOperatorType(PLATFORM);
                cancelRequest.setOperator(PLATFORM.getDesc());
                cancelRequest.setOrderId(tradeOrderVO.getOrderId());
                cancelRequest.setOperateTime(new Date());
                cancelRequest.setIdentifier(UUID.randomUUID().toString());
                orderFacadeService.timeout(cancelRequest);
            });
        }
    }

    /**
     * 取消订单
     *
     * @param
     * @return 是否成功
     */
    @PostMapping("/cancel")
    public Result<Boolean> cancel(@Valid @RequestBody CancelParam cancelParam) {
        String userId = (String) StpUtil.getLoginId();

        OrderCancelRequest orderCancelRequest = new OrderCancelRequest();
        orderCancelRequest.setIdentifier(cancelParam.getOrderId());
        orderCancelRequest.setOperateTime(new Date());
        orderCancelRequest.setOrderId(cancelParam.getOrderId());
        orderCancelRequest.setOperator(userId);
        orderCancelRequest.setOperatorType(UserType.CUSTOMER);

        OrderResponse orderResponse = RemoteCallWrapper.call(req -> orderFacadeService.cancel(req), orderCancelRequest, "cancelOrder");

        if (orderResponse.getSuccess()) {
            return Result.success(true);
        }

        throw new TradeException(TradeErrorCode.ORDER_CANCEL_FAILED);
    }

}
