package cn.gideon.nft.turbo.api.goods.request;

import cn.gideon.nft.turbo.api.goods.constant.GoodsEvent;

/**
 * @param identifier
 * @param goodsId
 * @param quantity
 * @author Gideon
 */
public record GoodsTrySaleRequest(String identifier, Long goodsId, Integer quantity) {

    public GoodsEvent eventType() {
        return GoodsEvent.TRY_SALE;
    }
}
