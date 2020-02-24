package it.cnit.blueprint.expbuilder.nsd.compose;

import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsVirtualLinkConnectivity;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.VnfProfile;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.VnfToLevelMapping;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VnfWrapper {

  private VnfToLevelMapping vnfToLevelMapping;
  private VnfProfile vnfProfile;
  private NsVirtualLinkConnectivity vlConnectivity;
  private String vfndId;
}
