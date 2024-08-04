package cn.hollis.nft.turbo.admin.controller;

import cn.dev33.satoken.stp.SaLoginModel;
import cn.dev33.satoken.stp.StpUtil;
import cn.hollis.nft.turbo.admin.infrastructure.exception.AdminException;
import cn.hollis.nft.turbo.admin.param.AdminLoginParam;
import cn.hollis.nft.turbo.admin.vo.AdminLoginVO;
import cn.hollis.nft.turbo.api.user.constant.UserRole;
import cn.hollis.nft.turbo.api.user.request.UserPageQueryRequest;
import cn.hollis.nft.turbo.api.user.request.UserQueryRequest;
import cn.hollis.nft.turbo.api.user.request.UserRegisterRequest;
import cn.hollis.nft.turbo.api.user.response.UserOperatorResponse;
import cn.hollis.nft.turbo.api.user.response.UserQueryResponse;
import cn.hollis.nft.turbo.api.user.response.data.UserInfo;
import cn.hollis.nft.turbo.api.user.service.UserFacadeService;
import cn.hollis.nft.turbo.api.user.service.UserManageFacadeService;
import cn.hollis.nft.turbo.base.response.PageResponse;
import cn.hollis.nft.turbo.web.util.MultiResultConvertor;
import cn.hollis.nft.turbo.web.vo.MultiResult;
import cn.hollis.nft.turbo.web.vo.Result;
import cn.hutool.crypto.digest.DigestUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.*;

import static cn.hollis.nft.turbo.admin.infrastructure.exception.AdminErrorCode.ADMIN_USER_NOT_EXIST;
import static cn.hollis.nft.turbo.admin.infrastructure.exception.AdminErrorCode.ADMIN_USER_PASSWORD_ERROR;

