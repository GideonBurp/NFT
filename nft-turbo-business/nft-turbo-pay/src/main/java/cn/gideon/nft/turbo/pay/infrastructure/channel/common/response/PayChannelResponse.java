package cn.gideon.nft.turbo.pay.infrastructure.channel.common.response;

import cn.gideon.nft.turbo.base.response.BaseResponse;
import lombok.Getter;
import lombok.Setter;

/**
 * @author wswyb001
 */
@Setter
@Getter
public class PayChannelResponse extends BaseResponse {
    protected String payUrl;
}
