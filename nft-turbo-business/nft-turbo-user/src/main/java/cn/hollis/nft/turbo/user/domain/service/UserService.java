package cn.hollis.nft.turbo.user.domain.service;

import cn.hollis.nft.turbo.api.user.constant.UserOperateTypeEnum;
import cn.hollis.nft.turbo.api.user.constant.UserStateEnum;
import cn.hollis.nft.turbo.api.user.request.UserActiveRequest;
import cn.hollis.nft.turbo.api.user.request.UserAuthRequest;
import cn.hollis.nft.turbo.api.user.request.UserModifyRequest;
import cn.hollis.nft.turbo.api.user.request.UserQueryRequest;
import cn.hollis.nft.turbo.api.user.response.UserOperatorResponse;
import cn.hollis.nft.turbo.base.exception.BizException;
import cn.hollis.nft.turbo.base.exception.RepoErrorCode;
import cn.hollis.nft.turbo.lock.DistributeLock;
import cn.hollis.nft.turbo.user.domain.entity.User;
import cn.hollis.nft.turbo.user.domain.entity.convertor.UserConvertor;
import cn.hollis.nft.turbo.user.infrastructure.exception.UserErrorCode;
import cn.hollis.nft.turbo.user.infrastructure.exception.UserException;
import cn.hollis.nft.turbo.user.infrastructure.mapper.UserMapper;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheManager;
import com.alicp.jetcache.anno.CacheRefresh;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.alicp.jetcache.template.QuickConfig;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static cn.hollis.nft.turbo.user.infrastructure.exception.UserErrorCode.*;

/**
 * 用户服务
 *
 * @author hollis
 */
@Service
public class UserService extends ServiceImpl<UserMapper, User> implements InitializingBean {

    private static final String DEFAULT_NICK_NAME_PREFIX = "藏家_";

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserOperateStreamService userOperateStreamService;

    @Autowired
    private AuthService authService;

    @Autowired
    private RedissonClient redissonClient;

    private RBloomFilter<String> bloomFilter;

    @Autowired
    private CacheManager cacheManager;

    private Cache<String, User> telUserCache;

    private Cache<String, User> idUserCache;

    @Autowired
    private UserCacheDelayDeleteService userCacheDelayDeleteService;

    @PostConstruct
    public void init() {
        QuickConfig telQc = QuickConfig.newBuilder(":user:cache:tel:")
                .cacheType(CacheType.BOTH)
                .expire(Duration.ofMinutes(3000))
                .syncLocal(true)
                .build();
        telUserCache = cacheManager.getOrCreateCache(telQc);

        QuickConfig idQc = QuickConfig.newBuilder(":user:cache:id:")
                .cacheType(CacheType.BOTH)
                .expire(Duration.ofMinutes(3000))
                .syncLocal(true)
                .build();
        idUserCache = cacheManager.getOrCreateCache(idQc);
    }

    @DistributeLock(keyExpression = "#telephone", scene = "USER_REGISTER")
    public UserOperatorResponse register(String telephone) {
        //前缀 + 6位随机数 + 手机号后四位
        String defaultNickName;
        do {
            defaultNickName = DEFAULT_NICK_NAME_PREFIX + RandomUtil.randomString(6) + telephone.substring(7, 11);
        } while (nickNameExist(defaultNickName));

        User user = register(telephone, defaultNickName, telephone);
        Assert.notNull(user, UserErrorCode.USER_OPERATE_FAILED.getCode());
        telUserCache.put(telephone, user);
        idUserCache.put(user.getId().toString(), user);

        //加入流水
        long streamResult = userOperateStreamService.insertStream(user, UserOperateTypeEnum.REGISTER);
        Assert.notNull(streamResult, () -> new BizException(RepoErrorCode.UPDATE_FAILED));

        UserOperatorResponse userOperatorResponse = new UserOperatorResponse();
        userOperatorResponse.setSuccess(true);

        return userOperatorResponse;
    }

    /**
     * 注册
     *
     * @param telephone
     * @param nickName
     * @param password
     * @return
     */
    private User register(String telephone, String nickName, String password) {
        if (userMapper.findByTelephone(telephone) != null) {
            throw new UserException(DUPLICATE_TELEPHONE_NUMBER);
        }

        User user = new User();
        user.register(telephone, nickName, password);
        if (save(user)) {
            addNickName(nickName);
            return userMapper.findByTelephone(telephone);
        }
        return null;
    }

