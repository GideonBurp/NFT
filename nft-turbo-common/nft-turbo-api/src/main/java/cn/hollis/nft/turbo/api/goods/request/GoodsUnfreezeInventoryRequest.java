package cn.hollis.nft.turbo.api.goods.request;

import cn.hollis.nft.turbo.api.goods.constant.GoodsEvent;

/**
 * @author Hollis
 * @param identifier
 * @param goodsId
 * @param quantity
 */
public record GoodsUnfreezeInventoryRequest(String identifier, Long goodsId, Integer quantity) {

    public GoodsEvent eventType() {
        return GoodsEvent.UNFREEZE_INVENTORY;
    }
}
