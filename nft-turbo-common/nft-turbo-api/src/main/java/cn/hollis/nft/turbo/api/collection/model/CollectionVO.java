package cn.hollis.nft.turbo.api.collection.model;

import cn.hollis.nft.turbo.api.collection.constant.CollectionStateEnum;
import cn.hollis.nft.turbo.api.goods.constant.GoodsState;
import cn.hollis.nft.turbo.api.goods.model.BaseGoodsVO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * @author Hollis
 */
@Getter
@Setter
@ToString
public class CollectionVO extends BaseGoodsVO {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;
    /**
     * '藏品名称'
     */
    private String name;

    /**
     * '藏品封面'
     */
    private String cover;

    /**
     * '藏品类目id'
     */
    private String classId;

    /**
     * '价格'
     */
    private BigDecimal price;

    /**
     * '藏品数量'
     */
    private Long quantity;

    /**
     * '库存'
     */
    private Long inventory;

    /**
     * '藏品创建时间'
     */
    private Date createTime;

    /**
     * '藏品发售时间'
     */
    private Date saleTime;

    /**
     * '藏品上链时间'
     */
    private Date syncChainTime;

    /**
     * 版本
     */
    private Integer version;

    public static final int DEFAULT_MIN_SALE_TIME = 60;

    public void setState(CollectionStateEnum state, Date saleTime, Long saleableInventory) {
        if (state.equals(CollectionStateEnum.INIT) || state.equals(CollectionStateEnum.REMOVED)) {
            super.setState(GoodsState.NOT_FOR_SALE);
        }

        Instant now = Instant.now();

        if (now.compareTo(saleTime.toInstant()) >= 0) {
            if (saleableInventory > 0) {
                super.setState(GoodsState.SELLING);
            } else {
                super.setState(GoodsState.SOLD_OUT);
            }
        } else {
            if (ChronoUnit.MINUTES.between(now, saleTime.toInstant()) > DEFAULT_MIN_SALE_TIME) {
                super.setState(GoodsState.WAIT_FOR_SALE);
            } else {
                super.setState(GoodsState.COMING_SOON);
            }
        }
    }

    @Override
    public String getGoodsName() {
        return this.name;
    }

    @Override
    public String getGoodsPicUrl() {
        return this.cover;
    }

    @Override
    public String getSellerId() {
        //藏品持有人默认是平台,平台ID用O表示
        return "0";
    }

    @Override
    public Integer getVersion() {
        return version;
    }

    @Override
    public BigDecimal getPrice() {
        return this.price;
    }
}
