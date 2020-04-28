package it.cnit.blueprint.composer.rest;

public class InvalidNsdException extends Exception {

  public InvalidNsdException(String message) {
    super(message);
  }

  public InvalidNsdException(String message, Throwable cause) {
    super(message, cause);
  }
}
