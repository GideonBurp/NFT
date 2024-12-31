package cn.hollis.nft.turbo.box;

import cn.hollis.nft.turbo.api.chain.service.ChainFacadeService;
import cn.hollis.nft.turbo.api.collection.service.CollectionReadFacadeService;
import cn.hollis.nft.turbo.api.order.OrderFacadeService;
import cn.hollis.nft.turbo.api.user.service.UserFacadeService;
import cn.hollis.nft.turbo.collection.domain.service.impl.HeldCollectionService;
import cn.hollis.nft.turbo.limiter.SlidingWindowRateLimiter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {NfTurboBoxApplication.class})
@ActiveProfiles("test")
public class BoxBaseTest {

    @MockBean
    protected RedissonClient redissonClient;

    @MockBean
    protected SlidingWindowRateLimiter slidingWindowRateLimiter;

    @MockBean
    protected ChainFacadeService chainFacadeService;

    @MockBean
    protected UserFacadeService userFacadeService;

    @MockBean
    protected CollectionReadFacadeService collectionReadFacadeService;

    @MockBean
    protected OrderFacadeService orderFacadeService;

    @MockBean
    protected HeldCollectionService heldCollectionService;

    @Test
    public void test(){

    }
}
