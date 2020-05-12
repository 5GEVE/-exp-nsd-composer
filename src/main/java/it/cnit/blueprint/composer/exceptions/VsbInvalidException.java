package it.cnit.blueprint.composer.exceptions;

public class VsbInvalidException extends DescriptorInvalidException {

  public VsbInvalidException(String descId, String message) {
    super(descId, message);
  }

  public VsbInvalidException(String descId, String message, Throwable cause) {
    super(descId, message, cause);
  }
}
