package cn.hollis.nft.turbo.order.domain.validator;

import cn.hollis.nft.turbo.api.goods.model.BaseGoodsInventoryVO;
import cn.hollis.nft.turbo.api.order.request.OrderCreateRequest;
import cn.hollis.nft.turbo.order.wrapper.InventoryWrapperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 库存校验器
 *
 * @author hollis
 */
@Component
public class StockValidator implements OrderCreateValidator {

    private OrderCreateValidator nextValidator;

    @Autowired
    private InventoryWrapperService inventoryWrapperService;

    @Override
    public void setNext(OrderCreateValidator nextValidator) {
        this.nextValidator = nextValidator;
    }

    @Override
    public void validate(OrderCreateRequest request) throws Exception {
        BaseGoodsInventoryVO goodsInventoryVO = inventoryWrapperService.queryInventory(request);

        if (goodsInventoryVO == null) {
            throw new Exception("库存不足");
        }

        if (goodsInventoryVO.getInventory() == 0) {
            throw new Exception("库存不足");
        }

        if (goodsInventoryVO.getQuantity() < request.getItemCount()) {
            throw new Exception("库存不足");
        }

        if (goodsInventoryVO.getInventory() < request.getItemCount()) {
            throw new Exception("库存不足");
        }
    }
}
