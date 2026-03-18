package cn.gideon.nft.turbo.gateway;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Gideon
 */
@SpringBootApplication(scanBasePackages = "cn.gideon.nft.turbo.gateway")
public class NfTurboGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(NfTurboGatewayApplication.class, args);
    }

}
