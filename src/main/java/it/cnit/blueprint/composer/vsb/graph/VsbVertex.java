package it.cnit.blueprint.composer.vsb.graph;

public abstract class VsbVertex {

  public abstract String getElementId();

  public String toString() {
    return this.getElementId();
  }

  public boolean equals(Object o) {
    return (o instanceof VsbVertex) && (toString().equals(o.toString()));
  }

  public int hashCode() {
    return toString().hashCode();
  }

  public String getVertexId() {
    return String.valueOf(hashCode());
  }

}
