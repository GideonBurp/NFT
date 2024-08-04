package cn.hollis.nft.turbo.collection.facade;

import cn.hollis.nft.turbo.api.chain.constant.ChainOperateBizTypeEnum;
import cn.hollis.nft.turbo.api.chain.request.ChainProcessRequest;
import cn.hollis.nft.turbo.api.chain.service.ChainFacadeService;
import cn.hollis.nft.turbo.api.collection.constant.CollectionStateEnum;
import cn.hollis.nft.turbo.api.collection.model.CollectionVO;
import cn.hollis.nft.turbo.api.collection.request.*;
import cn.hollis.nft.turbo.api.collection.response.CollectionChainResponse;
import cn.hollis.nft.turbo.api.collection.response.CollectionModifyResponse;
import cn.hollis.nft.turbo.api.collection.response.CollectionRemoveResponse;
import cn.hollis.nft.turbo.api.collection.service.CollectionManageFacadeService;
import cn.hollis.nft.turbo.base.response.PageResponse;
import cn.hollis.nft.turbo.collection.domain.entity.Collection;
import cn.hollis.nft.turbo.collection.domain.entity.convertor.CollectionConvertor;
import cn.hollis.nft.turbo.collection.domain.request.CollectionInventoryRequest;
import cn.hollis.nft.turbo.collection.domain.response.CollectionInventoryResponse;
import cn.hollis.nft.turbo.collection.domain.service.CollectionService;
import cn.hollis.nft.turbo.collection.domain.service.impl.db.CollectionDbService;
import cn.hollis.nft.turbo.collection.domain.service.impl.redis.CollectionInventoryRedisService;
import cn.hollis.nft.turbo.collection.exception.CollectionException;
import cn.hollis.nft.turbo.rpc.facade.Facade;
import com.alibaba.fastjson.JSON;
import org.apache.dubbo.config.annotation.DubboService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import static cn.hollis.nft.turbo.collection.exception.CollectionErrorCode.COLLECTION_INVENTORY_UPDATE_FAILED;
import static cn.hollis.nft.turbo.collection.exception.CollectionErrorCode.COLLECTION_QUERY_FAIL;

/**
 * 藏品管理服务
 *
 * @author hollis
 */
@DubboService(version = "1.0.0")
public class CollectionManageFacadeServiceImpl implements CollectionManageFacadeService {

    private static final Logger logger = LoggerFactory.getLogger(CollectionManageFacadeServiceImpl.class);

    @Autowired
    private ChainFacadeService chainFacadeService;

    @Autowired
    private CollectionService collectionService;

    @Autowired
    private CollectionDbService collectionDbService;

    @Autowired
    private CollectionInventoryRedisService collectionInventoryRedisService;

    @Override
    @Facade
    public CollectionChainResponse create(CollectionCreateRequest request) {
        Collection collection = collectionService.create(request);
        ChainProcessRequest chainProcessRequest = new ChainProcessRequest();
        chainProcessRequest.setIdentifier(request.getIdentifier());
        chainProcessRequest.setClassId(String.valueOf(collection.getId()));
        chainProcessRequest.setClassName(request.getName());
        chainProcessRequest.setBizType(ChainOperateBizTypeEnum.COLLECTION.name());
        chainProcessRequest.setBizId(collection.getId().toString());
        var chainRes = chainFacadeService.chain(chainProcessRequest);
        CollectionChainResponse response = new CollectionChainResponse();
        if (!chainRes.getSuccess()) {
            response.setSuccess(false);
            return response;
        }
        response.setSuccess(true);
        response.setCollectionId(collection.getId());
        return response;
    }

    @Override
    public CollectionRemoveResponse remove(CollectionRemoveRequest request) {
        CollectionRemoveResponse response = new CollectionRemoveResponse();
        Collection collection = collectionService.getById(request.getCollectionId());
        if (null == collection) {
            throw new CollectionException(COLLECTION_QUERY_FAIL);
        }

        collection.setState(CollectionStateEnum.REMOVED);
        var removeRes = collectionService.updateById(collection);

        if (removeRes) {
            CollectionInventoryRequest inventoryRequest = new CollectionInventoryRequest();
            inventoryRequest.setCollectionId(request.getCollectionId().toString());
            collectionInventoryRedisService.invalid(inventoryRequest);
        }

        response.setSuccess(removeRes);
        response.setCollectionId(collection.getId());
        return response;
    }

    @Override
    public CollectionModifyResponse modifyInventory(CollectionModifyInventoryRequest request) {
        CollectionModifyResponse response = new CollectionModifyResponse();
        response.setCollectionId(request.getCollectionId());
        Collection collection = collectionService.getById(request.getCollectionId());
        if (null == collection) {
            throw new CollectionException(COLLECTION_QUERY_FAIL);
        }

        long oldSaleableInventory = collection.getSaleableInventory();
        long quantityDiff = request.getQuantity() - collection.getQuantity();

        if (quantityDiff == 0) {
            response.setSuccess(true);
            return response;
        }

        CollectionInventoryRequest inventoryRequest = new CollectionInventoryRequest();
        inventoryRequest.setCollectionId(request.getCollectionId().toString());
        inventoryRequest.setIdentifier(request.getIdentifier());
        inventoryRequest.setInventory((int) Math.abs(quantityDiff));
        CollectionInventoryResponse inventoryResponse;
        if (quantityDiff > 0) {
            inventoryResponse = collectionInventoryRedisService.increase(inventoryRequest);
        } else {
            inventoryResponse = collectionInventoryRedisService.decrease(inventoryRequest);
        }

        if (!inventoryResponse.getSuccess()) {
            logger.error("modify inventory failed : " + JSON.toJSONString(inventoryResponse));
            throw new CollectionException(COLLECTION_INVENTORY_UPDATE_FAILED);
        }

        collection.setQuantity(request.getQuantity());
        collection.setSaleableInventory(oldSaleableInventory + quantityDiff);
        boolean res = collectionService.updateById(collection);
        response.setSuccess(res);
        return response;
    }

    @Override
    public CollectionModifyResponse modifyPrice(CollectionModifyPriceRequest request) {
        Collection collection = collectionService.getById(request.getCollectionId());
        if (null == collection) {
            throw new CollectionException(COLLECTION_QUERY_FAIL);
        }
        collection.setVersion(collection.getVersion() + 1);
        collection.setPrice(request.getPrice());
        var res = collectionService.updateAndSaveSnapshot(collection);

        CollectionModifyResponse response = new CollectionModifyResponse();
        response.setSuccess(res);
        response.setCollectionId(collection.getId());
        return response;
    }

    @Override
    public PageResponse<CollectionVO> pageQuery(CollectionPageQueryRequest request) {
        PageResponse<Collection> colletionPage = collectionDbService.pageQueryByState(request.getKeyword(), request.getState(), request.getCurrentPage(), request.getPageSize());
        return PageResponse.of(CollectionConvertor.INSTANCE.mapToVo(colletionPage.getDatas()), colletionPage.getTotal(), colletionPage.getPageSize(), request.getCurrentPage());
    }

}
