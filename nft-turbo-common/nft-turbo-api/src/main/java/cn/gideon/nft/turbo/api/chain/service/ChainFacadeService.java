package cn.gideon.nft.turbo.api.chain.service;

import cn.gideon.nft.turbo.api.chain.request.ChainProcessRequest;
import cn.gideon.nft.turbo.api.chain.response.ChainProcessResponse;
import cn.gideon.nft.turbo.api.chain.response.data.ChainCreateData;
import cn.gideon.nft.turbo.api.chain.response.data.ChainOperationData;

/**
 * @author Gideon
 */
public interface ChainFacadeService {

    /**
     * 创建链账户
     *
     * @param request
     * @return
     */
    ChainProcessResponse<ChainCreateData> createAddr(ChainProcessRequest request);

    /**
     * 上链藏品
     *
     * @param request
     * @return
     */
    ChainProcessResponse<ChainOperationData> chain(ChainProcessRequest request);

    /**
     * 铸造藏品
     *
     * @param request
     * @return
     */
    ChainProcessResponse<ChainOperationData> mint(ChainProcessRequest request);

    /**
     * 交易藏品
     *
     * @param request
     * @return
     */
    ChainProcessResponse<ChainOperationData> transfer(ChainProcessRequest request);

    /**
     * 销毁藏品
     *
     * @param request
     * @return
     */
    ChainProcessResponse<ChainOperationData> destroy(ChainProcessRequest request);
}
