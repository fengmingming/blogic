package blogic.productline.infras;

public class ProductId {

    private Long productId;

    private ProductId(Long productId) {
        this.productId = productId;
    }

    public Long get() {
        return this.productId;
    }

    public ProductId build(Long productId) {
        return new ProductId(productId);
    }

}
