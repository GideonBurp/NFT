package cn.hollis.nft.turbo.api.collection.constant;

/**
 * @author Hollis
 */
public enum CollectionEvent {

    /**
     * 上链事件
     */
    CHAIN,

    /**
     * 销毁事件
     */
    DESTROY,

    /**
     * 出售事件
     */
    SALE,
    TRY_SALE,
    CONFIRM_SALE,
    CANCEL_SALE,

    /**
     * 转移事件
     */
    TRANSFER;
}
