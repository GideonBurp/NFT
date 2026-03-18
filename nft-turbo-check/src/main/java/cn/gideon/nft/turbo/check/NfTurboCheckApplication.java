package cn.gideon.nft.turbo.check;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author gideon
 */
@SpringBootApplication(scanBasePackages = {"cn.gideon.nft.turbo.check"})
@EnableDubbo
public class NfTurboCheckApplication {

    public static void main(String[] args) {
        SpringApplication.run(NfTurboCheckApplication.class, args);
    }

}
