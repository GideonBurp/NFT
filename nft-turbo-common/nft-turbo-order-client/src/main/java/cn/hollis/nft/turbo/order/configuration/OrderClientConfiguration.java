package cn.hollis.nft.turbo.order.configuration;

import cn.hollis.nft.turbo.order.sharding.id.WorkerIdHolder;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Hollis
 */
@Configuration
public class OrderClientConfiguration {

    @Bean
    public WorkerIdHolder workerIdHolder(RedissonClient redisson) {
        return new WorkerIdHolder(redisson);
    }
}
