package eu._5geve.experiment.nsdgraph;

import it.nextworks.nfvmano.libs.descriptors.nsd.Sapd;

public class SapVertex implements ProfileVertex {

  private final Sapd sapd;

  SapVertex(Sapd sapd) {
    this.sapd = sapd;
  }

  public String toString() {
    return "Sap_" + this.sapd.getCpdId();
  }

  public int hashCode() {
    return toString().hashCode();
  }

  public boolean equals(Object o) {
    return (o instanceof SapVertex) && (toString().equals(o.toString()));
  }

  public Sapd getSapd() {
    return sapd;
  }

  @Override
  public String getProfileId() {
    return this.sapd.getCpdId();
  }

}
