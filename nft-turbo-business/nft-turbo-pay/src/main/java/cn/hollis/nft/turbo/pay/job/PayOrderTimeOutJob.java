package cn.hollis.nft.turbo.pay.job;

import cn.hollis.nft.turbo.pay.domain.entity.PayOrder;
import cn.hollis.nft.turbo.pay.domain.service.PayOrderService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Hollis
 */
@Component
public class PayOrderTimeOutJob {

    @Autowired
    private PayOrderService payOrderService;

    private static final int PAGE_SIZE = 100;

    private static final Logger LOG = LoggerFactory.getLogger(PayOrderTimeOutJob.class);

    @XxlJob("payTimeOutExecute")
    public ReturnT<String> execute() {

        List<PayOrder> payOrders = payOrderService.pageQueryTimeoutOrders(PAGE_SIZE, null);

        payOrders.forEach(this::executeSingle);

        while (CollectionUtils.isNotEmpty(payOrders)) {
            Long maxId = payOrders.stream().mapToLong(PayOrder::getId).max().orElse(Long.MAX_VALUE);
            payOrders = payOrderService.pageQueryTimeoutOrders(PAGE_SIZE, maxId + 1);
            payOrders.forEach(this::executeSingle);
        }

        return ReturnT.SUCCESS;
    }

    private void executeSingle(PayOrder payOrder) {
        LOG.info("start to execute order timeout , orderId is {}", payOrder.getPayOrderId());
        payOrderService.payExpired(payOrder.getPayOrderId());
    }
}
