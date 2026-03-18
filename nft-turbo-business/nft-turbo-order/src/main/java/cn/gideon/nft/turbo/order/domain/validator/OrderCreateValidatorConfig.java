package cn.gideon.nft.turbo.order.domain.validator;

import cn.gideon.nft.turbo.order.validator.GoodsBookValidator;
import cn.gideon.nft.turbo.order.validator.GoodsValidator;
import cn.gideon.nft.turbo.order.validator.OrderCreateValidator;
import cn.gideon.nft.turbo.order.validator.UserValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

/**
 * 订单创建校验器配置
 *
 * @author gideon
 */
//@Configuration 挪到cn.gideon.nft.turbo.order.configuration.OrderClientConfiguration 中，方便和trade模块复用
@Deprecated
public class OrderCreateValidatorConfig {

    @Autowired
    private GoodsValidator goodsValidator;

    @Autowired
    private UserValidator userValidator;

    @Autowired
    private GoodsBookValidator goodsBookValidator;

    @Bean
    public OrderCreateValidator orderValidatorChain() {
        userValidator.setNext(goodsValidator);
        goodsValidator.setNext(goodsBookValidator);
        return userValidator;
    }
}
