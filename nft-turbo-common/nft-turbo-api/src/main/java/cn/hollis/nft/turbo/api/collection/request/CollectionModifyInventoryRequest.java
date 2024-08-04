package cn.hollis.nft.turbo.api.collection.request;

import cn.hollis.nft.turbo.api.collection.constant.CollectionEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author wswyb001
 * @date 2024/01/17
 */

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class CollectionModifyInventoryRequest extends BaseCollectionRequest {
    /**
     * 幂等号
     */
    private String identifier;

    /**
     * '藏品id'
     */
    private Long collectionId;

    /**
     * '藏品数量'
     */
    private Long quantity;


    @Override
    public CollectionEvent getEventType() {
        return CollectionEvent.MODIFY_INVENTORY;
    }
}
