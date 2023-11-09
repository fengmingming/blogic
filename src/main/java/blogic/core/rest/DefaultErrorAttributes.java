package blogic.core.rest;

import blogic.core.context.ContextWebFilter;
import blogic.core.exception.CodedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.context.MessageSource;
import org.springframework.core.NestedRuntimeException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class DefaultErrorAttributes extends org.springframework.boot.web.reactive.error.DefaultErrorAttributes {

    private final MessageSource codedExceptionMessageSource;

    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        Map<String, Object> superResult = super.getErrorAttributes(request, options);
        Throwable e = super.getError(request);
        if(e instanceof NestedRuntimeException nestedE) {
            e = nestedE.getMostSpecificCause();
        }
        Map<String, Object> result = new HashMap<>();
        if(e instanceof CodedException ce) {
            log.warn("", e);
            result.put("code", ce.getCode());
        }else {
            log.error("", e);
            result.put("code", superResult.get("status"));
        }
        Locale locale = (Locale) request.attribute(ContextWebFilter.ATTRIBUTE_KEY_LOCALE).orElseGet(() -> Locale.getDefault());
        result.put("status", 200);
        result.put("codeDesc", doGetMessage(e, locale));
        return result;
    }

    private String doGetMessage(Throwable e, Locale locale) {
        if(e instanceof CodedException ce) {
            return codedExceptionMessageSource.getMessage(String.valueOf(ce.getCode()),
                    ce.getTemplateArgs(), String.format("service exception [%d]", ce.getCode()), locale);
        }
        if(e instanceof BindingResult br) {
            return br.getAllErrors().stream().map(it -> {
                if(it instanceof FieldError fe) {
                    return String.format("%s %s", fe.getField(), fe.getDefaultMessage());
                }else {
                    return it.getDefaultMessage();
                }
            }).collect(Collectors.joining(";"));
        }
        return "inner service error";
    }

}
