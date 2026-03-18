package cn.gideon.nft.turbo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author gideon
 */
@SpringBootApplication(scanBasePackages = "cn.gideon.nft.turbo")
public class NfTurboBusinessApplication {

    public static void main(String[] args) {
        SpringApplication.run(NfTurboBusinessApplication.class, args);
    }

}
