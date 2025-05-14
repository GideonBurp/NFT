package cn.hollis.nft.turbo.check.job;

import cn.hollis.nft.turbo.api.check.service.InventoryCheckFacadeService;
import cn.hollis.nft.turbo.api.goods.service.GoodsFacadeService;
import cn.hollis.nft.turbo.api.inventory.service.InventoryFacadeService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Hollis
 */
@Component
public class CollectionInventoryAndOrderCheckJob {

    @Autowired
    private InventoryFacadeService inventoryFacadeService;

    @Autowired
    private GoodsFacadeService goodsFacadeService;

    @Autowired
    private InventoryCheckFacadeService inventoryCheckFacadeService;

    @XxlJob("inventoryAndOrderCheckJob")
    public ReturnT<String> execute() {

        //todo

        return ReturnT.SUCCESS;
    }
}
