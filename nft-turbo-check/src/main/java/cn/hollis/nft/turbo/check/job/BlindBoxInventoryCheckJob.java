package cn.hollis.nft.turbo.check.job;

import cn.hollis.nft.turbo.api.check.request.InventoryCheckRequest;
import cn.hollis.nft.turbo.api.check.response.InventoryCheckResponse;
import cn.hollis.nft.turbo.api.check.service.InventoryCheckFacadeService;
import cn.hollis.nft.turbo.api.goods.constant.GoodsEvent;
import cn.hollis.nft.turbo.api.goods.constant.GoodsType;
import cn.hollis.nft.turbo.api.goods.service.GoodsFacadeService;
import cn.hollis.nft.turbo.api.inventory.request.InventoryRequest;
import cn.hollis.nft.turbo.api.inventory.service.InventoryFacadeService;
import cn.hollis.nft.turbo.base.response.MultiResponse;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.apache.commons.lang.time.DateUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

import static cn.hollis.nft.turbo.api.common.constant.CommonConstant.SEPARATOR;

/**
 * 库存一致性检查任务
 *
 * @author Hollis
 */
@Component
public class BlindBoxInventoryCheckJob {

    @DubboReference(version = "1.0.0")
    private InventoryFacadeService inventoryFacadeService;

    @DubboReference(version = "1.0.0")
    private GoodsFacadeService goodsFacadeService;

    @DubboReference(version = "1.0.0")
    private InventoryCheckFacadeService inventoryCheckFacadeService;

    @XxlJob("blindBoxInventoryCheckJob")
    public ReturnT<String> execute() {
        List<String> hotCollectionIds = goodsFacadeService.getHotGoods(GoodsType.BLIND_BOX.name());
        for (String hotCollectionId : hotCollectionIds) {
            InventoryRequest inventoryRequest = new InventoryRequest();
            inventoryRequest.setGoodsId(hotCollectionId);
            inventoryRequest.setGoodsType(GoodsType.BLIND_BOX);
            MultiResponse<String> inventoryLogs = inventoryFacadeService.getInventoryDecreaseLogs(inventoryRequest);

            for (String inventoryLog : inventoryLogs.getDatas()) {
                InventoryCheckRequest inventoryCheckRequest = new InventoryCheckRequest();
                JSONObject jsonObject = JSON.parseObject(inventoryLog);
                Date createTime = new Date(jsonObject.getLong("timestamp"));

                //只处理3秒钟之前的数据，避免出现清理后导致重复扣减
                if (DateUtils.addSeconds(createTime, 3).compareTo(new Date()) < 0) {
                    inventoryCheckRequest.setGoodsId(hotCollectionId);
                    inventoryCheckRequest.setGoodsType(GoodsType.BLIND_BOX);
                    inventoryCheckRequest.setGoodsEvent(GoodsEvent.TRY_SALE);
                    inventoryCheckRequest.setChangedQuantity(Integer.valueOf(jsonObject.getString("change")));
                    //内容为 <"DECREASE_1019222537308167987200003">，需要从中解析出具体的订单号
                    String identifier = jsonObject.getString("by");
                    inventoryCheckRequest.setIdentifier(identifier.substring(identifier.indexOf(SEPARATOR) + 1, identifier.lastIndexOf("\"")));
                    InventoryCheckResponse response = inventoryCheckFacadeService.check(inventoryCheckRequest);
                    //核对一致后清除redis中的流水
                    if (response.getSuccess() && response.getCheckResult()) {
                        inventoryRequest.setIdentifier(inventoryCheckRequest.getIdentifier());
                        inventoryFacadeService.removeInventoryDecreaseLog(inventoryRequest);
                    } else {
                        //todo 告警推送
                    }
                }
            }
        }

        return ReturnT.SUCCESS;
    }
}
