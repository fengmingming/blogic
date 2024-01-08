package blogic.productline.product.rest;

import blogic.productline.product.domain.Product;
import blogic.user.common.UserDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class ProductDto extends Product {

    private List<UserDto> users;

}
