package it.cnit.blueprint.composer.nsd.compose;

import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsVirtualLinkConnectivity;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.VnfProfile;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.VnfToLevelMapping;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class VnfInfo {

  private final String vfndId;
  private final VnfProfile vnfProfile;
  private final VnfToLevelMapping vnfToLevelMapping;
  private List<NsVirtualLinkConnectivity> mgmtVlcList = new ArrayList<>();
  private List<NsVirtualLinkConnectivity> dataVlcList = new ArrayList<>();

  public void setCpdLists(String mgmtVlProfileId) {
    for (NsVirtualLinkConnectivity vlc : vnfProfile.getNsVirtualLinkConnectivity()) {
      if (vlc.getVirtualLinkProfileId().equals(mgmtVlProfileId)) {
        mgmtVlcList.add(vlc);
      } else {
        dataVlcList.add(vlc);
      }
    }
  }
}
