package cn.hollis.nft.turbo.api.collection.constant;

/**
 * 藏品稀有度
 *
 * @author Hollis
 */
public enum CollectionRarity {
    /**
     * 普通
     */
    COMMON("普通"),
    /**
     * 稀有
     */
    RARE("稀有"),
    /**
     * 史诗
     */
    EPIC("史诗"),
    /**
     * 传说
     */
    LEGENDARY("传说"),
    /**
     * 独特
     */
    UNIQUE("独特"),
    /**
     * 神话
     */
    MYTHICAL("神话");

    private String value;

    CollectionRarity(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
