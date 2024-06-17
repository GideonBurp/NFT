package cn.hollis.nft.turbo.pay.controller;

import cn.hollis.nft.turbo.api.pay.constant.PayChannel;
import cn.hollis.nft.turbo.pay.infrastructure.channel.common.request.PayChannelRequest;
import cn.hollis.nft.turbo.pay.infrastructure.channel.wechat.response.WxPayChannelResponse;
import cn.hollis.nft.turbo.pay.infrastructure.channel.common.service.PayChannelService;
import cn.hollis.nft.turbo.pay.infrastructure.channel.common.service.PayChannelServiceFactory;
import com.ijpay.core.kit.PayKit;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 微信支付回调入口
 *
 * @author hollis
 */
@Slf4j
@Controller
@RequestMapping("/wxPay")
public class WxPayController {

    @Autowired
    private PayChannelServiceFactory payChannelServiceFactory;

    @RequestMapping("/nativePay")
    @ResponseBody
    public String nativePay() {
        PayChannelService wxPayChannelService = payChannelServiceFactory.get(PayChannel.WECHAT);
        PayChannelRequest payChannelRequest = new PayChannelRequest();
        payChannelRequest.setOrderId(PayKit.generateStr());
        payChannelRequest.setAmount(1L);
        payChannelRequest.setDescription("支付测试");
        payChannelRequest.setAttach("支付测试");
        WxPayChannelResponse response = (WxPayChannelResponse) wxPayChannelService.pay(payChannelRequest);
        return response.getPayUrl();
    }

    @RequestMapping(value = "/payNotify", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public void payNotify(HttpServletRequest request, HttpServletResponse response) {
        PayChannelService wxPayChannelService = payChannelServiceFactory.get(PayChannel.WECHAT);
        boolean result = wxPayChannelService.notify(request, response);
        Assert.isTrue(result, "支付通知失败");
    }
}
