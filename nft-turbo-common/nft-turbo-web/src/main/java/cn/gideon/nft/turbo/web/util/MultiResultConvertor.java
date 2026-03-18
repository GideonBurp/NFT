package cn.gideon.nft.turbo.web.util;

import cn.gideon.nft.turbo.base.response.PageResponse;
import cn.gideon.nft.turbo.web.vo.MultiResult;

import static cn.gideon.nft.turbo.base.response.ResponseCode.SUCCESS;

/**
 * @author Gideon
 */
public class MultiResultConvertor {

    public static <T> MultiResult<T> convert(PageResponse<T> pageResponse) {
        MultiResult<T> multiResult = new MultiResult<T>(true, SUCCESS.name(), SUCCESS.name(), pageResponse.getDatas(), pageResponse.getTotal(), pageResponse.getCurrentPage(), pageResponse.getPageSize());
        return multiResult;
    }
}
