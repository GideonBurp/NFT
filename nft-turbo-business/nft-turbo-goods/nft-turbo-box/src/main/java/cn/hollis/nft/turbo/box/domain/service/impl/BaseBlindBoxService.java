package cn.hollis.nft.turbo.box.domain.service.impl;

import cn.hollis.nft.turbo.api.box.constant.BlindAllotBoxRule;
import cn.hollis.nft.turbo.api.box.request.BlindBoxCreateRequest;
import cn.hollis.nft.turbo.api.box.request.BlindBoxItemCreateRequest;
import cn.hollis.nft.turbo.api.goods.request.GoodsCancelSaleRequest;
import cn.hollis.nft.turbo.api.goods.request.GoodsConfirmSaleRequest;
import cn.hollis.nft.turbo.api.goods.request.GoodsTrySaleRequest;
import cn.hollis.nft.turbo.api.goods.response.GoodsSaleResponse;
import cn.hollis.nft.turbo.box.domain.entity.BlindBox;
import cn.hollis.nft.turbo.box.domain.entity.BlindBoxInventoryStream;
import cn.hollis.nft.turbo.box.domain.entity.BlindBoxItem;
import cn.hollis.nft.turbo.box.domain.request.BlindBoxAssignRequest;
import cn.hollis.nft.turbo.box.domain.request.BlindBoxBindMatchRequest;
import cn.hollis.nft.turbo.box.domain.service.BlindBoxItemService;
import cn.hollis.nft.turbo.box.domain.service.BlindBoxRuleServiceFactory;
import cn.hollis.nft.turbo.box.domain.service.BlindBoxService;
import cn.hollis.nft.turbo.box.exception.BlindBoxException;
import cn.hollis.nft.turbo.box.infrastructure.mapper.BlindBoxInventoryStreamMapper;
import cn.hollis.nft.turbo.box.infrastructure.mapper.BlindBoxMapper;
import cn.hutool.core.lang.Assert;
import com.alicp.jetcache.anno.CacheInvalidate;
import com.alicp.jetcache.anno.CacheRefresh;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static cn.hollis.nft.turbo.box.exception.BlindBoxErrorCode.*;

/**
 * @author Hollis
 * <p>
 * 通用的盲盒服务
 */
public abstract class BaseBlindBoxService extends ServiceImpl<BlindBoxMapper, BlindBox> implements BlindBoxService {

