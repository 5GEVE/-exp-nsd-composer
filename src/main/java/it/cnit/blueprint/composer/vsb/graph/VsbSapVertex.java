package it.cnit.blueprint.composer.vsb.graph;

import it.nextworks.nfvmano.catalogue.blueprint.elements.VsbEndpoint;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class VsbSapVertex extends VsbVertex {

  @Getter
  private final VsbEndpoint vsbEndpoint;

  @Override
  public String getElementId() {
    return this.vsbEndpoint.getEndPointId();
  }
}
