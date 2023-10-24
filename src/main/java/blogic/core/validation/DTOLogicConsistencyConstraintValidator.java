package blogic.core.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DTOLogicConsistencyConstraintValidator implements ConstraintValidator<DTOLogicValid, DTOLogicConsistencyVerifier> {

    @Override
    public boolean isValid(DTOLogicConsistencyVerifier value, ConstraintValidatorContext context) {
        if(value == null) return true;
        try{
            value.verifyLogicConsistency();
            return true;
        }catch (Exception e) {
            log.error(value.getClass().getName() + ".verifyLogicConsistency exception", e);
            context.buildConstraintViolationWithTemplate(e.getMessage()).addConstraintViolation();
            return false;
        }
    }

}
