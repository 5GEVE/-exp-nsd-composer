package it.cnit.blueprint.composer.vsb.graph;

import it.nextworks.nfvmano.catalogue.blueprint.elements.VsComponent;

public class AtomicComponentVertex extends VsbVertex {

  private final VsComponent vsComponent;

  AtomicComponentVertex(VsComponent vsComponent) {
    this.vsComponent = vsComponent;
  }

  public String toString() {
    return "AC_" + this.vsComponent.getComponentId();
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

}
