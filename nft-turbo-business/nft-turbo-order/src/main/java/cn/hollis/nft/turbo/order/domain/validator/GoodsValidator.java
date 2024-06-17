package cn.hollis.nft.turbo.order.domain.validator;

import cn.hollis.nft.turbo.api.goods.model.BaseGoodsVO;
import cn.hollis.nft.turbo.api.goods.service.GoodsFacadeService;
import cn.hollis.nft.turbo.api.order.request.OrderCreateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 商品校验器
 *
 * @author hollis
 */
@Component
public class GoodsValidator implements OrderCreateValidator {

    @Autowired
    private GoodsFacadeService goodsFacadeService;

    private OrderCreateValidator nextValidator;

    @Override
    public void setNext(OrderCreateValidator nextValidator) {
        this.nextValidator = nextValidator;
    }

    @Override
    public void validate(OrderCreateRequest request) throws Exception {
        BaseGoodsVO baseGoodsVO = goodsFacadeService.getGoods(request.getGoodsId(), request.getGoodsType());

        if (!baseGoodsVO.available()) {
            throw new Exception("商品不可用");
        }

        if (baseGoodsVO.getPrice().compareTo(request.getItemPrice()) != 0) {
            throw new Exception("商品价格发生变化");
        }
    }
}
