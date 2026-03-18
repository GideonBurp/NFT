package cn.gideon.nft.turbo.goods.entity.convertor;

import cn.gideon.nft.turbo.api.goods.model.GoodsStreamVO;
import cn.gideon.nft.turbo.box.domain.entity.BlindBoxInventoryStream;
import cn.gideon.nft.turbo.collection.domain.entity.CollectionInventoryStream;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.factory.Mappers;

/**
 * @author Gideon
 */
@Mapper(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface GoodsStreamConvertor {

    GoodsStreamConvertor INSTANCE = Mappers.getMapper(GoodsStreamConvertor.class);

    /**
     * 转换实体
     *
     * @param request
     * @return
     */
    @Mapping(target = "goodsId", source = "request.collectionId")
    @Mapping(target = "goodsType", constant = "COLLECTION")
    public GoodsStreamVO mapToVo(CollectionInventoryStream request);

    /**
     * 转换为vo
     *
     * @param request
     * @return
     */
    @Mapping(target = "goodsId", source = "request.blindBoxId")
    @Mapping(target = "goodsType", constant = "BLIND_BOX")
    public GoodsStreamVO mapToVo(BlindBoxInventoryStream request);
}