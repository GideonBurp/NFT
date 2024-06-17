package pay;

import cn.hollis.nft.turbo.api.pay.constant.PayChannel;
import cn.hollis.nft.turbo.pay.infrastructure.channel.common.request.PayChannelRequest;
import cn.hollis.nft.turbo.pay.infrastructure.channel.wechat.response.WxPayChannelResponse;
import cn.hollis.nft.turbo.pay.infrastructure.channel.common.service.PayChannelService;
import cn.hollis.nft.turbo.pay.infrastructure.channel.common.service.PayChannelServiceFactory;
import com.ijpay.core.kit.PayKit;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class WXpayServiceTest extends PayBaseTest {


    @Autowired
    private PayChannelServiceFactory payChannelServiceFactory;



    @Test
    public void wxPayTest(){
        PayChannelService wxPayChannelService = payChannelServiceFactory.get(PayChannel.WECHAT);
        PayChannelRequest payChannelRequest =new PayChannelRequest();
        payChannelRequest.setOrderId(PayKit.generateStr());
        payChannelRequest.setAmount(1L);
        payChannelRequest.setDescription("支付测试");
        payChannelRequest.setAttach("支付测试");
        WxPayChannelResponse response= (WxPayChannelResponse) wxPayChannelService.pay(payChannelRequest);
        Assert.assertTrue(response.getSuccess());
    }

}
