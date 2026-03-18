package cn.gideon.nft.turbo.inventory;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author gideon
 */
@SpringBootApplication(scanBasePackages = "cn.gideon.nft.turbo.inventory")
@EnableDubbo
public class NfTurboInventoryApplication {

    public static void main(String[] args) {
        SpringApplication.run(NfTurboInventoryApplication.class, args);
    }

}
