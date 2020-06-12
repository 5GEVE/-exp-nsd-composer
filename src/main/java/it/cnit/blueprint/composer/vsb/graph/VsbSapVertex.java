package it.cnit.blueprint.composer.vsb.graph;

import it.nextworks.nfvmano.catalogue.blueprint.elements.VsbEndpoint;
import java.util.concurrent.atomic.AtomicLong;

public class VsbSapVertex extends VsbVertex {

  private static final AtomicLong NEXT_ID = new AtomicLong(0);

  private final VsbEndpoint vsbEndpoint;
  private final String id;

  public VsbSapVertex(VsbEndpoint vsbEndpoint) {
    this.vsbEndpoint = vsbEndpoint;
    this.id = Long.toString(NEXT_ID.getAndIncrement());
  }

  public String toString() {
    return "SAP_" + String.join("_", this.vsbEndpoint.getEndPointId());
  }

  public int hashCode() {
    return toString().hashCode();
  }

  public boolean equals(Object o) {
    return (o instanceof VsbSapVertex) && (toString().equals(o.toString()));
  }

  public VsbEndpoint getVsbEndpoint() {
    return vsbEndpoint;
  }

}
