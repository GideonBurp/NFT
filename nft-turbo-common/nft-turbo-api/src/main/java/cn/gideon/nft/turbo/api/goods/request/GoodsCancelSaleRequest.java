package cn.gideon.nft.turbo.api.goods.request;

import cn.gideon.nft.turbo.api.goods.constant.GoodsEvent;
/**
 * 商品取消售卖请求
 *
 * @author Gideon
 */
public record GoodsCancelSaleRequest(String identifier, Long collectionId, Integer quantity) {

    public GoodsEvent eventType() {
        return GoodsEvent.CANCEL_SALE;
    }
}
