package cn.hollis.nft.turbo.api.collection.model;

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * <p>
 * 藏品信息
 * </p>
 *
 * @author wswyb001
 * @since 2024-01-19
 */
@Getter
@Setter
@ToString
public class HeldCollectionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * '藏品id'
     */
    private Long collectionId;

    /**
     * '藏品编号'
     */
    private String serialNo;

    /**
     * 'nft唯一编号'
     */
    private String nftId;

    /**
     * '上一个持有人id'
     */
    private String preId;

    /**
     * '持有人id'
     */
    private String userId;

    /**
     * '状态'
     */
    private String state;

    /**
     * '交易hash'
     */
    private String txHash;

    /**
     * '藏品持有时间'
     */
    private Date holdTime;

    /**
     * '藏品同步时间'
     */
    private Date syncChainTime;

    /**
     * '藏品销毁时间'
     */
    private Date deleteTime;

    /**
     * 业务单号
     */
    private String bizNo;

    /**
     * 业务类型
     */
    private String bizType;



}
