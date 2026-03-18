package cn.gideon.nft.turbo.user.domain.service;

/**
 * 认证服务
 *
 * @author gideon
 */
public interface AuthService {
    /**
     * 校验认证信息
     *
     * @param realName
     * @param idCard
     * @return
     */
    public boolean checkAuth(String realName, String idCard);
}
