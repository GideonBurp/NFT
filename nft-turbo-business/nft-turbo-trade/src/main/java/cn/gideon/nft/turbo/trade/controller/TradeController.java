package cn.gideon.nft.turbo.trade.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.gideon.nft.turbo.api.check.request.InventoryCheckRequest;
import cn.gideon.nft.turbo.api.check.response.InventoryCheckResponse;
import cn.gideon.nft.turbo.api.check.service.InventoryCheckFacadeService;
import cn.gideon.nft.turbo.api.common.constant.BizOrderType;
import cn.gideon.nft.turbo.api.common.constant.BusinessCode;
import cn.gideon.nft.turbo.api.goods.constant.GoodsEvent;
import cn.gideon.nft.turbo.api.goods.constant.GoodsType;
import cn.gideon.nft.turbo.api.goods.model.BaseGoodsVO;
import cn.gideon.nft.turbo.api.goods.request.GoodsBookRequest;
import cn.gideon.nft.turbo.api.goods.response.GoodsBookResponse;
import cn.gideon.nft.turbo.api.goods.service.GoodsFacadeService;
import cn.gideon.nft.turbo.api.inventory.request.InventoryRequest;
import cn.gideon.nft.turbo.api.inventory.service.InventoryFacadeService;
import cn.gideon.nft.turbo.api.order.OrderFacadeService;
import cn.gideon.nft.turbo.api.order.constant.TradeOrderState;
import cn.gideon.nft.turbo.api.order.model.TradeOrderVO;
import cn.gideon.nft.turbo.api.order.request.OrderCancelRequest;
import cn.gideon.nft.turbo.api.order.request.OrderCreateAndConfirmRequest;
import cn.gideon.nft.turbo.api.order.request.OrderCreateRequest;
import cn.gideon.nft.turbo.api.order.request.OrderTimeoutRequest;
import cn.gideon.nft.turbo.api.order.response.OrderResponse;
import cn.gideon.nft.turbo.api.pay.model.PayOrderVO;
import cn.gideon.nft.turbo.api.pay.request.PayCreateRequest;
import cn.gideon.nft.turbo.api.pay.response.PayCreateResponse;
import cn.gideon.nft.turbo.api.pay.service.PayFacadeService;
import cn.gideon.nft.turbo.api.user.constant.UserType;
import cn.gideon.nft.turbo.base.response.SingleResponse;
import cn.gideon.nft.turbo.base.utils.RemoteCallWrapper;
import cn.gideon.nft.turbo.order.OrderException;
import cn.gideon.nft.turbo.order.sharding.id.DistributeID;
import cn.gideon.nft.turbo.order.sharding.id.WorkerIdHolder;
import cn.gideon.nft.turbo.order.validator.OrderCreateValidator;
import cn.gideon.nft.turbo.trade.application.TradeApplicationService;
import cn.gideon.nft.turbo.trade.exception.TradeErrorCode;
import cn.gideon.nft.turbo.trade.exception.TradeException;
import cn.gideon.nft.turbo.trade.param.BookParam;
import cn.gideon.nft.turbo.trade.param.BuyParam;
import cn.gideon.nft.turbo.trade.param.CancelParam;
import cn.gideon.nft.turbo.trade.param.PayParam;
import cn.gideon.nft.turbo.web.vo.Result;
import cn.gideon.turbo.stream.producer.StreamProducer;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.fastjson.JSON;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import static cn.gideon.nft.turbo.api.common.constant.CommonConstant.SEPARATOR;
import static cn.gideon.nft.turbo.api.user.constant.UserType.PLATFORM;
import static cn.gideon.nft.turbo.web.filter.TokenFilter.TOKEN_THREAD_LOCAL;

