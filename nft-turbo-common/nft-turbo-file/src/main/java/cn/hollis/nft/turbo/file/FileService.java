package cn.hollis.nft.turbo.file;

import java.io.InputStream;

/**
 * 文件 服务
 *
 * @author hollis
 */
public interface FileService {

    public boolean upload(String path, InputStream fileStream);

}
