package cn.hollis.nft.turbo.collection.domain.service.impl;

import cn.hollis.nft.turbo.base.response.PageResponse;
import cn.hollis.nft.turbo.collection.domain.entity.Collection;
import cn.hollis.nft.turbo.collection.infrastructure.es.mapper.CollectionEsMapper;
import com.google.common.collect.ImmutableList;
import org.dromara.easyes.core.biz.SAPageInfo;
import org.dromara.easyes.core.conditions.select.LambdaEsQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "spring.elasticsearch.enable", havingValue = "true")
public class CollectionEsService extends BaseCollectionService {

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Autowired
    private CollectionEsMapper collectionEsMapper;

    @Override
    public PageResponse<Collection> pageQueryByState(String name, String state, int currentPage, int pageSize) {
        Criteria criteria = null;
        if (name != null) {
            criteria = new Criteria("name").is(name).and(new Criteria("state").is(state), new Criteria("deleted").is("0"));
        } else {
            criteria = new Criteria("state").is(state).and(new Criteria("deleted").is("0"));
        }
        PageRequest pageRequest = PageRequest.of(currentPage - 1, pageSize);
        Query query = new CriteriaQuery(criteria).setPageable(pageRequest).addSort(Sort.by(Sort.Order.asc("collection_id")));
        SearchHits<Collection> searchHits = elasticsearchOperations.search(query, Collection.class);

        return PageResponse.of(searchHits.getSearchHits().stream().map(SearchHit::getContent).toList(), (int) searchHits.getTotalHits(), pageSize);
    }

    public SAPageInfo<Collection> deepPageQueryByState(String name, String state, int pageSize, Long lastId) {
        LambdaEsQueryWrapper<Collection> queryWrapper = new LambdaEsQueryWrapper<>();
        queryWrapper.match(Collection::getName, name)
                .and(wrapper -> wrapper
                        .match(collection -> collection.getState().name(), state)
                        .match(Collection::getDeleted, "0"))
                .orderByAsc("collection_id");

        SAPageInfo<Collection> saPageInfo;
        if (lastId == null) {
            saPageInfo = collectionEsMapper.searchAfterPage(queryWrapper, null, pageSize);
        } else {
            saPageInfo = collectionEsMapper.searchAfterPage(queryWrapper, ImmutableList.of(lastId), 10);
        }
        return saPageInfo;
    }
}
