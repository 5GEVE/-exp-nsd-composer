package eu._5geve.experiment.nsdgraph;

import it.nextworks.nfvmano.libs.descriptors.nsd.VnfProfile;

public class VnfProfileVertex implements ProfileVertex {

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

  @Override
  public String getProfileId() {
    return this.vnfProfile.getVnfProfileId();
  }

}
