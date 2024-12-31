package cn.hollis.nft.turbo.box.infrastructure.mapper;

import cn.hollis.nft.turbo.box.domain.entity.BlindBox;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 盲盒信息 Mapper 接口
 * </p>
 *
 * @author wswyb001
 * @since 2024-10-05
 */
@Mapper
public interface BlindBoxMapper extends BaseMapper<BlindBox> {

    /**
     * 根据藏品标识查询藏品信息
     *
     * @param identifier
     * @return
     */
    BlindBox selectByIdentifier(String identifier);

    /**
     * 库存扣减
     *
     * @param blindBox
     * @return
     */
    int sale(BlindBox blindBox);

    /**
     * 库存确认扣减
     *
     * @param id
     * @param occupiedInventory 变更前的库存
     * @param quantity
     * @return
     */
    int confirmSale(Long id, Long occupiedInventory, Integer quantity);

    /**
     * 库存预扣减
     *
     * @param id
     * @param quantity
     * @return
     */
    int trySale(Long id, Integer quantity);

    /**
     * 库存预扣减-无hint版
     *
     * @param id
     * @param quantity
     * @return
     */
    int trySaleWithoutHint(Long id, Integer quantity);

    /**
     * 库存退回
     *
     * @param id
     * @param quantity
     * @return
     */
    int cancelSale(Long id, Integer quantity);
}
