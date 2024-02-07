package blogic.productline.infras;

import blogic.core.exception.IllegalArgumentException;
import blogic.core.rest.Paging;
import blogic.core.validation.DTOLogicConsistencyVerifier;
import blogic.core.validation.DTOLogicValid;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@DTOLogicValid
public class MyDataReq extends Paging implements DTOLogicConsistencyVerifier {

    private Long createUserId;
    private Long currentUserId;

    @Override
    public void verifyLogicConsistency() throws IllegalArgumentException {
        if(createUserId == null && currentUserId == null) {
            throw new IllegalArgumentException("createUserId and currentUserId can not be both null at the same time");
        }
    }

}
