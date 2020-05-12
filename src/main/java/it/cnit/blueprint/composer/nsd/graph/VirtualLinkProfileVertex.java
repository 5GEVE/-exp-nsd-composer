package it.cnit.blueprint.composer.nsd.graph;

import it.nextworks.nfvmano.libs.ifa.descriptors.common.elements.VirtualLinkProfile;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class VirtualLinkProfileVertex extends ProfileVertex {

  @Getter
  private final VirtualLinkProfile vlProfile;

  @Override
  public String getElementId() {
    return this.vlProfile.getVirtualLinkProfileId();
  }
}
