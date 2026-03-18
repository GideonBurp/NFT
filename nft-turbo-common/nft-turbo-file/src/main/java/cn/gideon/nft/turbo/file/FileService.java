package cn.gideon.nft.turbo.file;

import java.io.InputStream;

/**
 * 文件 服务
 *
 * @author gideon
 */
public interface FileService {

    /**
     * 文件上传
     * @param path
     * @param fileStream
     * @return
     */
    public boolean upload(String path, InputStream fileStream);

}
