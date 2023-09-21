package blogic.core.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class DefaultErrorAttributes extends org.springframework.boot.web.reactive.error.DefaultErrorAttributes {

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
        result.put("status", superResult.get("status"));
        result.put("codeDesc", e.getMessage());
        return result;
    }

}
