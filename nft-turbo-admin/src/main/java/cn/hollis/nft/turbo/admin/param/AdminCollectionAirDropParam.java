package cn.hollis.nft.turbo.admin.param;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 空投参数
 *
 * @author wangyibo
 */
@Setter
@Getter
public class AdminCollectionAirDropParam {

    /**
     * '藏品id'
     */
    @NotNull(message = "藏品id不能为空")
    private Long collectionId;


    /**
     * '接收用户id'
     */
    @NotNull(message = "接收用户id不能为空")
    private String recipientUserId;

    /**
     * '藏品数量'
     */
    private Integer quantity;


    /**
     * '藏品交易类型'
     */
    private String bizType;


}