    /**
     * 实名认证
     *
     * @param userAuthRequest
     * @return
     */
    public UserOperatorResponse auth(UserAuthRequest userAuthRequest) {
        UserOperatorResponse userOperatorResponse = new UserOperatorResponse();
        User user = userMapper.findById(userAuthRequest.getUserId());
        Assert.notNull(user, () -> new UserException(USER_NOT_EXIST));

        if (user.getState() == UserStateEnum.AUTH || user.getState() == UserStateEnum.ACTIVE) {
            userOperatorResponse.setSuccess(true);
            return userOperatorResponse;
        }

        Assert.isTrue(user.getState() == UserStateEnum.INIT, () -> new UserException(USER_STATUS_IS_NOT_INIT));
        Assert.isTrue(authService.checkAuth(userAuthRequest.getRealName(), userAuthRequest.getIdCard()), () -> new UserException(USER_AUTH_FAIL));
        user.auth(userAuthRequest.getRealName(), userAuthRequest.getIdCard());
        boolean result = updateById(user);
        if (result) {
            telUserCache.remove(user.getTelephone());
            idUserCache.remove(user.getId().toString());
            //加入流水
            long streamResult = userOperateStreamService.insertStream(user, UserOperateTypeEnum.AUTH);
            Assert.notNull(streamResult, () -> new BizException(RepoErrorCode.UPDATE_FAILED));
            userOperatorResponse.setSuccess(true);
            userOperatorResponse.setUser(UserConvertor.INSTANCE.mapToVo(user));
        } else {
            userOperatorResponse.setSuccess(false);
            userOperatorResponse.setResponseCode(UserErrorCode.USER_OPERATE_FAILED.getCode());
            userOperatorResponse.setResponseCode(UserErrorCode.USER_OPERATE_FAILED.getMessage());
        }
        return userOperatorResponse;
    }

    /**
     * 用户激活
     *
     * @param userActiveRequest
     * @return
     */
    public UserOperatorResponse active(UserActiveRequest userActiveRequest) {
        UserOperatorResponse userOperatorResponse = new UserOperatorResponse();
        User user = userMapper.findById(userActiveRequest.getUserId());
        Assert.notNull(user, () -> new UserException(USER_NOT_EXIST));
        Assert.isTrue(user.getState() == UserStateEnum.AUTH, () -> new UserException(USER_STATUS_IS_NOT_AUTH));
        user.active(userActiveRequest.getBlockChainUrl(), userActiveRequest.getBlockChainPlatform());
        boolean result = updateById(user);
        if (result) {
            //加入流水
            long streamResult = userOperateStreamService.insertStream(user, UserOperateTypeEnum.ACTIVE);
            Assert.notNull(streamResult, () -> new BizException(RepoErrorCode.UPDATE_FAILED));
            telUserCache.remove(user.getTelephone());
            idUserCache.remove(user.getId().toString());
            userOperatorResponse.setSuccess(true);
        } else {
            userOperatorResponse.setSuccess(false);
            userOperatorResponse.setResponseCode(UserErrorCode.USER_OPERATE_FAILED.getCode());
            userOperatorResponse.setResponseCode(UserErrorCode.USER_OPERATE_FAILED.getMessage());
        }
        return userOperatorResponse;
    }

    /**
     * 冻结
     *
     * @param userId
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public UserOperatorResponse freeze(Long userId) {
        UserOperatorResponse userOperatorResponse = new UserOperatorResponse();
        User user = userMapper.findById(userId);
        Assert.notNull(user, () -> new UserException(USER_NOT_EXIST));
        Assert.isTrue(user.getState() == UserStateEnum.ACTIVE, () -> new UserException(USER_STATUS_IS_NOT_ACTIVE));

        //第一次删除缓存
        telUserCache.remove(user.getTelephone());
        idUserCache.remove(user.getId().toString());

        if (user.getState() == UserStateEnum.FROZEN) {
            userOperatorResponse.setSuccess(true);
            return userOperatorResponse;
        }
        user.setState(UserStateEnum.FROZEN);
        boolean updateResult = updateById(user);
        Assert.isTrue(updateResult, () -> new BizException(RepoErrorCode.UPDATE_FAILED));
        //加入流水
        long result = userOperateStreamService.insertStream(user, UserOperateTypeEnum.FREEZE);
        Assert.notNull(result, () -> new BizException(RepoErrorCode.UPDATE_FAILED));

        //第二次删除缓存
        userCacheDelayDeleteService.delayedCacheDelete(telUserCache, idUserCache, user);

        userOperatorResponse.setSuccess(true);
        return userOperatorResponse;
    }

    /**
     * 解冻
     *
     * @param userId
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public UserOperatorResponse unfreeze(Long userId) {
        UserOperatorResponse userOperatorResponse = new UserOperatorResponse();
        User user = userMapper.findById(userId);
        Assert.notNull(user, () -> new UserException(USER_NOT_EXIST));

        //第一次删除缓存
        telUserCache.remove(user.getTelephone());
        idUserCache.remove(user.getId().toString());

        if (user.getState() == UserStateEnum.ACTIVE) {
            userOperatorResponse.setSuccess(true);
            return userOperatorResponse;
        }
        user.setState(UserStateEnum.ACTIVE);
        //更新数据库
        boolean updateResult = updateById(user);
        Assert.isTrue(updateResult, () -> new BizException(RepoErrorCode.UPDATE_FAILED));
        //加入流水
        long result = userOperateStreamService.insertStream(user, UserOperateTypeEnum.UNFREEZE);
        Assert.notNull(result, () -> new BizException(RepoErrorCode.UPDATE_FAILED));

        //第二次删除缓存
        userCacheDelayDeleteService.delayedCacheDelete(telUserCache, idUserCache, user);

        userOperatorResponse.setSuccess(true);
        return userOperatorResponse;
    }

    /**
     * 通过手机号查询用户信息
     *
     * @param telephone
     * @return
     */
    @Cached(name = ":user:cache:tel:", expire = 3000, cacheType = CacheType.BOTH, key = "#telephone", cacheNullValue = true)
    @CacheRefresh(refresh = 60, timeUnit = TimeUnit.MINUTES)
    public User findByTelephone(String telephone) {
        return userMapper.findByTelephone(telephone);
    }

