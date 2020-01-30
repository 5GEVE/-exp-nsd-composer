package it.cnit.blueprint.expbuilder.nsdgraph;

import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.VnfProfile;

public class VnfProfileVertex extends ProfileVertex {

  private final VnfProfile vnfProfile;

  public VnfProfileVertex(VnfProfile vnfProfile) {
    this.vnfProfile = vnfProfile;
  }

  public VnfProfile getVnfProfile() {
    return vnfProfile;
  }

  @Override
  public String getElementId() {
    return this.vnfProfile.getVnfProfileId();
  }
}
