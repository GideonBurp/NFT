package cn.hollis.nft.turbo.box;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author hollis
 */
@SpringBootApplication(scanBasePackages = "cn.hollis.nft.turbo.box")
@EnableDubbo
public class NfTurboBoxApplication {

    public static void main(String[] args) {
        SpringApplication.run(NfTurboBoxApplication.class, args);
    }

}
