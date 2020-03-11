package it.cnit.blueprint.composer.nsd.compose;

import it.cnit.blueprint.composer.rest.ConnectInput;
import it.cnit.blueprint.composer.nsd.graph.NsdGraphService;
import it.cnit.blueprint.composer.rest.InvalidNsd;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.NotExistingEntityException;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsDf;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsLevel;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsVirtualLinkConnectivity;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Nsd;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.VnfProfile;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.VnfToLevelMapping;
import java.util.Map;
import java.util.Map.Entry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

@Service
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
@Slf4j
@Qualifier("PASS_THROUGH")
public class PassThroughComposer extends NsdComposer {

  public PassThroughComposer(NsdGraphService nsdGraphService) {
    super(nsdGraphService);
  }

  @Override
  public void composeWithStrategy(
          ConnectInput connectInput, VlInfo ranVlInfo, VlInfo vsbMgmtVlInfo, VlInfo ctxMgmtVlInfo,
          Nsd vsbNsd, NsDf vsbNsDf, NsLevel vsbNsLvl,
          Nsd ctxNsd, NsDf ctxNsDf, NsLevel ctxNsLvl)
      throws InvalidNsd {
    log.info("Compose with PASS_THROUGH.");
    // Retrieve ctx VNF
    String ctxVnfpId = ctxNsLvl.getVnfToLevelMapping().get(0).getVnfProfileId();
    VnfInfo ctxVnfInfo;
    try {
      ctxVnfInfo = retrieveVnfInfoByProfileId(ctxVnfpId, ctxNsd, ctxNsDf, ctxNsLvl);
      log.debug("Found VnfInfo for vnfpId='{}' in context.", ctxVnfpId);
    } catch (VnfNotFoundInLvlMapping e) {
      log.error(e.getMessage());
      throw new InvalidNsd(e.getMessage());
    }

    // Retrieve non-management VLs from ctx
    // Assumption: select the first Vl attached to ctxVnf
    Map<String, NsVirtualLinkConnectivity> ctxVnfCpds;
    VlInfo ctxNonMgmtVl;
    try {
      ctxVnfCpds = getMgmtDataCpds(ctxVnfInfo, vsbMgmtVlInfo, ctxMgmtVlInfo);
      ctxNonMgmtVl = retrieveVlInfo(ctxVnfCpds.get("data0").getVirtualLinkProfileId(),
          ctxNsd, ctxNsDf, ctxNsLvl);
    } catch (InvalidNsd | VlNotFoundInLvlMapping e) {
      log.error(e.getMessage());
      throw new InvalidNsd(e.getMessage());
    }

    // Retrieve RAN closest VNF information from vsb
    // Assumption: select the first VNF attached to the RAN VL
    // TODO Can create composition inconsistencies across multiple NsLvl
    String ranVnfCpd = null;
    VnfProfile ranVnfProfile = null;
    for (VnfToLevelMapping vnfLvl : vsbNsLvl.getVnfToLevelMapping()) {
      VnfInfo vnfInfo;
      try {
        vnfInfo = retrieveVnfInfoByProfileId(vnfLvl.getVnfProfileId(), vsbNsd, vsbNsDf, vsbNsLvl);
      } catch (VnfNotFoundInLvlMapping e) {
        log.error(e.getMessage());
        throw new InvalidNsd(e.getMessage());
      }
      for (NsVirtualLinkConnectivity vlc : vnfInfo.getVnfProfile().getNsVirtualLinkConnectivity()) {
        if (vlc.getVirtualLinkProfileId()
            .equals(ranVlInfo.getVlProfile().getVirtualLinkProfileId())) {
          ranVnfCpd = vlc.getCpdId().get(0);
          ranVnfProfile = vnfInfo.getVnfProfile();
          break;
        }
      }
    }
    if (ranVnfCpd == null) {
      String m = "Can't find a VNF close to ranVlInfo";
      log.error(m);
      throw new InvalidNsd(m);
    }
    log.debug("ranVnfProfile: '{}'", ranVnfProfile.getVnfProfileId());
    log.debug("ranVnfCpd: '{}'", ranVnfCpd);

    // Modify vsbNsd
    addVnf(ctxVnfInfo, vsbNsd, vsbNsDf, vsbNsLvl);
    log.debug("Added VnfProfile='{}' in service (if not present).",
        ctxVnfInfo.getVnfProfile().getVnfProfileId());
    addVirtualLink(ctxNonMgmtVl, vsbNsd, vsbNsDf, vsbNsLvl);
    log.debug("Added VlProfile='{}' in service (if not present).",
        ctxNonMgmtVl.getVlProfile().getVirtualLinkProfileId());
    try {
      // Connect ranVnf to the new VL coming from ctx
      connectVnfToVL(ranVnfProfile, ranVnfCpd, ctxNonMgmtVl.getVlProfile());
      log.debug("Created connection between vnfProfile='{}' and vlProfile='{}'",
          ranVnfProfile.getVnfProfileId(),
          ctxNonMgmtVl.getVlProfile().getVirtualLinkProfileId());
      // Connect ctxVnf with RAN VL
      connectVnfToVL(ctxVnfInfo.getVnfProfile(), ctxVnfCpds.get("data1").getCpdId().get(0),
          ranVlInfo.getVlProfile());
      log.debug("Created connection between vnfProfile='{}' and vlProfile='{}'",
          ctxVnfInfo.getVnfProfile().getVnfProfileId(),
          ranVlInfo.getVlProfile().getVirtualLinkProfileId());
      // Connect ctxVnf to vsbNsd mgmt VL
      if (ctxVnfCpds.get("mgmt") != null) {
        connectVnfToVL(ctxVnfInfo.getVnfProfile(), ctxVnfCpds.get("mgmt").getCpdId().get(0),
            vsbMgmtVlInfo.getVlProfile());
        log.debug("Created connection between vnfProfile='{}' and vlProfile='{}'",
            ctxVnfInfo.getVnfProfile().getVnfProfileId(),
            vsbMgmtVlInfo.getVlProfile().getVirtualLinkProfileId());
      } else {
        log.warn("Could not find a management Cp for ctxVnf. Skip.");
      }
      // Cleanup unused cpds of ctxVnf (if any)
      for (Entry<String, NsVirtualLinkConnectivity> cpd : ctxVnfCpds.entrySet()) {
        if (!cpd.getKey().equals("data0") && !cpd.getKey().equals("data1") && !cpd.getKey()
            .equals("mgmt")) {
          ctxVnfInfo.getVnfProfile().getNsVirtualLinkConnectivity().remove(cpd.getValue());
        }
      }
    } catch (NotExistingEntityException e) {
      log.error(e.getMessage());
      throw new InvalidNsd(e.getMessage());
    }
  }
}
