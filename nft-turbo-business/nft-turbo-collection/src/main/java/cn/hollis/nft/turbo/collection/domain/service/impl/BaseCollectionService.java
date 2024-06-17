package cn.hollis.nft.turbo.collection.domain.service.impl;

import cn.hollis.nft.turbo.api.collection.request.CollectionChainRequest;
import cn.hollis.nft.turbo.api.collection.request.CollectionSaleRequest;
import cn.hollis.nft.turbo.collection.domain.entity.Collection;
import cn.hollis.nft.turbo.collection.domain.entity.CollectionStream;
import cn.hollis.nft.turbo.collection.domain.request.HeldCollectionCreateRequest;
import cn.hollis.nft.turbo.collection.domain.response.CollectionConfirmSaleResponse;
import cn.hollis.nft.turbo.collection.domain.service.CollectionService;
import cn.hollis.nft.turbo.collection.domain.service.HeldCollectionService;
import cn.hollis.nft.turbo.collection.exception.CollectionException;
import cn.hollis.nft.turbo.collection.facade.CollectionCancelSaleRequest;
import cn.hollis.nft.turbo.collection.facade.CollectionConfirmSaleRequest;
import cn.hollis.nft.turbo.collection.facade.CollectionTrySaleRequest;
import cn.hollis.nft.turbo.collection.infrastructure.mapper.CollectionMapper;
import cn.hollis.nft.turbo.collection.infrastructure.mapper.CollectionStreamMapper;
import cn.hutool.core.lang.Assert;
import com.alicp.jetcache.anno.CacheRefresh;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static cn.hollis.nft.turbo.collection.exception.CollectionErrorCode.*;

public abstract class BaseCollectionService extends ServiceImpl<CollectionMapper, Collection> implements CollectionService {

    @Autowired
    private HeldCollectionService heldCollectionService;

    @Autowired
    private CollectionMapper collectionMapper;

    @Autowired
    private CollectionStreamMapper collectionStreamMapper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Collection create(CollectionChainRequest request) {
        Collection existCollection = collectionMapper.selectByIdentifier(request.getIdentifier(), request.getClassId());
        if (existCollection != null) {
            return existCollection;
        }

        Collection collection = Collection.create(request);
        var saveResult = this.save(collection);
        if (!saveResult) {
            throw new CollectionException(COLLECTION_SAVE_FAILED);
        }

        CollectionStream stream = new CollectionStream(collection, request.getIdentifier(), request.getEventType());
        saveResult = collectionStreamMapper.insert(stream) == 1;
        Assert.isTrue(saveResult, () -> new CollectionException(COLLECTION_STREAM_SAVE_FAILED));

        return collection;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean trySale(CollectionTrySaleRequest request) {
        //流水校验
        CollectionStream existStream = collectionStreamMapper.selectByIdentifier(request.identifier(), request.eventType().name(), request.collectionId());
        if (null != existStream) {
            throw new CollectionException(COLLECTION_STREAM_EXIST);
        }

        //核心逻辑执行
        int result = collectionMapper.trySale(request.collectionId(), request.quantity());
        Assert.isTrue(result == 1, () -> new CollectionException(COLLECTION_SAVE_FAILED));

        //查询出最新的值
        Collection collection = this.getById(request.collectionId());

        //新增collection流水
        CollectionStream stream = new CollectionStream(collection, request.identifier(), request.eventType());
        result = collectionStreamMapper.insert(stream);
        Assert.isTrue(result > 0, () -> new CollectionException(COLLECTION_STREAM_SAVE_FAILED));
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean cancelSale(CollectionCancelSaleRequest request) {
        //流水校验
        CollectionStream existStream = collectionStreamMapper.selectByIdentifier(request.identifier(), request.eventType().name(), request.collectionId());
        if (null != existStream) {
            throw new CollectionException(COLLECTION_STREAM_EXIST);
        }

        //核心逻辑执行
        int result = collectionMapper.cancelSale(request.collectionId(), request.quantity());
        Assert.isTrue(result == 1, () -> new CollectionException(COLLECTION_SAVE_FAILED));

        //查询出最新的值
        Collection collection = this.getById(request.collectionId());

        //新增collection流水
        CollectionStream stream = new CollectionStream(collection, request.identifier(), request.eventType());
        result = collectionStreamMapper.insert(stream);
        Assert.isTrue(result > 0, () -> new CollectionException(COLLECTION_STREAM_SAVE_FAILED));
        return true;
    }

    protected Boolean doExecute(CollectionSaleRequest request, Consumer<CollectionSaleRequest> consumer) {
        //流水校验
        CollectionStream existStream = collectionStreamMapper.selectByIdentifier(request.getIdentifier(), request.getEventType().name(), request.getCollectionId());
        if (null != existStream) {
            throw new CollectionException(COLLECTION_STREAM_EXIST);
        }

        //核心逻辑执行
        consumer.accept(request);

        //查询出最新的值
        Collection collection = this.getById(request.getCollectionId());

        //新增collection流水
        CollectionStream stream = new CollectionStream(collection, request.getIdentifier(), request.getEventType());
        int result = collectionStreamMapper.insert(stream);
        Assert.isTrue(result > 0, () -> new CollectionException(COLLECTION_STREAM_SAVE_FAILED));
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public CollectionConfirmSaleResponse confirmSale(CollectionConfirmSaleRequest request) {

        //流水校验
        CollectionStream existStream = collectionStreamMapper.selectByIdentifier(request.identifier(), request.eventType().name(), request.collectionId());
        if (null != existStream) {
            throw new CollectionException(COLLECTION_STREAM_EXIST);
        }
        Collection collection = this.getById(request.collectionId());

        int result = collectionMapper.confirmSale(request.collectionId(), collection.getOccupiedInventory(), request.quantity());
        Assert.isTrue(result == 1, () -> new CollectionException(COLLECTION_SAVE_FAILED));

        //新增collection流水
        CollectionStream stream = new CollectionStream(collection, request.identifier(), request.eventType());
        stream.setOccupiedInventory(collection.getOccupiedInventory() + request.quantity());

        result = collectionStreamMapper.insert(stream);
        Assert.isTrue(result > 0, () -> new CollectionException(COLLECTION_STREAM_SAVE_FAILED));

        HeldCollectionCreateRequest heldCollectionCreateRequest = new HeldCollectionCreateRequest(request,String.valueOf(collection.getOccupiedInventory() + 1));
        var heldCollection = heldCollectionService.create(heldCollectionCreateRequest);

        CollectionConfirmSaleResponse collectionSaleResponse = new CollectionConfirmSaleResponse();
        collectionSaleResponse.setSuccess(true);
        collectionSaleResponse.setCollection(collection);
        collectionSaleResponse.setHeldCollection(heldCollection);
        return collectionSaleResponse;
    }

    @Override
    public Collection queryByClassId(String classId) {
        QueryWrapper<Collection> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("class_id", classId);
        List<Collection> retList = list(queryWrapper);
        if (CollectionUtils.isEmpty(retList)) {
            return null;
        }
        return retList.get(0);
    }

    @Override
    @Cached(name = ":collection:cache:id:", expire = 60,localExpire = 10, timeUnit = TimeUnit.MINUTES, cacheType = CacheType.BOTH, key = "#collectionId", cacheNullValue = true)
    @CacheRefresh(refresh = 50, timeUnit = TimeUnit.MINUTES)
    public Collection queryById(Long collectionId) {
        return getById(collectionId);
    }
}
