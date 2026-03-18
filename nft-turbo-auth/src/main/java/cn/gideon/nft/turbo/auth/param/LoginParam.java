package cn.gideon.nft.turbo.auth.param;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Gideon
 */
@Setter
@Getter
public class LoginParam extends RegisterParam {

    /**
     * 记住我
     */
    private Boolean rememberMe;
}