/**
 * @author Gideon
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("trade")
public class TradeController {

    private static ThreadFactory inventoryBypassVerifyThreadFactory = new ThreadFactoryBuilder()
            .setNameFormat("inventory-bypass-verify-pool-%d").build();

    private ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(10, inventoryBypassVerifyThreadFactory);

    @Autowired
    private OrderFacadeService orderFacadeService;

    @Autowired
    private TradeApplicationService tradeApplicationService;

    @Autowired
    private PayFacadeService payFacadeService;

    @Autowired
    private GoodsFacadeService goodsFacadeService;

    @Autowired
    private StreamProducer streamProducer;

    @Autowired
    private InventoryFacadeService inventoryFacadeService;

    @Autowired
    private OrderCreateValidator orderValidatorChain;

    @Autowired
    private InventoryCheckFacadeService inventoryCheckFacadeService;

    /**
     * 预定
     *
     * @param
     * @return 预定id
     */
    @PostMapping("/book")
    public Result<Long> book(@Valid @RequestBody BookParam bookParam) {
        String userId = (String) StpUtil.getLoginId();

        GoodsBookRequest goodsBookRequest = new GoodsBookRequest();
        goodsBookRequest.setGoodsId(bookParam.getGoodsId());
        goodsBookRequest.setGoodsType(GoodsType.valueOf(bookParam.getGoodsType()));
        //数藏比较特殊，一个商品只能预定一次，所以这里直接用userId+goodsType+goodsId作为标识了，如果支持多次预定的话，需要在再有个活动的概念，基于活动做预约
        goodsBookRequest.setIdentifier(userId + SEPARATOR + bookParam.getGoodsType() + SEPARATOR + bookParam.getGoodsId());
        goodsBookRequest.setBuyerId(userId);
        GoodsBookResponse goodsBookResponse = RemoteCallWrapper.call(req -> goodsFacadeService.book(req), goodsBookRequest, "bookGoods");
        if (goodsBookResponse.getSuccess()) {
            return Result.success(goodsBookResponse.getBookId());
        }
        throw new TradeException(TradeErrorCode.GOODS_BOOK_FAILED);
    }

    /**
     * 下单
     * 秒杀下单，热点商品
     *
     * @param
     * @return 订单号
     */
    @PostMapping("/buy")
    @SentinelResource(value = "/trade/buy")
    public Result<String> buy(@Valid @RequestBody BuyParam buyParam) {
        try {
            OrderCreateRequest orderCreateRequest = getOrderCreateRequest(buyParam);

            OrderResponse orderResponse = RemoteCallWrapper.call(req -> orderFacadeService.create(req), orderCreateRequest, "createOrder");

            if (orderResponse.getSuccess()) {
                InventoryRequest inventoryRequest = new InventoryRequest(orderCreateRequest);
                inventoryBypassVerify(inventoryRequest);
                return Result.success(orderCreateRequest.getOrderId());
            }
        } catch (OrderException | TradeException e) {
            return Result.error(e.getErrorCode().getCode(), e.getErrorCode().getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return Result.error(TradeErrorCode.ORDER_CREATE_FAILED.getCode(), TradeErrorCode.ORDER_CREATE_FAILED.getMessage());
    }

    /**
     * 秒杀下单（不基于inventory hint的实现），热点商品，同步创建订单
     *
     * @param
     * @return 幂等号
     */
    @PostMapping("/newBuy")
    @SentinelResource(value = "/trade/buy")
    public Result<String> newBuy(@Valid @RequestBody BuyParam buyParam) {
        OrderCreateRequest orderCreateRequest = null;

        try {
            orderCreateRequest = getOrderCreateRequest(buyParam);
            orderValidatorChain.validate(orderCreateRequest);

            //本地事务执行器：InventoryDecreaseTransactionListener  消息监听：NewBuyMsgListener or NewBuyBatchMsgListener
            boolean result = streamProducer.send("newBuy-out-0", buyParam.getGoodsType(), JSON.toJSONString(orderCreateRequest));

            if (!result) {
                throw new TradeException(TradeErrorCode.ORDER_CREATE_FAILED);
            }

            //因为不管本地事务是否成功，只要一阶段消息发成功都会返回 true，所以这里需要确认是否成功
            //因为上面是用了MQ的事务消息，Redis的库存扣减是在事务消息的本地事务中同步执行的（InventoryDecreaseTransactionListener#executeLocalTransaction），所以只要成功了，这里一定能查到
            InventoryRequest inventoryRequest = new InventoryRequest(orderCreateRequest);
            //如果直接查库存,问题：只能知道当前库存数量，无法证明"这次扣减"是否成功执行
            SingleResponse<String> response = inventoryFacadeService.getInventoryDecreaseLog(inventoryRequest);

            if (response.getSuccess() && response.getData() != null) {
                //再检查一下是否有回退库存的流水，如果回退过，则不需要旁路验证
                SingleResponse<String> increaseLog = inventoryFacadeService.getInventoryIncreaseLog(inventoryRequest);
                if (increaseLog.getSuccess() && increaseLog.getData() == null) {
                    inventoryBypassVerify(inventoryRequest);
                    return Result.success(orderCreateRequest.getOrderId());
                }
            }
        } catch (OrderException | TradeException e) {
            return Result.error(e.getErrorCode().getCode(), e.getErrorCode().getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return Result.error(TradeErrorCode.ORDER_CREATE_FAILED.getCode(), TradeErrorCode.ORDER_CREATE_FAILED.getMessage());
    }

    /**
     * 秒杀下单（不基于inventory hint的实现），热点商品，同步创建订单
     *
     * @param
     * @return 幂等号
     */
    @PostMapping("/newBuyPlus")
    @SentinelResource(value = "/trade/buy")
    public Result<String> newBuyPlus(@Valid @RequestBody BuyParam buyParam) {
        try {
            OrderCreateAndConfirmRequest orderCreateAndConfirmRequest = getOrderCreateAndConfirmRequest(buyParam);
            orderValidatorChain.validate(orderCreateAndConfirmRequest);
            //本地事务执行器：OrderCreateTransactionListener  消息监听：NewBuyMsgListener or NewBuyBatchMsgListener ,
            boolean result = streamProducer.send("newBuyPlus-out-0", buyParam.getGoodsType(), JSON.toJSONString(orderCreateAndConfirmRequest));

            if (!result) {
                throw new TradeException(TradeErrorCode.ORDER_CREATE_FAILED);
            }

            //因为不管本地事务是否成功，只要一阶段消息发成功都会返回 true，所以这里需要确认是否成功
            //因为上面是用了MQ的事务消息，Redis的库存扣减是在事务消息的本地事务中同步执行的（InventoryDecreaseTransactionListener#executeLocalTransaction），所以只要成功了，这里一定能查到

            SingleResponse<TradeOrderVO> response = orderFacadeService.getTradeOrder(orderCreateAndConfirmRequest.getOrderId());

            if (response.getSuccess() && response.getData() != null && response.getData().getOrderState() == TradeOrderState.CONFIRM) {
                inventoryBypassVerify(new InventoryRequest(orderCreateAndConfirmRequest));
                return Result.success(orderCreateAndConfirmRequest.getOrderId());
            }
        } catch (OrderException | TradeException e) {
            return Result.error(e.getErrorCode().getCode(), e.getErrorCode().getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return Result.error(TradeErrorCode.ORDER_CREATE_FAILED.getCode(), TradeErrorCode.ORDER_CREATE_FAILED.getMessage());
    }

    /**
     * 库存扣减旁路验证
     *
     * @param inventoryRequest
     */
    private void inventoryBypassVerify(InventoryRequest inventoryRequest) {
        try {
            //延迟3秒检查数据库中是否有库存扣减记录
            scheduler.schedule(() -> {
                InventoryCheckRequest inventoryCheckRequest = new InventoryCheckRequest();
                inventoryCheckRequest.setIdentifier(inventoryRequest.getIdentifier());
                inventoryCheckRequest.setGoodsType(inventoryRequest.getGoodsType());
                inventoryCheckRequest.setGoodsId(inventoryRequest.getGoodsId());
                inventoryCheckRequest.setGoodsEvent(GoodsEvent.TRY_SALE);
                inventoryCheckRequest.setChangedQuantity(inventoryRequest.getInventory());
                InventoryCheckResponse checkResponse = inventoryCheckFacadeService.check(inventoryCheckRequest);
                //核验成功,数据一致
                if (checkResponse.getSuccess() && checkResponse.getCheckResult()) {
                    //删除库存扣减流水记录
                    inventoryFacadeService.removeInventoryDecreaseLog(inventoryRequest);
                }
            }, 3, TimeUnit.SECONDS);

        } catch (Exception e) {
            //核验失败打印日志，不影响主流程，等异步任务再核对
            log.error("inventoryBypassVerify failed,", e);
        }
    }

    /**
     * 普通下单，非热点商品
     *
     * ✅为什么要区分普通交易和秒杀交易？
     * 因为场景不一样，方案就会不一样。
     *
     * 秒杀交易是典型的高并发场景，普通交易则没那么高的并发，所以他们的技术方案一定是不同的。
     *
     * 而且如果做过大促保障或者做过秒杀业务的话会知道，有一个关键的可用性要求就是不能互相影响，不能因为做一个秒杀活动，导致普通商品无法下单了，
     * 同理，普通商品下单服务有问题也不应该导致秒杀服务不可用，所以需要做好隔离。
     *
     * 所以我们需要区分不同的交易类型，来做不同的技术方案。如何识别那些商品走普通交易哪些商品走秒杀交易呢？
     *
     * 其实，一种是通过频道或者活动报名，比如双十一活动报名，或者聚划算默认秒杀。
     *
     * 还有就是做热点商品检测，发现达到一定的热度之后，晋升为秒杀商品，走秒杀链路。
     *
     * 热点检测的方案有很多，我们用的是比较简单，那就是针对藏品做预约，预约过才能购买，根据预约人数判断商品够不够热。
     *
     * 还有就是可以参考 jd 开源的一个 hotkey 的框架做热点检测也可以，
     *
     * @param
     * @return 订单号
     */
    @PostMapping("/normalBuy")
    @SentinelResource(value = "/trade/normalBuy")
    public Result<String> normalBuy(@Valid @RequestBody BuyParam buyParam) {
        try {
            OrderCreateAndConfirmRequest orderCreateAndConfirmRequest = getOrderCreateAndConfirmRequest(buyParam);
            orderValidatorChain.validate(orderCreateAndConfirmRequest);
            OrderResponse orderResponse = RemoteCallWrapper.call(req -> tradeApplicationService.normalBuy(req), orderCreateAndConfirmRequest, "createOrder");

            if (orderResponse.getSuccess()) {
                //同步写redis，如果失败，不阻塞流程，靠binlog同步保障
                try {
                    InventoryRequest inventoryRequest = new InventoryRequest(orderCreateAndConfirmRequest);
                    inventoryFacadeService.decrease(inventoryRequest);
                } catch (Exception e) {
                    log.error("decrease inventory from redis failed", e);
                }

                return Result.success(orderCreateAndConfirmRequest.getOrderId());
            }
        } catch (OrderException | TradeException e) {
            return Result.error(e.getErrorCode().getCode(), e.getErrorCode().getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
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
        orderCreateRequest.setIdentifier(TOKEN_THREAD_LOCAL.get());
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
        orderCreateAndConfirmRequest.setIdentifier(TOKEN_THREAD_LOCAL.get());
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
