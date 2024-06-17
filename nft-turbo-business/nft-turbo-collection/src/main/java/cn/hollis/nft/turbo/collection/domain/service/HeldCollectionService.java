package cn.hollis.nft.turbo.collection.domain.service;

import java.util.List;

import cn.hollis.nft.turbo.api.collection.constant.HeldCollectionState;
import cn.hollis.nft.turbo.api.collection.model.HeldCollectionDTO;
import cn.hollis.nft.turbo.collection.domain.constant.HeldCollectionEventType;
import cn.hollis.nft.turbo.collection.domain.entity.HeldCollection;
import cn.hollis.nft.turbo.collection.domain.entity.convertor.HeldCollectionConvertor;
import cn.hollis.nft.turbo.collection.domain.request.HeldCollectionActiveRequest;
import cn.hollis.nft.turbo.collection.domain.request.HeldCollectionCreateRequest;
import cn.hollis.nft.turbo.collection.domain.request.HeldCollectionDestroyRequest;
import cn.hollis.nft.turbo.collection.domain.request.HeldCollectionTransferRequest;
import cn.hollis.nft.turbo.collection.exception.CollectionException;
import cn.hollis.nft.turbo.collection.infrastructure.mapper.HeldCollectionMapper;
import cn.hollis.turbo.stream.producer.StreamProducer;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import static cn.hollis.nft.turbo.collection.exception.CollectionErrorCode.HELD_COLLECTION_QUERY_FAIL;
import static cn.hollis.nft.turbo.collection.exception.CollectionErrorCode.HELD_COLLECTION_SAVE_FAILED;

/**
 * 持有藏品服务
 *
 * @author hollis
 */
@Service
public class HeldCollectionService extends ServiceImpl<HeldCollectionMapper, HeldCollection> {

    @Autowired
    private StreamProducer streamProducer;

    public HeldCollection create(HeldCollectionCreateRequest request) {
        HeldCollection heldCollection = new HeldCollection();
        heldCollection.init(request);
        var saveResult = this.save(heldCollection);
        if (!saveResult) {
            throw new CollectionException(HELD_COLLECTION_SAVE_FAILED);
        }
        return heldCollection;
    }


    public Boolean active(HeldCollectionActiveRequest request) {
        HeldCollection heldCollection = getById(request.getHeldCollectionId());
        if (null == heldCollection) {
            throw new CollectionException(HELD_COLLECTION_QUERY_FAIL);
        }

        if (heldCollection.getState().equals(HeldCollectionState.ACTIVED.name())) {
            return true;
        }

        heldCollection.actived(request.getNftId(), request.getTxHash());
        boolean result = updateById(heldCollection);
        if (result) {
            sendMsg(heldCollection, request.getEventType());
        }
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public HeldCollection transfer(HeldCollectionTransferRequest request) {
        //先失效历史的持有数据
        HeldCollection oldHeldCollection = this.getById(request.getHeldCollectionId());
        if (oldHeldCollection == null) {
            throw new CollectionException(HELD_COLLECTION_QUERY_FAIL);
        }
        oldHeldCollection.inActived();
        var inActiveRes = this.updateById(oldHeldCollection);
        if (!inActiveRes) {
            throw new CollectionException(HELD_COLLECTION_SAVE_FAILED);
        }
        //再初始化新的持有数据
        HeldCollection newHeldCollection = new HeldCollection();
        newHeldCollection.transfer(oldHeldCollection.getCollectionId(), oldHeldCollection.getSerialNo(),
                String.valueOf(request.getSellerId()),
                String.valueOf(request.getBuyerId()), oldHeldCollection.getNftId());
        var newHeldSaveResult = this.save(newHeldCollection);
        if (!newHeldSaveResult) {
            throw new CollectionException(HELD_COLLECTION_SAVE_FAILED);
        }
        return newHeldCollection;

    }

    public HeldCollection destroy(HeldCollectionDestroyRequest request) {
        //查询持有数据
        HeldCollection heldCollection = this.getById(request.getHeldCollectionId());
        if (heldCollection == null) {
            throw new CollectionException(HELD_COLLECTION_QUERY_FAIL);
        }
        heldCollection.destroy();
        var saveResult = this.updateById(heldCollection);
        if (!saveResult) {
            throw new CollectionException(HELD_COLLECTION_SAVE_FAILED);
        }
        return heldCollection;
    }

    public HeldCollection queryByCollectionIdAndSerialNo(Long collectionId, String serialNo) {
        QueryWrapper<HeldCollection> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("collection_id", collectionId);
        queryWrapper.eq("serial_no", serialNo);
        List<HeldCollection> retList = list(queryWrapper);
        if (CollectionUtils.isEmpty(retList)) {
            return null;
        }
        return retList.get(0);
    }

    public HeldCollection queryByNftIdAndState(String nftId, String state) {
        QueryWrapper<HeldCollection> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("nft_id", nftId);
        queryWrapper.eq("state", state);
        List<HeldCollection> retList = list(queryWrapper);
        if (CollectionUtils.isEmpty(retList)) {
            return null;
        }
        return retList.get(0);
    }

    public Page<HeldCollection> pageQueryByState(String userId, String state, int currentPage, int pageSize) {
        Page<HeldCollection> page = new Page<>(currentPage, pageSize);
        QueryWrapper<HeldCollection> wrapper = new QueryWrapper<>();
        wrapper.eq("userId", userId);

        if (state != null) {
            wrapper.eq("state", state);
        }
        wrapper.orderBy(true, true, "gmt_create");

        return this.page(page, wrapper);
    }

    private boolean sendMsg(HeldCollection heldCollection, HeldCollectionEventType eventType) {
        HeldCollectionDTO heldCollectionDTO = HeldCollectionConvertor.INSTANCE.mapToDto(heldCollection);
        return streamProducer.send("heldCollection-out-0", eventType.name(), JSON.toJSONString(heldCollectionDTO));
    }
}
