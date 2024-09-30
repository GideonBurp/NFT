package cn.hollis.nft.turbo.order.listener;

import cn.hollis.nft.turbo.api.order.OrderFacadeService;
import cn.hollis.nft.turbo.api.order.request.OrderCreateAndConfirmRequest;
import cn.hollis.nft.turbo.api.order.request.OrderCreateRequest;
import cn.hollis.nft.turbo.api.order.response.OrderResponse;
import cn.hollis.nft.turbo.api.user.constant.UserType;
import cn.hollis.nft.turbo.order.domain.validator.OrderCreateValidator;
import cn.hollis.turbo.stream.param.MessageBody;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.util.Assert;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.function.Consumer;

/**
 * @author Hollis
 */
@Component
@Slf4j
public class NewBuyMsgListener {

    @Autowired
    private OrderFacadeService orderFacadeService;

    @Autowired
    private OrderCreateValidator orderValidatorChain;

    @Bean
    Consumer<Message<MessageBody>> newBuy() {
        return msg -> {
            String messageId = msg.getHeaders().get("ROCKET_MQ_MESSAGE_ID", String.class);
            String tag = msg.getHeaders().get("ROCKET_TAGS", String.class);
            OrderCreateRequest orderCreateRequest = JSON.parseObject(msg.getPayload().getBody(), OrderCreateRequest.class);
            log.info("Received NewBuy Message messageId:{},orderCreateRequest:{}，tag:{}", messageId, orderCreateRequest, tag);

            OrderCreateAndConfirmRequest orderCreateAndConfirmRequest = new OrderCreateAndConfirmRequest();
            BeanUtils.copyProperties(orderCreateRequest, orderCreateAndConfirmRequest);
            orderCreateAndConfirmRequest.setOperator(UserType.PLATFORM.name());
            orderCreateAndConfirmRequest.setOperatorType(UserType.PLATFORM);
            orderCreateAndConfirmRequest.setOperateTime(new Date());

            OrderResponse orderResponse = orderFacadeService.createAndConfirm(orderCreateAndConfirmRequest);
            Assert.isTrue(orderResponse.getSuccess(), "create order failed");
        };
    }
}
