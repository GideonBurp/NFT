package cn.gideon.nft.turbo.api.check.service;

import cn.gideon.nft.turbo.api.check.request.InventoryCheckRequest;
import cn.gideon.nft.turbo.api.check.response.InventoryCheckResponse;

/**
 * @author Gideon
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
