package eu._5geve.experiment.graph;

import it.nextworks.nfvmano.libs.descriptors.nsd.PnfProfile;

public class PnfProfileVertex implements ProfileVertex {

  private final PnfProfile pnfProfile;
  private final String type;

  PnfProfileVertex(PnfProfile pnfProfile) {
    this.pnfProfile = pnfProfile;
    this.type = "pnfProfile";
  }

  public String toString() {
    return this.type + "_" + this.pnfProfile.getPnfProfileId();
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

  public String getType() {
    return type;
  }

  @Override
  public String getProfileId() {
    return this.pnfProfile.getPnfProfileId();
  }

}
