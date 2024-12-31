package cn.hollis.nft.turbo.api.box.request;

import cn.hollis.nft.turbo.base.request.PageRequest;
import lombok.Getter;
import lombok.Setter;

/**
 * @author wswyb001
 */
@Getter
@Setter
public class BlindBoxPageQueryRequest extends PageRequest {

    private String state;

    private String keyword;
}
