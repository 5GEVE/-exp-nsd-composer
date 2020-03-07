package it.cnit.blueprint.expbuilder.nsd.compose;

import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.VnfProfile;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.VnfToLevelMapping;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VnfInfo
{

  private String vfndId;
  private VnfProfile vnfProfile;
  private VnfToLevelMapping vnfToLevelMapping;
}
