package cn.hollis.nft.turbo.collection.domain.service.impl;

import cn.hollis.nft.turbo.collection.domain.request.CollectionInventoryRequest;
import cn.hollis.nft.turbo.collection.domain.response.CollectionInventoryResponse;
import cn.hollis.nft.turbo.collection.domain.service.CollectionInventoryService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 藏品库存服务-基于数据库
 *
 * @author Hollis
 */
@Service
public class CollectionInventoryDBService implements CollectionInventoryService {

    @Override
    public CollectionInventoryResponse init(CollectionInventoryRequest request) {
        //todo
        return null;
    }

    @Override
    public Integer getInventory(CollectionInventoryRequest request) {
        //todo
        return null;
    }

    @Override
    public CollectionInventoryResponse decrease(CollectionInventoryRequest request) {
        //todo
        return null;
    }

    @Override
    public List<Object> getInventoryDecreaseLogs(CollectionInventoryRequest request) {
        //todo
        return null;
    }

    @Override
    public CollectionInventoryResponse increase(CollectionInventoryRequest request) {
        //todo
        return null;
    }

    @Override
    public void invalid(CollectionInventoryRequest request) {
        //todo
    }
}
