package blogic.core.validation;

import blogic.core.exception.IllegalArgumentException;

public interface DTOLogicConsistencyVerifier {

    public void verifyLogicConsistency() throws IllegalArgumentException;

}
