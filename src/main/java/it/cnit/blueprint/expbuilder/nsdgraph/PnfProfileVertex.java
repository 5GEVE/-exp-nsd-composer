package it.cnit.blueprint.expbuilder.nsdgraph;

import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.PnfProfile;

public class PnfProfileVertex extends ProfileVertex {

  private final PnfProfile pnfProfile;

  public PnfProfileVertex(PnfProfile pnfProfile) {
    this.pnfProfile = pnfProfile;
  }

  public PnfProfile getPnfProfile() {
    return pnfProfile;
  }

  @Override
  public String getElementId() {
    return pnfProfile.getPnfProfileId();
  }
}