/**
 * 用户后台管理
 *
 * @author Hollis
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("admin/user")
@CrossOrigin(origins = "*")
public class UserAdminController {

    @DubboReference(version = "1.0.0")
    private UserFacadeService userFacadeService;

    @DubboReference(version = "1.0.0")
    private UserManageFacadeService userManageFacadeService;

    /**
     * 默认登录超时时间：7天
     */
    private static final Integer DEFAULT_LOGIN_SESSION_TIMEOUT = 60 * 60 * 24 * 7;

    @GetMapping("/getUserInfo")
    public Result<UserInfo> getUserInfo() {
        String userId = (String) StpUtil.getLoginId();
        UserQueryRequest request = new UserQueryRequest();
        request.setUserId(Long.valueOf(userId));
        UserQueryResponse<UserInfo> userQueryResponse = userFacadeService.query(request);
        UserInfo userInfo = userQueryResponse.getData();

        if (userInfo == null) {
            throw new AdminException(ADMIN_USER_NOT_EXIST);
        }
        return Result.success(userInfo);
    }

    @GetMapping("/userList")
    public MultiResult<UserInfo> userList(@NotBlank String state, String keyWord, int pageSize, int currentPage) {
        UserPageQueryRequest userPageQueryRequest = new UserPageQueryRequest();
        userPageQueryRequest.setState(state);
        userPageQueryRequest.setKeyWord(keyWord);
        userPageQueryRequest.setCurrentPage(currentPage);
        userPageQueryRequest.setPageSize(pageSize);
        PageResponse<UserInfo> pageResponse = userFacadeService.pageQuery(userPageQueryRequest);
        return MultiResultConvertor.convert(pageResponse);
    }

    @PostMapping("/registerAdmin")
    public Result<Boolean> registerAdmin(@Valid String phone) {
        //查询用户信息
        UserQueryRequest userQueryRequest = new UserQueryRequest();
        userQueryRequest.setTelephone(phone);
        UserQueryResponse<UserInfo> userQueryResponse = userFacadeService.query(userQueryRequest);
        UserInfo userInfo = userQueryResponse.getData();
        //用户不存在或者不是管理员用户
        if (userInfo == null || !userInfo.getUserRole().equals(UserRole.ADMIN)) {
            //需要注册
            UserRegisterRequest userRegisterRequest = new UserRegisterRequest();
            userRegisterRequest.setTelephone(phone);
            userRegisterRequest.setPassword(phone);
            userManageFacadeService.registerAdmin(userRegisterRequest);
        }
        return Result.success(true);
    }

    /**
     * 登录方法
     *
     * @param loginParam 登录信息
     * @return 结果
     */
    @PostMapping("/login")
    public Result<AdminLoginVO> login(@Valid @RequestBody AdminLoginParam loginParam) {

        //查询用户信息
        UserQueryRequest userQueryRequest = new UserQueryRequest();
        userQueryRequest.setTelephone(loginParam.getTelephone());
        UserQueryResponse<UserInfo> userQueryResponse = userFacadeService.query(userQueryRequest);
        UserInfo userInfo = userQueryResponse.getData();
        //用户不存在或者不是管理员用户，不能登陆
        if (userInfo == null || !userInfo.getUserRole().equals(UserRole.ADMIN)) {
            return Result.error(ADMIN_USER_NOT_EXIST.getCode(), ADMIN_USER_NOT_EXIST.getMessage());
        } else {
            if (StringUtils.equals(DigestUtil.md5Hex(loginParam.getPassword()), userInfo.getPasswordHash())) {
                //登录
                StpUtil.login(userInfo.getUserId(), new SaLoginModel().setIsLastingCookie(loginParam.getRememberMe()).setTimeout(DEFAULT_LOGIN_SESSION_TIMEOUT));
                StpUtil.getSession().set(userInfo.getUserId().toString(), userInfo);
                AdminLoginVO loginVO = new AdminLoginVO(userInfo);
                return Result.success(loginVO);
            } else {
                return Result.error(ADMIN_USER_PASSWORD_ERROR.getCode(), ADMIN_USER_PASSWORD_ERROR.getMessage());
            }
        }
    }

    @PostMapping("/logout")
    public Result<Boolean> logout() {
        StpUtil.logout();
        return Result.success(true);
    }

    @PostMapping("/freeze")
    public Result<UserOperatorResponse> freeze(@Valid Long userId) {
        String adminUserId = (String) StpUtil.getLoginId();
        //查询用户信息
        UserQueryRequest adminQueryRequest = new UserQueryRequest();
        adminQueryRequest.setUserId(Long.valueOf(adminUserId));
        UserQueryResponse<UserInfo> userQueryResponse = userFacadeService.query(adminQueryRequest);
        UserInfo userInfo = userQueryResponse.getData();
        //用户不存在或者不是管理员用户
        if (userInfo == null || !userInfo.getUserRole().equals(UserRole.ADMIN)) {
            return Result.error(ADMIN_USER_NOT_EXIST.getCode(), ADMIN_USER_NOT_EXIST.getMessage());
        }
        var res = userManageFacadeService.freeze(userId);

        //重新查出用户信息，更新登录的session，确保用户权限实时更新
        refreshUserInSession(userId);

        return Result.success(res);
    }

    @PostMapping("/unfreeze")
    public Result<UserOperatorResponse> unfreeze(@Valid Long userId) {
        String adminUserId = (String) StpUtil.getLoginId();
        //查询用户信息
        UserQueryRequest adminQueryRequest = new UserQueryRequest();
        adminQueryRequest.setUserId(Long.valueOf(adminUserId));
        UserQueryResponse<UserInfo> userQueryResponse = userFacadeService.query(adminQueryRequest);
        UserInfo userInfo = userQueryResponse.getData();
        //用户不存在或者不是管理员用户
        if (userInfo == null || !userInfo.getUserRole().equals(UserRole.ADMIN)) {
            return Result.error(ADMIN_USER_NOT_EXIST.getCode(), ADMIN_USER_NOT_EXIST.getMessage());
        }
        var res = userManageFacadeService.unfreeze(userId);

        //重新查出用户信息，更新登录的session，确保用户权限实时更新
        refreshUserInSession(userId);
        return Result.success(res);
    }

    private void refreshUserInSession(Long userId) {
        UserQueryRequest userQueryRequest = new UserQueryRequest();
        userQueryRequest.setUserId(userId);
        UserQueryResponse userQueryResponse = userFacadeService.query(userQueryRequest);
        StpUtil.getSession().set(userId.toString(), userQueryResponse.getData());
    }

}
