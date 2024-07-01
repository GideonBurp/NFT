package cn.hollis.nft.turbo.user.domain.service;

import cn.hollis.nft.turbo.user.domain.entity.User;
import com.alicp.jetcache.Cache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 用户缓存延迟删除服务
 *
 * @author hollis
 */
@Service
@Slf4j
public class UserCacheDelayDeleteService {

    @Scheduled(fixedDelay = 2, timeUnit = TimeUnit.SECONDS)
    public void delayedCacheDelete(Cache idUserCache, User user) {
        boolean idDeleteResult = idUserCache.remove(user.getId().toString());
        log.info("idUserCache removed, key = {} , result  = {}", user.getId(), idDeleteResult);
    }
}
