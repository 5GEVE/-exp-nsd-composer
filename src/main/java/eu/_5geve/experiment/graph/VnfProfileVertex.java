package eu._5geve.experiment.graph;

import it.nextworks.nfvmano.libs.descriptors.nsd.VnfProfile;

public class VnfProfileVertex implements ProfileVertex {

  private final VnfProfile vnfProfile;
  private final String type;

  VnfProfileVertex(VnfProfile vnfProfile) {
    this.vnfProfile = vnfProfile;
    this.type = "vnfProfile";
  }

  public String toString() {
    return this.type + "_" + this.vnfProfile.getVnfProfileId();
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

  public String getType() {
    return type;
  }

  @Override
  public String getProfileId() {
    return this.vnfProfile.getVnfProfileId();
  }

}
