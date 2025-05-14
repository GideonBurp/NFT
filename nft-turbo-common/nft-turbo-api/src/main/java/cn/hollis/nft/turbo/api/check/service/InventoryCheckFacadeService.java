package cn.hollis.nft.turbo.api.check.service;

import cn.hollis.nft.turbo.api.check.request.InventoryCheckRequest;
import cn.hollis.nft.turbo.api.check.response.InventoryCheckResponse;

/**
 * @author Hollis
 */
public interface InventoryCheckFacadeService {

    /**
     * 库存核对
     *
     * @param request
     * @return
     */
    public InventoryCheckResponse check(InventoryCheckRequest request);
}
