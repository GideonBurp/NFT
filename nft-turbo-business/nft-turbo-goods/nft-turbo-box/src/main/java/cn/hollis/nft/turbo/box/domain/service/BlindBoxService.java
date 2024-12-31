package cn.hollis.nft.turbo.box.domain.service;

import cn.hollis.nft.turbo.api.box.request.BlindBoxCreateRequest;
import cn.hollis.nft.turbo.api.goods.request.GoodsCancelSaleRequest;
import cn.hollis.nft.turbo.api.goods.request.GoodsConfirmSaleRequest;
import cn.hollis.nft.turbo.api.goods.request.GoodsTrySaleRequest;
import cn.hollis.nft.turbo.api.goods.response.GoodsSaleResponse;
import cn.hollis.nft.turbo.base.response.PageResponse;
import cn.hollis.nft.turbo.box.domain.entity.BlindBox;
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
     */
    public GoodsSaleResponse confirmSale(GoodsConfirmSaleRequest request);


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
