package cn.gideon.nft.turbo.api.user.response;

import cn.gideon.nft.turbo.api.user.response.data.UserInfo;
import cn.gideon.nft.turbo.base.response.BaseResponse;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户操作响应
 *
 * @author Gideon
 */
@Getter
@Setter
public class UserOperatorResponse extends BaseResponse {

    private UserInfo user;
}
