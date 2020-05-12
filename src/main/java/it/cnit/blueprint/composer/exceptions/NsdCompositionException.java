package it.cnit.blueprint.composer.exceptions;

public class NsdCompositionException extends DescriptorInvalidException {

  public NsdCompositionException(String descId, String message) {
    super(descId, message);
  }

  public NsdCompositionException(String descId, String message, Throwable cause) {
    super(descId, message, cause);
  }
}
