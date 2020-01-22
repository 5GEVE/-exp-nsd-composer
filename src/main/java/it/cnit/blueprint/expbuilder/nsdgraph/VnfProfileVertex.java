package it.cnit.blueprint.expbuilder.nsdgraph;

import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.VnfProfile;

public class VnfProfileVertex extends ProfileVertex {

  private final VnfProfile vnfProfile;

  VnfProfileVertex(VnfProfile vnfProfile) {
    this.vnfProfile = vnfProfile;
  }

  public String toString() {
    return "vnfProfile_" + this.vnfProfile.getVnfProfileId();
  }

  public int hashCode() {
    return toString().hashCode();
  }

  public boolean equals(Object o) {
    return (o instanceof VnfProfileVertex) && (toString().equals(o.toString()));
  }

  public VnfProfile getVnfProfile() {
    return vnfProfile;
  }

}
