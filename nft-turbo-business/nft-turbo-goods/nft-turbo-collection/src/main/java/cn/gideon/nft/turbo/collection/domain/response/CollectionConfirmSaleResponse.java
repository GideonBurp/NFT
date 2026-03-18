package cn.gideon.nft.turbo.collection.domain.response;

import cn.gideon.nft.turbo.base.response.BaseResponse;
import cn.gideon.nft.turbo.collection.domain.entity.Collection;
import cn.gideon.nft.turbo.collection.domain.entity.HeldCollection;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Gideon
 */
@Setter
@Getter
public class CollectionConfirmSaleResponse extends BaseResponse {

    private Collection collection;

    private HeldCollection heldCollection;
}
