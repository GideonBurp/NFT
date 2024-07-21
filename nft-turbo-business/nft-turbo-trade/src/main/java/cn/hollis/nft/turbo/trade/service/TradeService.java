package cn.hollis.nft.turbo.trade.service;

import cn.hollis.nft.turbo.api.common.constant.BizOrderType;
import cn.hollis.nft.turbo.api.goods.constant.GoodsType;
import cn.hollis.nft.turbo.api.goods.service.GoodsFacadeService;
import cn.hollis.nft.turbo.api.order.OrderFacadeService;
import cn.hollis.nft.turbo.api.order.request.OrderCreateRequest;
import cn.hollis.nft.turbo.api.order.response.OrderResponse;
import cn.hollis.nft.turbo.api.pay.constant.PayChannel;
import cn.hollis.nft.turbo.api.pay.request.PayCreateRequest;
import cn.hollis.nft.turbo.api.pay.service.PayFacadeService;
import cn.hollis.nft.turbo.api.user.request.UserRegisterRequest;
import cn.hollis.nft.turbo.api.user.service.UserFacadeService;
import cn.hutool.core.util.RandomUtil;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.shardingsphere.transaction.annotation.ShardingTransactionType;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class TradeService {

    @DubboReference(version = "1.0.0")
    private OrderFacadeService orderFacadeService;

    @DubboReference(version = "1.0.0")
    private PayFacadeService payFacadeService;

    @DubboReference(version = "1.0.0")
    private GoodsFacadeService goodsFacadeService;

    @DubboReference(version = "1.0.0")
    private UserFacadeService userFacadeService;

    @Transactional(rollbackFor = Exception.class)
    @ShardingTransactionType(TransactionType.BASE)
    public void testTransaction(String tel) {
        OrderCreateRequest orderCreateRequest = new OrderCreateRequest();
        orderCreateRequest.setBuyerId("29");
        orderCreateRequest.setSellerId("123321111");
        orderCreateRequest.setGoodsId("10018");
        orderCreateRequest.setGoodsName(UUID.randomUUID().toString());
        orderCreateRequest.setGoodsType(GoodsType.COLLECTION);
        orderCreateRequest.setOrderAmount(new BigDecimal("10.000000"));
        orderCreateRequest.setIdentifier(UUID.randomUUID().toString());
        orderCreateRequest.setItemPrice(new BigDecimal("10.000000"));
        orderCreateRequest.setItemCount(1);

        OrderResponse response = orderFacadeService.create(orderCreateRequest);

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
        payFacadeService.generatePayUrl(payCreateRequest);

        UserRegisterRequest request = new UserRegisterRequest();
        request.setTelephone(tel);
        userFacadeService.register(request);

//        throw new RuntimeException();
    }
}
