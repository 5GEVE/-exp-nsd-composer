package eu._5geve.experiment.vsbgraph;

import eu._5geve.blueprint.vsb.VsbLink;

public class VsbLinkVertex implements VsbVertex {

  private final VsbLink vsbLink;
  private final String id;
  private final String type;

  VsbLinkVertex(VsbLink vsbLink) {
    this.vsbLink = vsbLink;
    this.id = Integer.toString(System.identityHashCode(this));
    this.type = "vsbLink";
  }

  public String toString() {
    return this.type + "_" + String.join("_", this.vsbLink.getEndPointIds());
  }

  public int hashCode() {
    return toString().hashCode();
  }

  public boolean equals(Object o) {
    return (o instanceof VsbLinkVertex) && (toString().equals(o.toString()));
  }

  public VsbLink getVsbLink() {
    return vsbLink;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public String getType() {
    return this.type;
  }
}
