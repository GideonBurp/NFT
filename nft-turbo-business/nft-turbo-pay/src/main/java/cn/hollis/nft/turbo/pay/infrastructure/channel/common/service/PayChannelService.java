package cn.hollis.nft.turbo.pay.infrastructure.channel.common.service;

import cn.hollis.nft.turbo.pay.infrastructure.channel.common.response.PayChannelResponse;
import cn.hollis.nft.turbo.pay.infrastructure.channel.common.request.PayChannelRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 支付渠道服务
 *
 * @author Hollis
 */
public interface PayChannelService {
    /**
     * 支付
     *
     * @param payChannelRequest
     * @return
     */
    PayChannelResponse pay(PayChannelRequest payChannelRequest);

    /**
     * 支付结果回调
     *
     * @param request
     * @param response
     * @return 通知结果
     */
    boolean notify(HttpServletRequest request, HttpServletResponse response);
}
