package cn.hollis.nft.turbo.collection.domain.service;

import cn.hollis.nft.turbo.api.collection.request.CollectionCreateRequest;
import cn.hollis.nft.turbo.base.response.PageResponse;
import cn.hollis.nft.turbo.collection.domain.entity.Collection;
import cn.hollis.nft.turbo.collection.domain.response.CollectionConfirmSaleResponse;
import cn.hollis.nft.turbo.collection.facade.request.CollectionCancelSaleRequest;
import cn.hollis.nft.turbo.collection.facade.request.CollectionConfirmSaleRequest;
import cn.hollis.nft.turbo.collection.facade.request.CollectionTrySaleRequest;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 藏品服务
 *
 * @author Hollis
 */
public interface CollectionService extends IService<Collection> {
    /**
     * 创建
     *
     * @param request
     * @return
     */
    public Collection create(CollectionCreateRequest request);

    /**
     * 尝试售卖
     *
     * @param request
     * @return
     */
    public Boolean trySale(CollectionTrySaleRequest request);

    /**
     * 取消售卖
     *
     * @param request
     * @return
     */
    public Boolean cancelSale(CollectionCancelSaleRequest request);

    /**
     * 确认售卖
     *
     * @param request
     * @return
     */
    public CollectionConfirmSaleResponse confirmSale(CollectionConfirmSaleRequest request);

    /**
     * 查询
     * @param collectionId
     * @return
     */
    public Collection queryById(Long collectionId);

    /**
     * 分页查询
     *
     * @param keyWord
     * @param state
     * @param currentPage
     * @param pageSize
     * @return
     */
    public PageResponse<Collection> pageQueryByState(String keyWord, String state, int currentPage, int pageSize);

    /**
     * 更新并保存快照
     * @param collection
     * @return
     */
    public boolean updateAndSaveSnapshot(Collection collection);

}
