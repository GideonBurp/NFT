package cn.hollis.nft.turbo.api.user.service;

import cn.hollis.nft.turbo.api.user.request.UserActiveRequest;
import cn.hollis.nft.turbo.api.user.request.UserAuthRequest;
import cn.hollis.nft.turbo.api.user.request.UserModifyRequest;
import cn.hollis.nft.turbo.api.user.request.UserQueryRequest;
import cn.hollis.nft.turbo.api.user.request.UserRegisterRequest;
import cn.hollis.nft.turbo.api.user.response.UserOperatorResponse;
import cn.hollis.nft.turbo.api.user.response.UserQueryResponse;
import cn.hollis.nft.turbo.api.user.response.data.UserInfo;

/**
 * @author Hollis
 */
public interface UserFacadeService {

    /**
     * 用户信息查询
     * @param userLoginRequest
     * @return
     */
    UserQueryResponse<UserInfo> query(UserQueryRequest userLoginRequest);

    /**
     * 用户注册
     * @param userRegisterRequest
     * @return
     */
    UserOperatorResponse register(UserRegisterRequest userRegisterRequest);

    /**
     * 用户信息修改
     * @param userModifyRequest
     * @return
     */
    UserOperatorResponse modify(UserModifyRequest userModifyRequest);

    /**
     * 用户实名认证
     * @param userAuthRequest
     * @return
     */
    UserOperatorResponse auth(UserAuthRequest userAuthRequest);

    /**
     * 用户激活
     * @param userActiveRequest
     * @return
     */
    UserOperatorResponse active(UserActiveRequest userActiveRequest);

}
