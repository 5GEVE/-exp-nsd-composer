package it.cnit.blueprint.composer;

import it.cnit.blueprint.composer.exceptions.ApiError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class ExceptionConfiguration {

  @ExceptionHandler(HttpMessageNotReadableException.class)
  protected ResponseEntity<ApiError> handleHttpMessageNotReadable(
      HttpMessageNotReadableException ex) {
    log.debug(ex.getMessage());
    Throwable c = ex.getCause();
    log.error(c.getMessage());
    return ResponseEntity.badRequest()
        .body(new ApiError(HttpStatus.BAD_REQUEST, "my error", "my message", "my path"));
  }

}
