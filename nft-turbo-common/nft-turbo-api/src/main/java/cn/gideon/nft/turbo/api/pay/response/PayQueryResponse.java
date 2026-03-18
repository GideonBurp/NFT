package cn.gideon.nft.turbo.api.pay.response;

import cn.gideon.nft.turbo.api.pay.model.PayOrderVO;
import cn.gideon.nft.turbo.base.response.BaseResponse;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author Gideon
 */
@Getter
@Setter
public class PayQueryResponse extends BaseResponse {

    private List<PayOrderVO> payOrders;
}
