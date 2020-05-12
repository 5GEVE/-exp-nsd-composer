package it.cnit.blueprint.composer.nsd.compose;

import it.nextworks.nfvmano.libs.ifa.descriptors.common.elements.VirtualLinkProfile;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsVirtualLinkDesc;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.VirtualLinkToLevelMapping;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VlInfo {

  private NsVirtualLinkDesc vlDescriptor;
  private VirtualLinkProfile vlProfile;
  private VirtualLinkToLevelMapping vlToLevelMapping;
}
