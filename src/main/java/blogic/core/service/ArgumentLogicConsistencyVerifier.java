package blogic.core.service;

import blogic.core.exception.IllegalArgumentException;

public interface ArgumentLogicConsistencyVerifier {

    public void verifyArguments() throws IllegalArgumentException;

}
