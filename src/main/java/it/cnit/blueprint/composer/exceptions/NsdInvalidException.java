package it.cnit.blueprint.composer.exceptions;

public class NsdInvalidException extends DescriptorInvalidException {

  public NsdInvalidException(String descId, String message) {
    super(descId, message);
  }

  public NsdInvalidException(String descId, String message, Throwable cause) {
    super(descId, message, cause);
  }
}
