package cn.gideon.nft.turbo.order;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author gideon
 */
@SpringBootApplication(scanBasePackages = "cn.gideon.nft.turbo.order")
@EnableDubbo
public class NfTurboOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(NfTurboOrderApplication.class, args);
    }

}
