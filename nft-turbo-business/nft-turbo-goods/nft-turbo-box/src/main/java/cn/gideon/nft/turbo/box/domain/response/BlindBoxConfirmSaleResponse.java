package cn.gideon.nft.turbo.box.domain.response;

import cn.gideon.nft.turbo.base.response.BaseResponse;
import cn.gideon.nft.turbo.box.domain.entity.BlindBox;
import cn.gideon.nft.turbo.box.domain.entity.BlindBoxItem;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Gideon
 */
@Setter
@Getter
public class BlindBoxConfirmSaleResponse extends BaseResponse {

    private BlindBox blindBox;

    private BlindBoxItem blindBoxItem;
}
