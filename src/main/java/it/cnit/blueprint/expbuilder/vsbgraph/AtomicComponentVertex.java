package it.cnit.blueprint.expbuilder.vsbgraph;

import it.nextworks.nfvmano.catalogue.blueprint.elements.VsComponent;

public class AtomicComponentVertex implements VsbVertex {

  private final VsComponent vsComponent;

  AtomicComponentVertex(VsComponent vsComponent) {
    this.vsComponent = vsComponent;
  }

  public String toString() {
    return "atomicComponent_" + this.vsComponent.getComponentId();
  }

  public int hashCode() {
    return toString().hashCode();
  }

  public boolean equals(Object o) {
    return (o instanceof AtomicComponentVertex) && (toString().equals(o.toString()));
  }

  public VsComponent getVsComponent() {
    return vsComponent;
  }

  @Override
  public String getId() {
    return vsComponent.getComponentId();
  }

}
