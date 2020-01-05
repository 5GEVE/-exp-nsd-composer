package eu._5geve.experiment.nsdgraph;

import it.nextworks.nfvmano.libs.ifa.descriptors.common.elements.VirtualLinkProfile;

public class VirtualLinkProfileVertex implements ProfileVertex {

  private final VirtualLinkProfile vlProfile;

  VirtualLinkProfileVertex(VirtualLinkProfile vlProfile) {
    this.vlProfile = vlProfile;
  }

  public String toString() {
    return "vlProfile_" + this.vlProfile.getVirtualLinkProfileId();
  }

  public int hashCode() {
    return toString().hashCode();
  }

  public boolean equals(Object o) {
    return (o instanceof VirtualLinkProfileVertex) && (toString().equals(o.toString()));
  }

  public VirtualLinkProfile getVlProfile() {
    return vlProfile;
  }

  @Override
  public String getProfileId() {
    return this.vlProfile.getVirtualLinkProfileId();
  }

}
