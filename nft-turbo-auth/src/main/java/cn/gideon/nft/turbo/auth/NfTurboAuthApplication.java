package cn.gideon.nft.turbo.auth;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author gideon
 */
@SpringBootApplication(scanBasePackages = {"cn.gideon.nft.turbo.auth"})
@EnableDubbo
public class NfTurboAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(NfTurboAuthApplication.class, args);
    }

}
