package cn.gideon.nft.turbo.inventory.domain.response;

import cn.gideon.nft.turbo.api.goods.constant.GoodsType;
import cn.gideon.nft.turbo.base.response.BaseResponse;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Gideon
 */
@Getter
@Setter
public class InventoryResponse extends BaseResponse {

    private String goodsId;

    private GoodsType goodsType;

    private String identifier;

    private Integer inventory;
}
