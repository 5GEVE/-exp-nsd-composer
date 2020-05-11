package it.cnit.blueprint.composer.exceptions;

public class NsdInvalidException extends Exception {

  public NsdInvalidException(String message) {
    super(message);
  }

  public NsdInvalidException(String message, Throwable cause) {
    super(message, cause);
  }
}
