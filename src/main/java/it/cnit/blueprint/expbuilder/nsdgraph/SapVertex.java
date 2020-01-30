package it.cnit.blueprint.expbuilder.nsdgraph;

import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Sapd;

public class SapVertex extends ProfileVertex {

  private final Sapd sapd;

  public SapVertex(Sapd sapd) {
    this.sapd = sapd;
  }

  public Sapd getSapd() {
    return sapd;
  }

  @Override
  public String getElementId() {
    return this.sapd.getCpdId();
  }
}
