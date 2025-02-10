package cn.hollis.nft.turbo.inventory.facade.service;

import cn.hollis.nft.turbo.api.goods.constant.GoodsType;
import cn.hollis.nft.turbo.api.inventory.request.InventoryRequest;
import cn.hollis.nft.turbo.api.inventory.service.InventoryFacadeService;
import cn.hollis.nft.turbo.base.response.SingleResponse;
import cn.hollis.nft.turbo.inventory.domain.response.InventoryResponse;
import cn.hollis.nft.turbo.inventory.domain.service.impl.BlindBoxInventoryRedisService;
import cn.hollis.nft.turbo.inventory.domain.service.impl.CollectionInventoryRedisService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

import static cn.hollis.nft.turbo.inventory.domain.service.impl.AbstraceInventoryRedisService.ERROR_CODE_INVENTORY_NOT_ENOUGH;

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

    private Cache<String, Boolean> soldOutGoodsLocalCache;

    @PostConstruct
    public void init() {
        soldOutGoodsLocalCache = Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .maximumSize(3000)
                .build();
    }


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

        if (soldOutGoodsLocalCache.getIfPresent(goodsType + "_" + inventoryRequest.getGoodsId()) != null) {
            return SingleResponse.fail(ERROR_CODE_INVENTORY_NOT_ENOUGH, "库存不足");
        }

        InventoryResponse inventoryResponse = switch (goodsType) {
            case COLLECTION -> collectionInventoryRedisService.decrease(inventoryRequest);

            case BLIND_BOX -> blindBoxInventoryRedisService.decrease(inventoryRequest);

            default -> throw new UnsupportedOperationException("unsupport goods type");
        };

        if (inventoryResponse.getSuccess()) {
            //如果库存为0，则在本地缓存记录，用于对售罄商品快速决策
            if (inventoryResponse.getInventory() == 0) {
                soldOutGoodsLocalCache.put(goodsType + "_" + inventoryRequest.getGoodsId(), true);
            }
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

            //如果库存大于0，则清除本地缓存中的商品售罄标记
            //但是因为是本地缓存，所以无法保证一致性，极端情况下，会存在一分钟的数据不一致的延迟。但是在高并发秒杀场景下，一般是不允许修改库存，所以这种不一致业务上可接受
            if (inventoryResponse.getInventory() > 0) {
                soldOutGoodsLocalCache.invalidate(goodsType + "_" + inventoryRequest.getGoodsId());
            }

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

        soldOutGoodsLocalCache.invalidate(goodsType + "_" + inventoryRequest.getGoodsId());

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

        if (soldOutGoodsLocalCache.getIfPresent(goodsType + "_" + InventoryRequest.getGoodsId()) != null) {
            return SingleResponse.of(0);
        }

        Integer inventory = switch (goodsType) {
            case COLLECTION -> collectionInventoryRedisService.getInventory(InventoryRequest);
            case BLIND_BOX -> blindBoxInventoryRedisService.getInventory(InventoryRequest);
            default -> throw new UnsupportedOperationException("unsupport goods type");
        };

        return SingleResponse.of(inventory);
    }
}
