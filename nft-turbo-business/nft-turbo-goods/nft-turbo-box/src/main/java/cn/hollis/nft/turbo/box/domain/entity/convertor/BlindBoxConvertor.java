package cn.hollis.nft.turbo.box.domain.entity.convertor;

import cn.hollis.nft.turbo.api.box.constant.BlindBoxStateEnum;
import cn.hollis.nft.turbo.api.box.constant.BlindBoxVoState;
import cn.hollis.nft.turbo.api.box.model.BlindBoxVO;
import cn.hollis.nft.turbo.api.box.request.BlindBoxCreateRequest;
import cn.hollis.nft.turbo.box.domain.entity.BlindBox;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * @author Hollis
 */
@Mapper(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface BlindBoxConvertor {

    BlindBoxConvertor INSTANCE = Mappers.getMapper(BlindBoxConvertor.class);

    public static final int DEFAULT_MIN_SALE_TIME = 60;

    /**
     * 转换为VO
     *
     * @param request
     * @return
     */
    @Mapping(target = "inventory", source = "request.saleableInventory")
    @Mapping(target = "state", ignore = true)
    public BlindBoxVO mapToVo(BlindBox request);



    /**
     * 转换为实体
     *
     * @param request
     * @return
     */
    public BlindBox mapToEntity(BlindBoxCreateRequest request);

    /**
     * 转换为VO
     *
     * @param request
     * @return
     */
    public List<BlindBoxVO> mapToVo(List<BlindBox> request);

    /**
     * 状态映射
     *
     * @param blindBox
     * @return
     */
    default BlindBoxVoState transState(BlindBox blindBox) {

        if (blindBox.getState().equals(BlindBoxStateEnum.INIT) || blindBox.getState().equals(BlindBoxStateEnum.REMOVED)) {
            return BlindBoxVoState.NOT_FOR_SALE;
        }

        Instant now = Instant.now();

        if (now.compareTo(blindBox.getSaleTime().toInstant()) >= 0) {
            if (blindBox.getSaleableInventory() > 0) {
                return BlindBoxVoState.SELLING;
            } else {
                return BlindBoxVoState.SOLD_OUT;
            }
        } else {
            if (ChronoUnit.MINUTES.between(now, blindBox.getSaleTime().toInstant()) > DEFAULT_MIN_SALE_TIME) {
                return BlindBoxVoState.WAIT_FOR_SALE;
            }
            return BlindBoxVoState.COMING_SOON;
        }
    }
}
