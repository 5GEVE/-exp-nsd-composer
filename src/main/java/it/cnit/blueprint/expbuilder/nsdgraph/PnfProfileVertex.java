package it.cnit.blueprint.expbuilder.nsdgraph;

import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.PnfProfile;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class PnfProfileVertex extends ProfileVertex {

  @Getter
  private final PnfProfile pnfProfile;

  @Override
  public String getElementId() {
    return pnfProfile.getPnfProfileId();
  }
}
