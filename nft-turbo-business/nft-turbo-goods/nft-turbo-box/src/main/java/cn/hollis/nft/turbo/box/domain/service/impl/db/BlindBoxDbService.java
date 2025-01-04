package cn.hollis.nft.turbo.box.domain.service.impl.db;

import cn.hollis.nft.turbo.base.response.PageResponse;
import cn.hollis.nft.turbo.box.domain.entity.BlindBox;
import cn.hollis.nft.turbo.box.domain.service.impl.BaseBlindBoxService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
/**
 * 盲盒数据库服务实现
 * @author Hollis
 */
@Service
public class BlindBoxDbService extends BaseBlindBoxService {

    @Override
    public PageResponse<BlindBox> pageQueryByState(String keyWord, String state, int currentPage, int pageSize){
        Page<BlindBox> page = new Page<>(currentPage, pageSize);
        QueryWrapper<BlindBox> wrapper = new QueryWrapper<>();
        wrapper.eq("state", state);

        if (StringUtils.isNotBlank(keyWord)) {
            wrapper.like("name", keyWord);
        }
        wrapper.orderBy(true, true, "gmt_create");

        Page<BlindBox> blindBoxPage = this.page(page, wrapper);

        return PageResponse.of(blindBoxPage.getRecords(), (int) blindBoxPage.getTotal(), pageSize, currentPage);
    }

}
