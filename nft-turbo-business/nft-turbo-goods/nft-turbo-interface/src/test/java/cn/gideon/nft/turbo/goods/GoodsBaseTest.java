package cn.gideon.nft.turbo.goods;


import cn.gideon.nft.turbo.api.chain.service.ChainFacadeService;
import cn.gideon.nft.turbo.api.order.OrderFacadeService;
import cn.gideon.nft.turbo.api.user.service.UserFacadeService;
import cn.gideon.nft.turbo.collection.facade.CollectionReadFacadeServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {NfTurboGoodsApplication.class})
@ActiveProfiles("test")
public class GoodsBaseTest {

    @MockBean
    protected ChainFacadeService chainFacadeService;

    @MockBean
    protected UserFacadeService userFacadeService;

    @MockBean
    protected CollectionReadFacadeServiceImpl collectionReadFacadeService;

    @MockBean
    protected OrderFacadeService orderFacadeService;

    @Test
    public void test() {

    }
}
