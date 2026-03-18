package cn.gideon.nft.turbo.tcc.config;

import cn.gideon.nft.turbo.tcc.service.TransactionLogService;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Gideon
 */
@Configuration
@MapperScan("cn.gideon.nft.turbo.tcc.mapper")
public class TccConfiguration {

    @Bean
    public TransactionLogService transactionLogService() {
        return new TransactionLogService();
    }
}
