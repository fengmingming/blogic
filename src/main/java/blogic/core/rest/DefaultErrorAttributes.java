package blogic.core.rest;

import blogic.core.exception.CodedException;
import jakarta.validation.ValidationException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class DefaultErrorAttributes extends org.springframework.boot.web.reactive.error.DefaultErrorAttributes {

    private final ErrorHandleProperties errorHandleProperties;
    private final MessageSource codedExceptionMessageSource;

    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        Map<String, Object> superResult = super.getErrorAttributes(request, options);
        Throwable e = super.getError(request);
        Map<String, Object> result = new HashMap<>();
        if(e instanceof CodedException ce) {
            result.put("code", ce.getCode());
        }else {
            result.put("code", superResult.get("status"));
        }
        List<Locale> locales = request.headers().acceptLanguage().stream()
                .map(range -> Locale.forLanguageTag(range.getRange()))
                .filter(locale -> StringUtils.hasText(locale.getDisplayName())).toList();
        result.put("status", 500);
        result.put("codeDesc", doGetMessage(e, locales.size() > 0? locales.get(0): Locale.getDefault()));
        return result;
    }

    private String doGetMessage(Throwable e, Locale locale) {
        log.error("", e);
        if(e instanceof CodedException ce) {
            return codedExceptionMessageSource.getMessage(String.valueOf(ce.getCode()),
                    ce.getTemplateArgs(), String.format("service exception [%d]", ce.getCode()), locale);
        }
        if(!errorHandleProperties.isErrorMessageHandle()) return e.getMessage();
        if(e instanceof DataAccessException dae) {
            return "execute sql exception";
        }else if(e instanceof BindingResult br) {
            return br.getAllErrors().stream().map(it -> {
                if(it instanceof FieldError fe) {
                    return String.format("%s %s", fe.getField(), fe.getDefaultMessage());
                }else {
                    return String.format("%s %s", it.getObjectName(), it.getDefaultMessage());
                }
            }).collect(Collectors.joining(";"));
        }else if(e instanceof ValidationException ve) {
            return "internal parameter exception";
        }
        return "service exception";
    }

    @ConfigurationProperties(prefix = "blogic")
    @Setter
    @Getter
    public static class ErrorHandleProperties {
        private boolean errorMessageHandle = false;
    }

}
