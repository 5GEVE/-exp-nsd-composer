package it.cnit.blueprint.expbuilder.vsbgraph;

import it.nextworks.nfvmano.catalogue.blueprint.elements.VsbLink;
import java.util.concurrent.atomic.AtomicLong;

public class VsbLinkVertex extends VsbVertex {

  private static final AtomicLong NEXT_ID = new AtomicLong(0);

  private final VsbLink vsbLink;
  private final String id;

  VsbLinkVertex(VsbLink vsbLink) {
    this.vsbLink = vsbLink;
    this.id = Long.toString(NEXT_ID.getAndIncrement());
  }

  public String toString() {
    return "VL_" + String.join("_", this.vsbLink.getEndPointIds());
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

}
