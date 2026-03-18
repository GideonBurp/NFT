package cn.gideon.nft.turbo.api.goods.service;

import cn.gideon.nft.turbo.api.goods.request.GoodsSaleRequest;
import cn.gideon.nft.turbo.api.goods.response.GoodsSaleResponse;

/**
 * @author Gideon
 */
public interface GoodsTransactionFacadeService {

    /**
     * 锁定库存
     * @param request
     * @return
     */
    public GoodsSaleResponse tryDecreaseInventory(GoodsSaleRequest request);

    /**
     * 解锁并扣减库存
     * @param request
     * @return
     */
    public GoodsSaleResponse confirmDecreaseInventory(GoodsSaleRequest request);

    /**
     * 解锁库存
     * @param request
     * @return
     */
    public GoodsSaleResponse cancelDecreaseInventory(GoodsSaleRequest request);
}
