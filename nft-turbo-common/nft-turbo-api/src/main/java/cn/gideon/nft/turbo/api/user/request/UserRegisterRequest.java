package cn.gideon.nft.turbo.api.user.request;

import cn.gideon.nft.turbo.base.request.BaseRequest;
import lombok.*;

/**
 * @author Gideon
 */
@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class UserRegisterRequest extends BaseRequest {

    private String telephone;

    private String inviteCode;

    private String password;

}