    @Autowired
    private BlindBoxInventoryStreamMapper blindBoxInventoryStreamMapper;
    @Autowired
    private BlindBoxItemService blindBoxItemService;
    @Autowired
    private BlindBoxMapper blindBoxMapper;
    @Autowired
    private BlindBoxRuleServiceFactory blindBoxRuleServiceFactory;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BlindBox create(BlindBoxCreateRequest request) {
        BlindBox blindBox = BlindBox.create(request);

        var saveResult = this.save(blindBox);
        Assert.isTrue(saveResult, () -> new BlindBoxException(BLIND_BOX_SAVE_FAILED));

        List<BlindBoxItem> items = new ArrayList<>();

        //构造盲盒条目
        for (BlindBoxItemCreateRequest boxItemCreateRequest : request.getBlindBoxItemCreateRequests()) {
            Long quality = boxItemCreateRequest.getQuantity();
            //数量有多少个，就循环初始化多少个盲盒条目
            IntStream.range(0, quality.intValue()).forEach(i -> {
                BlindBoxItem blindBoxItem = BlindBoxItem.create(boxItemCreateRequest, blindBox);
                items.add(blindBoxItem);
            });
        }

        //批量创建盲盒条目
        saveResult = blindBoxItemService.batchCreateItem(items);
        Assert.isTrue(saveResult, () -> new BlindBoxException(BLIND_BOX_ITEM_SAVE_FAILED));

        return blindBox;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean trySale(GoodsTrySaleRequest request) {
        //流水校验
        BlindBoxInventoryStream existStream = blindBoxInventoryStreamMapper.selectByIdentifier(request.identifier(), request.eventType().name(), request.goodsId());
        if (null != existStream) {
            return true;
        }

        //查询出最新的值
        BlindBox blindBox = this.getById(request.goodsId());

        //新增blindBox流水
        BlindBoxInventoryStream stream = new BlindBoxInventoryStream(blindBox, request.identifier(), request.eventType(), request.quantity());
        int result = blindBoxInventoryStreamMapper.insert(stream);
        Assert.isTrue(result > 0, () -> new BlindBoxException(BLIND_BOX_STREAM_SAVE_FAILED));

        //核心逻辑执行
        result = blindBoxMapper.trySale(request.goodsId(), request.quantity());
        Assert.isTrue(result == 1, () -> new BlindBoxException(BLIND_BOX_SAVE_FAILED));
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean trySaleWithoutHint(GoodsTrySaleRequest request) {
        //流水校验
        BlindBoxInventoryStream existStream = blindBoxInventoryStreamMapper.selectByIdentifier(request.identifier(), request.eventType().name(), request.goodsId());
        if (null != existStream) {
            return true;
        }

        //查询出最新的值
        BlindBox blindBox = this.getById(request.goodsId());

        //新增collection流水
        BlindBoxInventoryStream stream = new BlindBoxInventoryStream(blindBox, request.identifier(), request.eventType(), request.quantity());
        int result = blindBoxInventoryStreamMapper.insert(stream);
        Assert.isTrue(result > 0, () -> new BlindBoxException(BLIND_BOX_STREAM_SAVE_FAILED));

        //核心逻辑执行
        result = blindBoxMapper.trySaleWithoutHint(request.goodsId(), request.quantity());
        Assert.isTrue(result == 1, () -> new BlindBoxException(BLIND_BOX_SAVE_FAILED));
        return true;
    }

    @SuppressWarnings("AliDeprecation")
    @Transactional(rollbackFor = Exception.class)
    @Override
    @Deprecated
    public GoodsSaleResponse confirmSale(GoodsConfirmSaleRequest request) {
        //流水校验
        BlindBoxInventoryStream existStream = blindBoxInventoryStreamMapper.selectByIdentifier(request.identifier(), request.eventType().name(), request.goodsId());
        if (null != existStream) {
            GoodsSaleResponse response = new GoodsSaleResponse();
            response.setSuccess(true);
            return response;
        }

        BlindBox blindBox = this.getById(request.goodsId());

        //新增blindBox流水
        BlindBoxInventoryStream stream = new BlindBoxInventoryStream(blindBox, request.identifier(), request.eventType(), request.quantity());
        stream.setOccupiedInventory(blindBox.getOccupiedInventory() + request.quantity());

        int result = blindBoxInventoryStreamMapper.insert(stream);
        Assert.isTrue(result > 0, () -> new BlindBoxException(BLIND_BOX_STREAM_SAVE_FAILED));

        result = blindBoxMapper.confirmSale(request.goodsId(), blindBox.getOccupiedInventory(), request.quantity());
        Assert.isTrue(result == 1, () -> new BlindBoxException(BLIND_BOX_SAVE_FAILED));

        //调用分配的规则进行分配
        BlindAllotBoxRule ruleName = blindBox.getAllocateRule();
        BlindBoxBindMatchRequest matchRequest = new BlindBoxBindMatchRequest();
        matchRequest.setBlindBoxId(request.goodsId());
        Long blindBoxItemId = blindBoxRuleServiceFactory.get(ruleName).match(matchRequest);
        Assert.notNull(blindBoxItemId, () -> new BlindBoxException(BLIND_BOX_ITEM_ALLOCATE_FAILED));

        //更新blindBoxItem状态
        BlindBoxItem blindBoxItem = new BlindBoxItem();
        blindBoxItem.setId(blindBoxItemId);
        blindBoxItem.assign(request, blindBox);
        boolean updateResult = blindBoxItemService.updateById(blindBoxItem);
        Assert.isTrue(updateResult, () -> new BlindBoxException(BLIND_BOX_UPDATE_FAILED));

        stream.addBlindBoxItemId(blindBoxItem.getId());
        int res = blindBoxInventoryStreamMapper.updateById(stream);
        Assert.isTrue(res > 0, () -> new BlindBoxException(BLIND_BOX_STREAM_SAVE_FAILED));

        GoodsSaleResponse blindBoxConfirmSaleResponse = new GoodsSaleResponse();
        blindBoxConfirmSaleResponse.setSuccess(true);
        return blindBoxConfirmSaleResponse;
    }

    @Override
    public Boolean assign(BlindBoxAssignRequest request) {
        BlindBox blindBox = this.getById(request.getBlindBoxId());
        //调用分配的规则进行分配
        BlindAllotBoxRule ruleName = blindBox.getAllocateRule();
        BlindBoxBindMatchRequest matchRequest = new BlindBoxBindMatchRequest();
        matchRequest.setBlindBoxId(request.getBlindBoxId());
        Long blindBoxItemId = blindBoxRuleServiceFactory.get(ruleName).match(matchRequest);
        Assert.notNull(blindBoxItemId, () -> new BlindBoxException(BLIND_BOX_ITEM_ALLOCATE_FAILED));

        //更新blindBoxItem状态
        BlindBoxItem blindBoxItem = new BlindBoxItem();
        blindBoxItem.setId(blindBoxItemId);
        blindBoxItem.assign(request, blindBox);
        boolean updateResult = blindBoxItemService.updateById(blindBoxItem);
        Assert.isTrue(updateResult, () -> new BlindBoxException(BLIND_BOX_UPDATE_FAILED));
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean cancelSale(GoodsCancelSaleRequest request) {
        //流水校验
        BlindBoxInventoryStream existStream = blindBoxInventoryStreamMapper.selectByIdentifier(request.identifier(), request.eventType().name(), request.collectionId());
        if (null != existStream) {
            return true;
        }

        //查询出最新的值
        BlindBox blindBox = this.getById(request.collectionId());

        //新增collection流水
        BlindBoxInventoryStream stream = new BlindBoxInventoryStream(blindBox, request.identifier(), request.eventType(), request.quantity());
        int result = blindBoxInventoryStreamMapper.insert(stream);
        Assert.isTrue(result > 0, () -> new BlindBoxException(BLIND_BOX_STREAM_SAVE_FAILED));

        //核心逻辑执行
        result = blindBoxMapper.cancelSale(request.collectionId(), request.quantity());
        Assert.isTrue(result == 1, () -> new BlindBoxException(BLIND_BOX_SAVE_FAILED));
        return true;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheInvalidate(name = ":blindBox:cache:id:", key = "#blindBox.id")
    public boolean updateById(BlindBox blindBox) {
        var saveResult = super.updateById(blindBox);
        Assert.isTrue(saveResult, () -> new BlindBoxException(BLIND_BOX_SAVE_FAILED));
        return true;
    }


    @Override
    @Cached(name = ":blindBox:cache:id:", expire = 60, localExpire = 10, timeUnit = TimeUnit.MINUTES, cacheType = CacheType.BOTH, key = "#blindBoxId", cacheNullValue = true)
    @CacheRefresh(refresh = 50, timeUnit = TimeUnit.MINUTES)
    public BlindBox queryById(Long blindBoxId) {
        return this.getById(blindBoxId);
    }

}
