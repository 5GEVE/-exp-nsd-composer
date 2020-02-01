package it.cnit.blueprint.expbuilder.nsdgraph;

import it.nextworks.nfvmano.libs.ifa.descriptors.common.elements.VirtualLinkProfile;

public class VirtualLinkProfileVertex extends ProfileVertex {

  private final VirtualLinkProfile vlProfile;

  public VirtualLinkProfileVertex(VirtualLinkProfile vlProfile) {
    this.vlProfile = vlProfile;
  }

  public VirtualLinkProfile getVlProfile() {
    return vlProfile;
  }

  @Override
  public String getElementId() {
    return this.vlProfile.getVirtualLinkProfileId();
  }
}
