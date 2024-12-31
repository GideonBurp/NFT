package cn.hollis.nft.turbo.api.box.model;

import cn.hollis.nft.turbo.api.box.constant.BlindBoxStateEnum;
import cn.hollis.nft.turbo.api.goods.constant.GoodsState;
import cn.hollis.nft.turbo.api.goods.model.BaseGoodsVO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static cn.hollis.nft.turbo.api.collection.model.CollectionVO.DEFAULT_MIN_SALE_TIME;

/**
 * @author Hollis
 */
@Getter
@Setter
@ToString
public class BlindBoxVO extends BaseGoodsVO {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * '盲盒名称'
     */
    private String name;

    /**
     * '盲盒封面'
     */
    private String cover;

    /**
     * '盲盒详情'
     */
    private String detail;

    /**
     * '价格'
     */
    private BigDecimal price;

    /**
     * '库存'
     */
    private Long inventory;

    /**
     * '藏品数量'
     */
    private Long quantity;

    /**
     * 幂等号
     */
    private String identifier;

    /**
     * '可售库存'
     */
    private Long saleableInventory;

    /**
     * '已占库存'
     */
    private Long occupiedInventory;

    /**
     * 盲盒分配规则
     */
    private String allocateRule;

    public void setState(BlindBoxStateEnum state, Date saleTime, Long saleableInventory) {
        if (state.equals(BlindBoxStateEnum.INIT) || state.equals(BlindBoxStateEnum.REMOVED)) {
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
        return name;
    }

    @Override
    public String getGoodsPicUrl() {
        return cover;
    }

    @Override
    public String getSellerId() {
        //藏品持有人默认是平台,平台ID用O表示
        return "0";
    }

    @Override
    public Integer getVersion() {
        return 0;
    }
}
