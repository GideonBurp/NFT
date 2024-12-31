package cn.hollis.nft.turbo.api.box.request;

import cn.hollis.nft.turbo.api.goods.constant.GoodsEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author wswyb001
 * @date 2024/01/17
 */

@Setter
@Getter
@ToString
@AllArgsConstructor
public class BlindBoxBindRequest extends BaseBlindBoxRequest {


    @Override
    public GoodsEvent getEventType() {
        return GoodsEvent.BIND;
    }
}
