package cn.hollis.nft.turbo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author hollis
 *
 * nft-turbo-app 有什么用: https://thoughts.aliyun.com/workspaces/6655879cf459b7001ba42f1b/docs/66853e935e11940001dbc631
 * 文档找不到？没权限怎么办？查看地址：http://nfturbo.wiki/
 */
@SpringBootApplication(scanBasePackages = "cn.hollis.nft.turbo")
public class NfTurboBusinessApplication {

    public static void main(String[] args) {
        SpringApplication.run(NfTurboBusinessApplication.class, args);
    }

}
