package it.cnit.blueprint.composer.rest;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class UnprocessableEntityException extends RuntimeException {

  public UnprocessableEntityException() {
  }

  public UnprocessableEntityException(String message) {
    super(message);
  }

  public UnprocessableEntityException(String message, Throwable cause) {
    super(message, cause);
  }
}
