package it.cnit.blueprint.composer.vsb.graph;

import it.nextworks.nfvmano.catalogue.blueprint.elements.VsbLink;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class VsbLinkVertex extends VsbVertex {

  @Getter
  private final VsbLink vsbLink;

  @Override
  public String getElementId() {
    return this.vsbLink.getName();
  }
}
