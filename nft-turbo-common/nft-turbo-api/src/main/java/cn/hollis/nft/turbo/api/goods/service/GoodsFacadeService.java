package cn.hollis.nft.turbo.api.goods.service;

import cn.hollis.nft.turbo.api.goods.constant.GoodsType;
import cn.hollis.nft.turbo.api.goods.model.BaseGoodsVO;
import cn.hollis.nft.turbo.api.goods.request.GoodsSaleRequest;
import cn.hollis.nft.turbo.api.goods.response.GoodsSaleResponse;

/**
 * 商品服务
 *
 * @author hollis
 */
public interface GoodsFacadeService {

    /**
     * 获取商品
     *
     * @param goodsId
     * @param goodsType
     * @return
     */
    public BaseGoodsVO getGoods(String goodsId, GoodsType goodsType);

    /**
     * 藏品出售的try阶段，做库存预占用
     *
     * @param request
     * @return
     */
    GoodsSaleResponse trySale(GoodsSaleRequest request);

    /**
     * 藏品出售的try阶段，做库存预占用-无hint
     *
     * @param request
     * @return
     */
    GoodsSaleResponse trySaleWithoutHint(GoodsSaleRequest request);

    /**
     * 藏品出售的confirm阶段，做真正售出
     *
     * @param request
     * @return
     */
    GoodsSaleResponse confirmSale(GoodsSaleRequest request);

    /**
     * 藏品出售的cancel阶段，做库存退还
     *
     * @param request
     * @return
     */
    GoodsSaleResponse cancelSale(GoodsSaleRequest request);
}
