package cn.gideon.nft.turbo.goods;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author gideon
 */
@SpringBootApplication(scanBasePackages = {"cn.gideon.nft.turbo.goods", "cn.gideon.nft.turbo.collection", "cn.gideon.nft.turbo.box"})
@EnableDubbo(scanBasePackages = {"cn.gideon.nft.turbo.goods", "cn.gideon.nft.turbo.collection", "cn.gideon.nft.turbo.box"})
public class NfTurboGoodsApplication {

    public static void main(String[] args) {
        SpringApplication.run(NfTurboGoodsApplication.class, args);
    }

}
