package cn.hollis.nft.turbo.admin.param;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 藏品修改参数
 *
 * @author wangyibo
 */
@Setter
@Getter
public class AdminCollectionModifyParam {

    /**
     * '藏品id'
     */
    @NotNull(message = "藏品id不能为空")
    private Long collectionId;

    /**
     * '藏品数量'
     */
    private Integer quantity;


    /**
     * '价格'
     */
    private BigDecimal price;


}
