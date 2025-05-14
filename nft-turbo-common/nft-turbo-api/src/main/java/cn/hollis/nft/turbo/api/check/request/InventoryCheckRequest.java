package cn.hollis.nft.turbo.api.check.request;

import cn.hollis.nft.turbo.api.goods.constant.GoodsEvent;
import cn.hollis.nft.turbo.api.goods.constant.GoodsType;
import cn.hollis.nft.turbo.base.request.BaseRequest;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Hollis
 */
@Getter
@Setter
public class InventoryCheckRequest extends BaseRequest {

    /**
     * '商品ID
     */
    @NotNull(message = "goodsId不能为空")
    private String goodsId;

    /**
     * '商品类型'
     */
    @NotNull(message = "goodsType不能为空")
    private GoodsType goodsType;

    /**
     * '标识符'
     */
    @NotNull(message = "identifier不能为空")
    private String identifier;

    /**
     * '变更数量'
     */
    @NotNull(message = "changedQuantity不能为空")
    private Integer changedQuantity;

    /**
     * '商品事件'
     */
    @NotNull(message = "goodsEvent不能为空")
    private GoodsEvent goodsEvent;
}
