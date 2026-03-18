package cn.gideon.nft.turbo.pay.infrastructure.channel.common.request;

import cn.gideon.nft.turbo.base.request.BaseRequest;
import lombok.*;

/**
 * 账单下载参数
 *
 * @author Gideon
 * @date 2025/07/01
 */

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class DownloadBillChannelRequest extends BaseRequest {

    /**
     * 账单token
     */
    private String token;
}
