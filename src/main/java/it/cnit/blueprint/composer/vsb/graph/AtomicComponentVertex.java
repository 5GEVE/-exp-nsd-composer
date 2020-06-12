package it.cnit.blueprint.composer.vsb.graph;

import it.nextworks.nfvmano.catalogue.blueprint.elements.VsComponent;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class AtomicComponentVertex extends VsbVertex {

  @Getter
  private final VsComponent vsComponent;

  @Override
  public String getElementId() {
    return this.vsComponent.getComponentId();
  }
}
