package cn.hollis.nft.turbo.api.collection.request;

import cn.hollis.nft.turbo.api.collection.constant.CollectionEvent;
import cn.hollis.nft.turbo.base.request.BaseRequest;

/**
 * @author Hollis
 */
public abstract class BaseCollectionRequest extends BaseRequest {

    /**
     * 获取事件类型
     * @return
     */
    public abstract CollectionEvent getEventType();
}
