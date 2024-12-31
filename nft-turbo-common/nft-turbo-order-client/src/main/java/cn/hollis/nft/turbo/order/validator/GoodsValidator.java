package cn.hollis.nft.turbo.order.validator;

import cn.hollis.nft.turbo.api.goods.model.BaseGoodsVO;
import cn.hollis.nft.turbo.api.goods.service.GoodsFacadeService;
import cn.hollis.nft.turbo.api.order.request.OrderCreateRequest;
import cn.hollis.nft.turbo.order.OrderException;

import static cn.hollis.nft.turbo.api.order.constant.OrderErrorCode.GOODS_NOT_AVAILABLE;
import static cn.hollis.nft.turbo.api.order.constant.OrderErrorCode.GOODS_PRICE_CHANGED;

/**
 * 商品校验器
 *
 * @author hollis
 */
public class GoodsValidator extends BaseOrderCreateValidator {

    private GoodsFacadeService goodsFacadeService;

    @Override
    protected void doValidate(OrderCreateRequest request) throws OrderException {
        BaseGoodsVO baseGoodsVO = goodsFacadeService.getGoods(request.getGoodsId(), request.getGoodsType());

        if (!baseGoodsVO.available()) {
            throw new OrderException(GOODS_NOT_AVAILABLE);
        }

        if (baseGoodsVO.getPrice().compareTo(request.getItemPrice()) != 0) {
            throw new OrderException(GOODS_PRICE_CHANGED);
        }
    }

    public GoodsValidator(GoodsFacadeService goodsFacadeService) {
        this.goodsFacadeService = goodsFacadeService;
    }

    public GoodsValidator() {
    }
}
