package cn.hollis.nft.turbo.api.collection.request;

import cn.hollis.nft.turbo.api.order.request.OrderCreateRequest;
import cn.hollis.nft.turbo.base.request.BaseRequest;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Hollis
 */
@Getter
@Setter
@NoArgsConstructor
public class InventoryRequest extends BaseRequest {

    /**
     * 藏品ID
     */
    @NotNull(message = "collectionId is null")
    private String collectionId;

    /**
     * 唯一标识
     */
    private String identifier;

    /**
     * 库存数量
     */
    private Integer inventory;

    public InventoryRequest(OrderCreateRequest  orderCreateRequest) {
        this.collectionId = orderCreateRequest.getGoodsId();
        this.identifier = orderCreateRequest.getOrderId();
        this.inventory = orderCreateRequest.getItemCount();
    }
}
