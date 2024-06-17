package cn.hollis.nft.turbo.collection.domain.request;

import cn.hollis.nft.turbo.collection.domain.constant.HeldCollectionEventType;
import cn.hollis.nft.turbo.collection.facade.CollectionConfirmSaleRequest;
import lombok.*;

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

    public HeldCollectionCreateRequest(CollectionConfirmSaleRequest collectionConfirmSaleRequest,String serialNo) {
        this.collectionId = collectionConfirmSaleRequest.collectionId();
        this.userId = Long.valueOf(collectionConfirmSaleRequest.userId());
        this.bizNo = collectionConfirmSaleRequest.bizNo();
        this.bizType = collectionConfirmSaleRequest.bizType();
        this.serialNo = serialNo;
    }

    @Override
    public HeldCollectionEventType getEventType() {
        return HeldCollectionEventType.CREATE;
    }
}
