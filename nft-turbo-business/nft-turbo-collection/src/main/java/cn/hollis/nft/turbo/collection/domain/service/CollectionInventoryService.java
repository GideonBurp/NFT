package cn.hollis.nft.turbo.collection.domain.service;

import cn.hollis.nft.turbo.api.collection.request.InventoryRequest;
import cn.hollis.nft.turbo.collection.domain.response.CollectionInventoryResponse;
import org.springframework.stereotype.Service;

/**
 * 藏品库存服务
 *
 * @author Hollis
 */
@Service
public interface CollectionInventoryService {

    /**
     * 初始化藏品库存
     *
     * @param request
     * @return
     */
    public CollectionInventoryResponse init(InventoryRequest request);

    /**
     * 获取藏品库存
     *
     * @param request
     * @return
     */
    public Integer getInventory(InventoryRequest request);

    /**
     * 扣减藏品库存
     *
     * @param request
     * @return
     */
    public CollectionInventoryResponse decrease(InventoryRequest request);

    /**
     * 获取藏品库存扣减日志
     *
     * @param request
     * @return
     */
    public String getInventoryDecreaseLog(InventoryRequest request);

    /**
     * 增加藏品库存
     *
     * @param request
     * @return
     */
    public CollectionInventoryResponse increase(InventoryRequest request);

    /**
     * 失效藏品库存
     *
     * @param request
     * @return
     */
    public void invalid(InventoryRequest request);
}
