package it.cnit.blueprint.expbuilder.nsd.compose;

import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsVirtualLinkConnectivity;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.VnfProfile;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.VnfToLevelMapping;
import lombok.Data;
import lombok.NonNull;

@Data
public class VnfWrapper {

  @NonNull
  private VnfToLevelMapping vnfToLevelMapping;
  @NonNull
  private VnfProfile vnfProfile;
  @NonNull
  private NsVirtualLinkConnectivity vlConnectivity;
  @NonNull
  private String vfndId;
}
