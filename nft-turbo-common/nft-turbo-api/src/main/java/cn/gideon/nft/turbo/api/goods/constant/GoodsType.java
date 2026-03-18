package cn.gideon.nft.turbo.api.goods.constant;

/**
 * @author Gideon
 */
public enum GoodsType {

    /** 藏品 */
    COLLECTION("藏品"),

    /**
     * 盲盒
     */
    BLIND_BOX("盲盒");


    private String value;

    GoodsType(String value) {
        this.value = value;
    }
}