    /**
     * 通过用户ID查询用户信息
     *
     * @param userId
     * @return
     */
    @Cached(name = ":user:cache:id:", expire = 3000, cacheType = CacheType.BOTH, key = "#userId", cacheNullValue = true)
    @CacheRefresh(refresh = 60, timeUnit = TimeUnit.MINUTES)
    public User findById(Long userId) {
        return userMapper.findById(userId);
    }

    /**
     * 查询用户信息
     *
     * @param userQueryRequest
     * @return
     */
    public User queryUserInfo(UserQueryRequest userQueryRequest) {

        if (null != userQueryRequest.getUserId()) {
            return findById(userQueryRequest.getUserId());
        }

        if (StringUtils.isNotBlank(userQueryRequest.getTelephone())) {
            return findByTelephone(userQueryRequest.getTelephone());
        }

        return null;
    }

    /**
     * 更新用户信息
     *
     * @param userModifyRequest
     * @return
     */
    public UserOperatorResponse modify(UserModifyRequest userModifyRequest) {
        UserOperatorResponse userOperatorResponse = new UserOperatorResponse();
        User user = userMapper.findById(userModifyRequest.getUserId());
        Assert.notNull(user, () -> new UserException(USER_NOT_EXIST));
        Assert.isTrue(user.canModifyInfo(), () -> new UserException(USER_STATUS_CANT_OPERATE));

        if (StringUtils.isNotBlank(userModifyRequest.getNickName()) && nickNameExist(userModifyRequest.getNickName())) {
            throw new UserException(NICK_NAME_EXIST);
        }
        BeanUtils.copyProperties(userModifyRequest, user);

        if (StringUtils.isNotBlank(userModifyRequest.getPassword())) {
            user.setPasswordHash(DigestUtil.md5Hex(userModifyRequest.getPassword()));
        }
        if (updateById(user)) {
            //加入流水
            long streamResult = userOperateStreamService.insertStream(user, UserOperateTypeEnum.MODIFY);
            Assert.notNull(streamResult, () -> new BizException(RepoErrorCode.UPDATE_FAILED));
            addNickName(userModifyRequest.getNickName());
            userOperatorResponse.setSuccess(true);

            //删除缓存
            telUserCache.remove(user.getTelephone());
            idUserCache.remove(user.getId().toString());

            return userOperatorResponse;
        }
        userOperatorResponse.setSuccess(false);
        userOperatorResponse.setResponseCode(UserErrorCode.USER_OPERATE_FAILED.getCode());
        userOperatorResponse.setResponseCode(UserErrorCode.USER_OPERATE_FAILED.getMessage());

        return userOperatorResponse;
    }

    public boolean nickNameExist(String nickName) {
        //如果布隆过滤器中存在，再进行数据库二次判断
        if (this.bloomFilter != null && this.bloomFilter.contains(nickName)) {
            return userMapper.findByNickname(nickName) != null;
        }

        return false;
    }

    private boolean addNickName(String nickName) {
        return this.bloomFilter != null && this.bloomFilter.add(nickName);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.bloomFilter = redissonClient.getBloomFilter("nickName");
        if (bloomFilter != null && !bloomFilter.isExists()) {
            this.bloomFilter.tryInit(100000L, 0.01);
        }

    }
}
