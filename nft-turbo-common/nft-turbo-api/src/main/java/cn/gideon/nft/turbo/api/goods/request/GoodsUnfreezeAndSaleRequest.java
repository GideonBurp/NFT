package cn.gideon.nft.turbo.api.goods.request;

import cn.gideon.nft.turbo.api.goods.constant.GoodsEvent;

/**
 * @author Gideon
 * @param identifier
 * @param goodsId
 * @param quantity
 */
public record GoodsUnfreezeAndSaleRequest(String identifier, Long goodsId, Integer quantity) {

    public GoodsEvent eventType() {
        return GoodsEvent.UNFREEZE_AND_SALE;
    }
}
