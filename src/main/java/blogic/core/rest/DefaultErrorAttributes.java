package blogic.core.rest;

import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class DefaultErrorAttributes extends org.springframework.boot.web.reactive.error.DefaultErrorAttributes {

    @Value("${error.message.handle:false}")
    private Boolean errorMessageHandle = false;

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
        result.put("status", 500);
        result.put("codeDesc", doGetMessage(e));
        return result;
    }

    private String doGetMessage(Throwable e) {
        if(!errorMessageHandle) return e.getMessage();

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
        }else if(e instanceof CodedException ce) {
            //TODO 根据code找模板
            return e.getMessage();
        }
        return "service exception";
    }

}
