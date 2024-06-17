package cn.hollis.nft.turbo.order.domain.validator;

import cn.hollis.nft.turbo.api.order.request.OrderCreateRequest;
import cn.hollis.nft.turbo.api.user.constant.UserRole;
import cn.hollis.nft.turbo.api.user.constant.UserStateEnum;
import cn.hollis.nft.turbo.api.user.request.UserQueryRequest;
import cn.hollis.nft.turbo.api.user.response.UserQueryResponse;
import cn.hollis.nft.turbo.api.user.response.data.UserInfo;
import cn.hollis.nft.turbo.api.user.service.UserFacadeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 用户校验器
 *
 * @author hollis
 */
@Component
public class UserValidator implements OrderCreateValidator {

    private OrderCreateValidator nextValidator;

    @Autowired
    private UserFacadeService userFacadeService;

    @Override
    public void setNext(OrderCreateValidator nextValidator) {
        this.nextValidator = nextValidator;
    }

    @Override
    public void validate(OrderCreateRequest request) throws Exception {
        String buyerId = request.getBuyerId();
        UserQueryRequest userQueryRequest = new UserQueryRequest();
        userQueryRequest.setUserId(Long.valueOf(buyerId));
        UserQueryResponse<UserInfo> userQueryResponse = userFacadeService.query(userQueryRequest);
        if (userQueryResponse.getSuccess() && userQueryResponse.getData() != null) {
            UserInfo userInfo = userQueryResponse.getData();
            if (userInfo.getUserRole() != null && !userInfo.getUserRole().equals(UserRole.CUSTOMER)) {
                throw new Exception("买家不能是平台用户");
            }
            //判断买家状态
            if (userInfo.getState() != null && !userInfo.getState().equals(UserStateEnum.ACTIVE.name())) {
                throw new Exception("买家状态异常");
            }
            //判断买家状态
            if (userInfo.getState() != null && !userInfo.getCertification()) {
                throw new Exception("买家未完成实名认证");
            }
        }
    }
}
