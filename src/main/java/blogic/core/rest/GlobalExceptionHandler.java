package blogic.core.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ResVo> throwableHandler(Throwable e) {
        log.error("GlobalExceptionHandler catch throwable", e);
        if(e instanceof CodedException ce) {
            return ResponseEntity.status(500).body(ResVo.error(ce.getCode(), e.getMessage()));
        }
        return ResponseEntity.status(500).body(ResVo.error(e.getMessage()));
    }

}
