package cn.hollis.nft.turbo.pay.job;

import cn.hollis.nft.turbo.api.pay.constant.PayRefundOrderState;
import cn.hollis.nft.turbo.base.utils.MoneyUtils;
import cn.hollis.nft.turbo.pay.domain.entity.RefundOrder;
import cn.hollis.nft.turbo.pay.domain.service.RefundOrderService;
import cn.hollis.nft.turbo.pay.infrastructure.channel.common.request.RefundChannelRequest;
import cn.hollis.nft.turbo.pay.infrastructure.channel.common.response.RefundChannelResponse;
import cn.hollis.nft.turbo.pay.infrastructure.channel.common.service.PayChannelServiceFactory;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * 退款重试任务
 *
 * @author Hollis
 */
@Component
public class RefundOrderRetryJob {

    @Autowired
    private RefundOrderService refundOrderService;

    @Autowired
    @Lazy
    private PayChannelServiceFactory payChannelServiceFactory;

    private static final int PAGE_SIZE = 100;

    private static final Logger LOG = LoggerFactory.getLogger(RefundOrderRetryJob.class);

    @XxlJob("refundOrderRetryJob")
    public ReturnT<String> execute() {

        int currentPage = 1;
        Page<RefundOrder> page = refundOrderService.pageQueryNeedRetryOrders(currentPage, PAGE_SIZE);

        page.getRecords().forEach(this::executeSingle);

        while (page.hasNext()) {
            currentPage++;
            page = refundOrderService.pageQueryNeedRetryOrders(currentPage, PAGE_SIZE);
            page.getRecords().forEach(this::executeSingle);
        }

        return ReturnT.SUCCESS;
    }

    private void executeSingle(RefundOrder refundOrder) {
        LOG.info("start to execute refund , orderId is {}", refundOrder.getPayOrderId());

        RefundChannelRequest refundChannelRequest = new RefundChannelRequest();
        refundChannelRequest.setRefundOrderId(refundOrder.getRefundOrderId());
        refundChannelRequest.setPaidAmount(MoneyUtils.yuanToCent(refundOrder.getPaidAmount()));
        refundChannelRequest.setPayChannelStreamId(refundOrder.getPayChannelStreamId());
        refundChannelRequest.setPayOrderId(refundOrder.getPayOrderId());
        refundChannelRequest.setRefundAmount(MoneyUtils.yuanToCent(refundOrder.getApplyRefundAmount()));
        refundChannelRequest.setRefundReason(refundOrder.getMemo());

        RefundChannelResponse refundChannelResponse = payChannelServiceFactory.get(refundOrder.getRefundChannel()).refund(refundChannelRequest);

        if (refundOrder.getRefundOrderState() == PayRefundOrderState.TO_REFUND && refundChannelResponse.getSuccess()) {
            refundOrderService.refunding(refundOrder.getRefundOrderId());
        }
    }
}
