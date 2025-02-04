package cn.hollis.nft.turbo.admin.param;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 藏品创建参数
 *
 * @author wangyibo
 */
@Setter
@Getter
public class AdminCollectionCreateParam {

    /**
     * '藏品名称'
     */
    @NotNull(message = "藏品名称不能为空")
    private String name;

    /**
     * '藏品封面'
     */
    @NotNull(message = "藏品封面不能为空")
    private String cover;

    /**
     * '藏品详情'
     */
    private String detail;

    /**
     * '价格'
     */
    @NotNull(message = "价格不能为空")
    private BigDecimal price;

    /**
     * '藏品数量'
     */
    @Min(value = 1, message = "藏品数量不能小于1")
    private Long quantity;

    /**
     * '藏品发售时间'
     */
    @NotNull(message = "藏品发售时间不能为空")
    private String saleTime;

    /**
     * '藏品是否预约'
     */
    @NotNull(message = "藏品是否预约不能为空")
    private Integer canBook;

    /**
     * '藏品预约开始时间'
     */
    private String bookStartTime;

    /**
     * '藏品预约结束时间'
     */
    private String bookEndTime;
}
