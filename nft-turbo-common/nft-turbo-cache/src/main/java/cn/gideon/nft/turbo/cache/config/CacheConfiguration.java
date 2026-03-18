package cn.gideon.nft.turbo.cache.config;

import com.alicp.jetcache.anno.config.EnableMethodCache;
import org.springframework.context.annotation.Configuration;

/**
 * 缓存配置
 *
 * @author Gideon
 */
@Configuration
@EnableMethodCache(basePackages = "cn.gideon.nft.turbo")
public class CacheConfiguration {
}
