package cn.hollis.nft.turbo.collection.domain.service;

import java.math.BigDecimal;
import java.util.Date;

import cn.hollis.nft.turbo.api.collection.constant.HeldCollectionState;
import cn.hollis.nft.turbo.api.collection.request.CollectionChainRequest;
import cn.hollis.nft.turbo.collection.CollectionBaseTest;
import cn.hollis.nft.turbo.collection.domain.entity.Collection;
import cn.hollis.nft.turbo.collection.domain.request.HeldCollectionCreateRequest;
import cn.hollis.nft.turbo.collection.domain.request.HeldCollectionDestroyRequest;
import cn.hollis.nft.turbo.collection.domain.request.HeldCollectionTransferRequest;
import cn.hollis.nft.turbo.collection.domain.service.impl.HeldCollectionService;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class HeldCollectionServiceTest extends CollectionBaseTest {
    @Autowired
    private HeldCollectionService heldCollectionService;

    @Autowired
    private CollectionService collectionService;

    @Test
    public void serviceTest() {
        CollectionChainRequest request =new CollectionChainRequest();
        request.setIdentifier("123456");
        request.setClassId("classId");
        request.setName("name");
        request.setCover("cover");
        request.setPrice(BigDecimal.ONE);
        request.setQuantity(100L);
        request.setCreateTime(new Date());
        request.setSaleTime(new Date());
        Collection collection=collectionService.create(request);
        Assert.assertTrue(collection.getId()!=null);

        //create
        HeldCollectionCreateRequest mintRequest =new HeldCollectionCreateRequest();
        mintRequest.setCollectionId(collection.getId());
        mintRequest.setIdentifier("123");
        mintRequest.setSerialNo("12345");
        mintRequest.setUserId(1L);
        var heldCollection= heldCollectionService.create(mintRequest);
        Assert.assertTrue(heldCollection.getId()!=null);
        Assert.assertTrue(StringUtils.equals(heldCollection.getState(), HeldCollectionState.INIT.name()));
        //transfer
        HeldCollectionTransferRequest transferRequest=new HeldCollectionTransferRequest();
        transferRequest.setHeldCollectionId(heldCollection.getId());
        transferRequest.setIdentifier("345");
        transferRequest.setBuyerId(2L);
        transferRequest.setSellerId(1L);
        var newHeldCollection= heldCollectionService.transfer(transferRequest);
        Assert.assertTrue(newHeldCollection.getId()!=null);
        Assert.assertTrue(StringUtils.equals(newHeldCollection.getState(), HeldCollectionState.INIT.name()));
        var oldHeldCollection= heldCollectionService.queryByCollectionIdAndSerialNo(heldCollection.getCollectionId(),
            heldCollection.getSerialNo());
        Assert.assertTrue(StringUtils.equals(oldHeldCollection.getState(), HeldCollectionState.INACTIVED.name()));
        //destroy
        HeldCollectionDestroyRequest destroyRequest=new HeldCollectionDestroyRequest();
        destroyRequest.setHeldCollectionId(newHeldCollection.getId());
        destroyRequest.setIdentifier("456");
        var destroyHeldCollection= heldCollectionService.destroy(destroyRequest);
        Assert.assertTrue(destroyHeldCollection.getId()!=null);
        Assert.assertTrue(StringUtils.equals(destroyHeldCollection.getState(), HeldCollectionState.DESTROYED.name()));
    }
}
