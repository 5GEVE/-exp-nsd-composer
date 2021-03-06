package it.cnit.blueprint.composer.nsd.graph;

public abstract class ProfileVertex {

  public abstract String getElementId();

  public String toString() {
    return this.getElementId();
  }

  public boolean equals(Object o) {
    return (o instanceof ProfileVertex) && (toString().equals(o.toString()));
  }

  public int hashCode() {
    return toString().hashCode();
  }

  public String getVertexId() {
    return String.valueOf(hashCode());
  }

}
