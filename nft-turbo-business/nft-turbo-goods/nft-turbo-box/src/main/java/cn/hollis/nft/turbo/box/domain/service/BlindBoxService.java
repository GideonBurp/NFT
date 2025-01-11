package cn.hollis.nft.turbo.box.domain.service;

import cn.hollis.nft.turbo.api.box.request.BlindBoxCreateRequest;
import cn.hollis.nft.turbo.api.goods.request.GoodsCancelSaleRequest;
import cn.hollis.nft.turbo.api.goods.request.GoodsConfirmSaleRequest;
import cn.hollis.nft.turbo.api.goods.request.GoodsTrySaleRequest;
import cn.hollis.nft.turbo.api.goods.response.GoodsSaleResponse;
import cn.hollis.nft.turbo.base.response.PageResponse;
import cn.hollis.nft.turbo.box.domain.entity.BlindBox;
import cn.hollis.nft.turbo.box.domain.request.BlindBoxAssignRequest;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 盲盒服务
 *
 * @author Hollis
 */
public interface BlindBoxService extends IService<BlindBox> {
    /**
     * 创建
     *
     * @param request
     * @return
     */
    public BlindBox create(BlindBoxCreateRequest request);

    /**
     * 尝试售卖
     *
     * @param request
     * @return
     */
    public Boolean trySale(GoodsTrySaleRequest request);

    /**
     * 尝试售卖-无hint版
     *
     * @param request
     * @return
     */
    public Boolean trySaleWithoutHint(GoodsTrySaleRequest request);

    /**
     * 确认售卖
     *
     * @param request
     * @return
     * @deprecated 废弃，这个方法之前是依赖数据库做的藏品的序号的生成，但是这里存在并发问题。
     * 当然也可以基于乐观锁/悲观锁的方式解决，但是会影响吞吐量，所以改用其他方式实现
     */
    @Deprecated
    public GoodsSaleResponse confirmSale(GoodsConfirmSaleRequest request);

    /**
     * 盲盒分配
     *
     * @param request
     * @return
     */
    public Boolean assign(BlindBoxAssignRequest request);


    /**
     * 取消售卖
     *
     * @param request
     * @return
     */
    public Boolean cancelSale(GoodsCancelSaleRequest request);

    /**
     * 查询
     *
     * @param blindBoxId
     * @return
     */
    public BlindBox queryById(Long blindBoxId);

    /**
     * 分页查询
     *
     * @param keyWord
     * @param state
     * @param currentPage
     * @param pageSize
     * @return
     */
    public PageResponse<BlindBox> pageQueryByState(String keyWord, String state, int currentPage, int pageSize);
}
