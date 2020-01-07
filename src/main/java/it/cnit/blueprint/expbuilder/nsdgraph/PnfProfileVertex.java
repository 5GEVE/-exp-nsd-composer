package it.cnit.blueprint.expbuilder.nsdgraph;

import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.PnfProfile;

public class PnfProfileVertex implements ProfileVertex {

  private final PnfProfile pnfProfile;

  PnfProfileVertex(PnfProfile pnfProfile) {
    this.pnfProfile = pnfProfile;
  }

  public String toString() {
    return "pnfProfile_" + this.pnfProfile.getPnfProfileId();
  }

  public int hashCode() {
    return toString().hashCode();
  }

  public boolean equals(Object o) {
    return (o instanceof PnfProfileVertex) && (toString().equals(o.toString()));
  }

  public PnfProfile getPnfProfile() {
    return pnfProfile;
  }

  @Override
  public String getProfileId() {
    return this.pnfProfile.getPnfProfileId();
  }

}
