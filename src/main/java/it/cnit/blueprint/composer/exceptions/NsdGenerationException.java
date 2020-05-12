package it.cnit.blueprint.composer.exceptions;

public class NsdGenerationException extends DescriptorInvalidException {

  public NsdGenerationException(String descId, String message) {
    super(descId, message);
  }

  public NsdGenerationException(String descId, String message, Throwable cause) {
    super(descId, message, cause);
  }
}
