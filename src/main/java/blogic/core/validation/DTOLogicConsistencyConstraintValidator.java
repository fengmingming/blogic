package blogic.core.validation;

import blogic.core.context.SpringContext;
import blogic.core.exception.CodedException;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

@Slf4j
public class DTOLogicConsistencyConstraintValidator implements ConstraintValidator<DTOLogicValid, DTOLogicConsistencyVerifier> {

    @Override
    public boolean isValid(DTOLogicConsistencyVerifier value, ConstraintValidatorContext context) {
        if(value == null) return true;
        try{
            value.verifyLogicConsistency();
            return true;
        }catch (Exception e) {
            String message = e.getMessage();
            if(e instanceof CodedException codedE) {
                Locale locale = LocaleContextHolder.getLocale();
                message = SpringContext.getMessage(codedE.getCode(), locale, codedE.getTemplateArgs());
            }
            log.error(value.getClass().getName() + ".verifyLogicConsistency exception {}", message, e);
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
            return false;
        }
    }

}
