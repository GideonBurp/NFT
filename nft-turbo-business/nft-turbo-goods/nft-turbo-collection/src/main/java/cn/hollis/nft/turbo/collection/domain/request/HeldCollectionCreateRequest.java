package cn.hollis.nft.turbo.collection.domain.request;

import cn.hollis.nft.turbo.api.collection.constant.CollectionRarity;
import cn.hollis.nft.turbo.api.goods.request.GoodsConfirmSaleRequest;
import cn.hollis.nft.turbo.collection.domain.constant.HeldCollectionEventType;
import lombok.*;

import java.math.BigDecimal;

/**
 * @author wswyb001
 * @date 2024/01/17
 */

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class HeldCollectionCreateRequest extends BaseHeldCollectionRequest {
    /**
     * 藏品名称
     */
    private String name;

    /**
     * 藏品封面
     */
    private String cover;

    /**
     * 购入价格
     */
    private BigDecimal purchasePrice;

    /**
     * 参考价格
     */
    private BigDecimal referencePrice;

    /**
     * 稀有度
     */
    private CollectionRarity rarity;

    /**
     * '藏品id'
     */
    private Long collectionId;

    /**
     * '持有人id'
     */
    private Long userId;

    /**
     * '藏品编号'
     */
    private String serialNo;

    /**
     * '业务Id'
     */
    private String bizNo;

    /**
     * '业务类型'
     */
    private String bizType;

    public HeldCollectionCreateRequest(GoodsConfirmSaleRequest goodsConfirmSaleRequest, String serialNo) {
        this.collectionId = goodsConfirmSaleRequest.goodsId();
        this.userId = Long.valueOf(goodsConfirmSaleRequest.userId());
        this.bizNo = goodsConfirmSaleRequest.bizNo();
        this.bizType = goodsConfirmSaleRequest.bizType();
        this.name = goodsConfirmSaleRequest.name();
        this.cover = goodsConfirmSaleRequest.cover();
        this.purchasePrice = goodsConfirmSaleRequest.purchasePrice();
        this.referencePrice = goodsConfirmSaleRequest.purchasePrice();
        this.serialNo = serialNo;
    }

    @Override
    public HeldCollectionEventType getEventType() {
        return HeldCollectionEventType.CREATE;
    }
}
