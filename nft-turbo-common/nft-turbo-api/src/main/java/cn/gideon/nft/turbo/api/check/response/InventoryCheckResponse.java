package cn.gideon.nft.turbo.api.check.response;

import cn.gideon.nft.turbo.base.response.BaseResponse;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Gideon
 */
@Getter
@Setter
public class InventoryCheckResponse extends BaseResponse {

    /**
     * 核对结果
     */
    private Boolean checkResult;
}