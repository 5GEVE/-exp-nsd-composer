package it.cnit.blueprint.composer.exceptions;

public class InvalidNsdException extends Exception {

  public InvalidNsdException(String message) {
    super(message);
  }

  public InvalidNsdException(String message, Throwable cause) {
    super(message, cause);
  }
}
