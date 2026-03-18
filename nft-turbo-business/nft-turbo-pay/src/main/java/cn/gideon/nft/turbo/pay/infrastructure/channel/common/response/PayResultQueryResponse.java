package cn.gideon.nft.turbo.pay.infrastructure.channel.common.response;

import cn.gideon.nft.turbo.base.response.BaseResponse;
import cn.gideon.nft.turbo.pay.infrastructure.channel.wechat.entity.WxPayNotifyEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * @author wswyb001
 */
@Setter
@Getter
public class PayResultQueryResponse extends BaseResponse {
    protected WxPayNotifyEntity wxPayNotifyEntity;
}
