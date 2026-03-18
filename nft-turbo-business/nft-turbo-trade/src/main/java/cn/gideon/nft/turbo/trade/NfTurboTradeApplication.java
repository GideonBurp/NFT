package cn.gideon.nft.turbo.trade;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author gideon
 */
@SpringBootApplication(scanBasePackages = "cn.gideon.nft.turbo.trade")
@EnableDubbo
public class NfTurboTradeApplication {

    public static void main(String[] args) {
        SpringApplication.run(NfTurboTradeApplication.class, args);
    }

}
