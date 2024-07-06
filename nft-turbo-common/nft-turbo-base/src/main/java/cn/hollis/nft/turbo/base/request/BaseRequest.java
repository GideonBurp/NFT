package cn.hollis.nft.turbo.base.request;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 通用入参
 * 通用入参出参设计: https://thoughts.aliyun.com/workspaces/6655879cf459b7001ba42f1b/docs/6673e7e30f232c0001a8b4a1
 * 文档找不到？没权限怎么办？查看地址：http://nfturbo.wiki/
 * @author Hollis
 */
@Setter
@Getter
public class BaseRequest implements Serializable {
    private static final long serialVersionUID = 1L;

}
