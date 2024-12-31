package cn.hollis.nft.turbo.inventory.facade.service;

import cn.hollis.nft.turbo.api.inventory.request.InventoryRequest;
import cn.hollis.nft.turbo.api.goods.constant.GoodsType;
import cn.hollis.nft.turbo.api.inventory.service.InventoryFacadeService;
import cn.hollis.nft.turbo.base.response.SingleResponse;
import cn.hollis.nft.turbo.inventory.domain.response.InventoryResponse;
import cn.hollis.nft.turbo.inventory.domain.service.impl.BlindBoxInventoryRedisService;
import cn.hollis.nft.turbo.inventory.domain.service.impl.CollectionInventoryRedisService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 库存门面服务
 *
 * @author Hollis
 */
@DubboService(version = "1.0.0")
public class InventoryFacadeServiceImpl implements InventoryFacadeService {

    @Autowired
    private CollectionInventoryRedisService collectionInventoryRedisService;

    @Autowired
    private BlindBoxInventoryRedisService blindBoxInventoryRedisService;

    @Override
    public SingleResponse<Boolean> init(InventoryRequest inventoryRequest) {
        GoodsType goodsType = inventoryRequest.getGoodsType();
        InventoryResponse inventoryResponse = switch (goodsType) {
            case COLLECTION -> collectionInventoryRedisService.init(inventoryRequest);

            case BLIND_BOX -> blindBoxInventoryRedisService.init(inventoryRequest);

            default -> throw new UnsupportedOperationException("unsupport goods type");
        };

        if (inventoryResponse.getSuccess()) {
            return SingleResponse.of(true);
        }

        return SingleResponse.fail(inventoryResponse.getResponseCode(), inventoryResponse.getResponseMessage());
    }

    @Override
    public SingleResponse<Boolean> decrease(InventoryRequest inventoryRequest) {
        GoodsType goodsType = inventoryRequest.getGoodsType();
        InventoryResponse inventoryResponse = switch (goodsType) {
            case COLLECTION -> collectionInventoryRedisService.decrease(inventoryRequest);

            case BLIND_BOX -> blindBoxInventoryRedisService.decrease(inventoryRequest);

            default -> throw new UnsupportedOperationException("unsupport goods type");
        };

        if (inventoryResponse.getSuccess()) {
            return SingleResponse.of(true);
        }

        return SingleResponse.fail(inventoryResponse.getResponseCode(), inventoryResponse.getResponseMessage());
    }

    @Override
    public SingleResponse<Boolean> increase(InventoryRequest inventoryRequest) {
        GoodsType goodsType = inventoryRequest.getGoodsType();
        InventoryResponse inventoryResponse = switch (goodsType) {
            case COLLECTION -> collectionInventoryRedisService.increase(inventoryRequest);

            case BLIND_BOX -> blindBoxInventoryRedisService.increase(inventoryRequest);

            default -> throw new UnsupportedOperationException("unsupport goods type");
        };

        if (inventoryResponse.getSuccess()) {
            return SingleResponse.of(true);
        }

        return SingleResponse.fail(inventoryResponse.getResponseCode(), inventoryResponse.getResponseMessage());
    }

    @Override
    public SingleResponse<Void> invalid(InventoryRequest inventoryRequest) {
        GoodsType goodsType = inventoryRequest.getGoodsType();
        switch (goodsType) {
            case COLLECTION -> collectionInventoryRedisService.invalid(inventoryRequest);

            case BLIND_BOX -> blindBoxInventoryRedisService.invalid(inventoryRequest);

            default -> throw new UnsupportedOperationException("unsupport goods type");
        }

        return SingleResponse.of(null);
    }

    @Override
    public SingleResponse<String> getInventoryDecreaseLog(InventoryRequest inventoryRequest) {
        GoodsType goodsType = inventoryRequest.getGoodsType();
        String inventoryResponse = switch (goodsType) {
            case COLLECTION -> collectionInventoryRedisService.getInventoryDecreaseLog(inventoryRequest);

            case BLIND_BOX -> blindBoxInventoryRedisService.getInventoryDecreaseLog(inventoryRequest);

            default -> throw new UnsupportedOperationException("unsupport goods type");
        };

        return SingleResponse.of(inventoryResponse);
    }

    @Override
    public SingleResponse<Integer> queryInventory(InventoryRequest InventoryRequest) {

        GoodsType goodsType = InventoryRequest.getGoodsType();

        Integer inventory = switch (goodsType) {
            case COLLECTION -> collectionInventoryRedisService.getInventory(InventoryRequest);
            case BLIND_BOX -> blindBoxInventoryRedisService.getInventory(InventoryRequest);
            default -> throw new UnsupportedOperationException("unsupport goods type");
        };

        return SingleResponse.of(inventory);
    }
}
