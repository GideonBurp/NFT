package cn.gideon.nft.turbo.collection.infrastructure.mapper;

import cn.gideon.nft.turbo.collection.domain.entity.HeldCollectionStream;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 持有藏品流水表Mapper
 *
 * @author Gideon
 */
@Mapper
public interface HeldCollectionStreamMapper extends BaseMapper<HeldCollectionStream> {
}