package blogic.productline.product;

import blogic.BLogicBootstrap;
import blogic.core.rest.Paging;
import blogic.core.security.TokenInfo;
import blogic.core.security.UserCurrentContext;
import blogic.productline.product.rest.ProductRest;
import blogic.user.domain.RoleEnum;
import cn.hutool.json.JSONUtil;
import org.apache.el.parser.Token;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = BLogicBootstrap.class)
public class TestProduct {

    @Autowired
    private ProductRest productRest;

    @Test
    public void testFindProducts() {
        TokenInfo tokenInfo = TokenInfo.builder().userId(31L).build();
        UserCurrentContext context = new UserCurrentContext();
        context.setCompanyId(1L);
        context.getAuthorities().add(RoleEnum.ROLE_DEVELOPER);
        System.out.println(JSONUtil.toJsonStr(productRest.findProducts(1L, tokenInfo,
                context, Paging.builder().pageNum(1).pageSize(10).build()).block()));
    }

}
