package eu._5geve.experiment.vsbgraph;

import eu._5geve.blueprint.vsb.VsComponent;

public class AtomicComponentVertex implements VsbVertex {

  private final VsComponent vsComponent;
  private final String type;

  AtomicComponentVertex(VsComponent vsComponent) {
    this.vsComponent = vsComponent;
    this.type = "atomicComponent";
  }

  public String toString() {
    return this.type + "_" + this.vsComponent.getComponentId();
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

  @Override
  public String getType() {
    return this.type;
  }

}
