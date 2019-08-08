package eu._5geve.experiment.graph;

import it.nextworks.nfvmano.libs.descriptors.common.elements.VirtualLinkProfile;

public class VirtualLinkProfileVertex implements ProfileVertex {

  private final VirtualLinkProfile vlProfile;
  private final String type;

  VirtualLinkProfileVertex(VirtualLinkProfile vlProfile) {
    this.vlProfile = vlProfile;
    this.type = "vlProfile";
  }

  public String toString() {
    return this.type + "-" + this.vlProfile.getVirtualLinkProfileId();
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

  public String getType() {
    return type;
  }

  @Override
  public String getProfileId() {
    return this.vlProfile.getVirtualLinkProfileId();
  }

}
