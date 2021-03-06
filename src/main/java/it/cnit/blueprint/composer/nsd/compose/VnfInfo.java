package it.cnit.blueprint.composer.nsd.compose;

import it.nextworks.nfvmano.libs.ifa.common.exceptions.NotExistingEntityException;
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

  private final String vnfdId;
  private final VnfProfile vnfProfile;
  private final VnfToLevelMapping vnfToLevelMapping;
  private List<NsVirtualLinkConnectivity> mgmtVlcList;
  private List<NsVirtualLinkConnectivity> dataVlcList;

  public void setVlcLists(List<String> mgmtVlProfileIds) throws NotExistingEntityException {
    mgmtVlcList = new ArrayList<>();
    dataVlcList = new ArrayList<>();
    for (NsVirtualLinkConnectivity vlc : vnfProfile.getNsVirtualLinkConnectivity()) {
      if (mgmtVlProfileIds.contains(vlc.getVirtualLinkProfileId())) {
        mgmtVlcList.add(vlc);
      } else {
        dataVlcList.add(vlc);
      }
    }
    if (dataVlcList.isEmpty()) {
      throw new NotExistingEntityException(
          "No data cpd found for vnfProfile: " + vnfProfile.getVnfProfileId());
    }
  }

  public void cleanUpVlc(NsVirtualLinkConnectivity vlcToRemove) {
    vnfProfile.getNsVirtualLinkConnectivity().remove(vlcToRemove);
  }
}
