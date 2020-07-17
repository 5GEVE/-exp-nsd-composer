package it.cnit.blueprint.composer;

import com.fasterxml.jackson.core.JsonProcessingException;
import it.cnit.blueprint.composer.exceptions.ApiError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class ExceptionConfiguration {

  @ExceptionHandler(JsonProcessingException.class)
  protected ResponseEntity<ApiError> handleJsonProcessingException(
      JsonProcessingException ex) {
    ApiError err = new ApiError(
        HttpStatus.BAD_REQUEST,
        ex.getOriginalMessage() + ". Error at line " + ex.getLocation().getLineNr() + ", column " +
            ex.getLocation().getColumnNr() + "."
    );
    return ResponseEntity.badRequest().body(err);
  }

}
