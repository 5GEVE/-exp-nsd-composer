package it.cnit.blueprint.composer.exceptions;

public class DescriptorInvalidException extends Exception {

  private String descId;

  public DescriptorInvalidException(String descId, String message) {
    super(message);
    this.descId = descId;
  }

  public DescriptorInvalidException(String descId, String message, Throwable cause) {
    super(message, cause);
    this.descId = descId;
  }

  @Override
  public String getMessage() {
    return String.join(" ", descId, "is invalid:", super.getMessage());
  }
}
