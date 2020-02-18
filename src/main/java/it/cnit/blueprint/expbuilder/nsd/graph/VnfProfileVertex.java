package it.cnit.blueprint.expbuilder.nsd.graph;

import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.VnfProfile;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class VnfProfileVertex extends ProfileVertex {

  @Getter
  private final VnfProfile vnfProfile;

  @Override
  public String getElementId() {
    return this.vnfProfile.getVnfProfileId();
  }
}
