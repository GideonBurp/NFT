package cn.hollis.nft.turbo.api.goods.model;

import cn.hollis.nft.turbo.api.goods.constant.GoodsState;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Hollis
 */
public abstract class BaseGoodsVO implements Serializable {

    /**
     * '状态'
     */
    private GoodsState state;

    public GoodsState getState() {
        return state;
    }

    public void setState(GoodsState state) {
        this.state = state;
    }

    /**
     * 商品名称
     *
     * @return
     */
    public abstract String getGoodsName();

    /**
     * 商品图片
     *
     * @return
     */
    public abstract String getGoodsPicUrl();

    /**
     * 卖家id
     *
     * @return
     */
    public abstract String getSellerId();

    /**
     * 版本
     *
     * @return
     */
    public abstract Integer getVersion();

    /**
     * 是否可用
     *
     * @return
     */
    public Boolean available() {
        return this.state == GoodsState.SELLING;
    }

    /**
     * 价格
     *
     * @return
     */
    public abstract BigDecimal getPrice();
}
