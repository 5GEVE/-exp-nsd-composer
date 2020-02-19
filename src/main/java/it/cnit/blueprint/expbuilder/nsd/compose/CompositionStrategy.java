package it.cnit.blueprint.expbuilder.nsd.compose;

import it.cnit.blueprint.expbuilder.rest.CtxComposeInfo;
import it.cnit.blueprint.expbuilder.rest.InvalidCtxComposeInfo;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsDf;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsLevel;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Nsd;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.VnfProfile;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.VnfToLevelMapping;

public abstract class CompositionStrategy {

  void addVnf(Nsd nsd, NsDf nsDf, NsLevel nsLevel, VnfProfile vnfProfile,
      VnfToLevelMapping vnfLvlMap) {
    if (nsd.getVnfdId().stream().noneMatch(id -> id.equals(vnfProfile.getVnfdId()))) {
      nsd.getVnfdId().add(vnfProfile.getVnfdId());
    }
    if (nsDf.getVnfProfile().stream()
        .noneMatch(vp -> vp.getVnfProfileId().equals(vnfProfile.getVnfProfileId()))) {
      nsDf.getVnfProfile().add(vnfProfile);
    }
    nsLevel.getVnfToLevelMapping().add(vnfLvlMap);
  }

  abstract void compose(Nsd nsd, NsDf nsDf, NsLevel nsLevel, CtxComposeInfo composeInfo)
      throws InvalidCtxComposeInfo;

}
