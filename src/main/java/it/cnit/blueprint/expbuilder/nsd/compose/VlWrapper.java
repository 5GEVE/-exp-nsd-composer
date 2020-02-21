package it.cnit.blueprint.expbuilder.nsd.compose;

import it.nextworks.nfvmano.libs.ifa.descriptors.common.elements.VirtualLinkProfile;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsVirtualLinkConnectivity;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsVirtualLinkDesc;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.VirtualLinkToLevelMapping;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.VnfProfile;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.VnfToLevelMapping;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VlWrapper {

  private VirtualLinkToLevelMapping vlToLevelMapping;
  private VirtualLinkProfile vlProfile;
  private NsVirtualLinkDesc vlDescriptor;
}
