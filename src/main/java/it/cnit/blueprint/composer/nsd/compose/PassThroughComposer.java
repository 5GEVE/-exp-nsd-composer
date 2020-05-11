package it.cnit.blueprint.composer.nsd.compose;

import it.cnit.blueprint.composer.nsd.graph.NsdGraphService;
import it.cnit.blueprint.composer.exceptions.NsdInvalidException;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.NotExistingEntityException;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsDf;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsLevel;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsVirtualLinkConnectivity;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Nsd;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.VnfProfile;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.VnfToLevelMapping;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
      Map<String, String> connectInput, VlInfo ranVlInfo, VlInfo expMgmtVlInfo,
      VlInfo ctxMgmtVlInfo,
      Nsd expNsd, NsDf expNsDf, NsLevel expNsLvl,
      Nsd ctxNsd, NsDf ctxNsDf, NsLevel ctxNsLvl)
      throws NsdInvalidException {
    log.info("Compose with PASS_THROUGH.");
    List<String> mgmtVlProfileIds = Arrays.asList(
        ctxMgmtVlInfo.getVlProfile().getVirtualLinkProfileId(),
        expMgmtVlInfo.getVlProfile().getVirtualLinkProfileId());

    // Retrieve ctx VNF
    // Assumption: a PASS_TROUGH context has only one VnfD and VnfProfile.
    String ctxVnfpId = ctxNsLvl.getVnfToLevelMapping().get(0).getVnfProfileId();
    VnfInfo ctxVnfInfo;
    try {
      ctxVnfInfo = retrieveVnfInfoByProfileId(ctxVnfpId, ctxNsd, ctxNsDf, ctxNsLvl);
      log.debug("Found VnfInfo for vnfpId {} in context.", ctxVnfpId);
      ctxVnfInfo.setVlcLists(mgmtVlProfileIds);
    } catch (NotExistingEntityException e) {
      throw new NsdInvalidException(
          "Error retrieving VNF info for VNF profile ID " + ctxVnfpId, e);
    }

    // Retrieve non-management VLs from ctx
    // Assumption: select the first Vl attached to ctxVnf
    VlInfo ctxNonMgmtVl;
    try {
      ctxNonMgmtVl = retrieveVlInfoByProfileId(
          ctxVnfInfo.getDataVlcList().get(0).getVirtualLinkProfileId(),
          ctxNsd, ctxNsDf, ctxNsLvl);
    } catch (NotExistingEntityException e) {
      throw new NsdInvalidException("Error retrieving VL info for a non-management VL", e);
    }

    // Retrieve RAN closest VNF information from exp
    // Assumption: select the first VNF attached to the RAN VL
    // TODO Can create composition inconsistencies across multiple NsLvl
    String ranVnfCpd = null;
    VnfProfile ranVnfProfile = null;
    for (VnfToLevelMapping vnfLvl : expNsLvl.getVnfToLevelMapping()) {
      VnfProfile vnfProfile;
      try {
        vnfProfile = getVnfProfileById(vnfLvl.getVnfProfileId(), expNsDf);
      } catch (NotExistingEntityException e) {
        throw new NsdInvalidException(
            "Error retrieving VNF info for VNF profile ID " + vnfLvl.getVnfProfileId(), e);
      }
      for (NsVirtualLinkConnectivity vlc : vnfProfile.getNsVirtualLinkConnectivity()) {
        if (vlc.getVirtualLinkProfileId()
            .equals(ranVlInfo.getVlProfile().getVirtualLinkProfileId())) {
          ranVnfCpd = vlc.getCpdId().get(0);
          ranVnfProfile = vnfProfile;
          break;
        }
      }
    }
    if (ranVnfCpd == null) {
      throw new NsdInvalidException(
          "Can't find a VNF close to ranVlInfo in nsLevel " + expNsLvl.getNsLevelId());
    }
    log.debug("ranVnfProfile: {}", ranVnfProfile.getVnfProfileId());
    log.debug("ranVnfCpd: {}", ranVnfCpd);

    // Modify expNsd
    addVnf(ctxVnfInfo, expNsd, expNsDf, expNsLvl);
    log.debug("Added VnfProfile {} in service (if not present).",
        ctxVnfInfo.getVnfProfile().getVnfProfileId());
    addVirtualLink(ctxNonMgmtVl, expNsd, expNsDf, expNsLvl);
    log.debug("Added VlProfile {} in service (if not present).",
        ctxNonMgmtVl.getVlProfile().getVirtualLinkProfileId());
    try {
      // Connect ranVnf to the new VL coming from ctx
      connectVnfToVL(ranVnfProfile, ranVnfCpd, ctxNonMgmtVl.getVlProfile());
      log.debug("Created connection between vnfProfile {} and vlProfile {}",
          ranVnfProfile.getVnfProfileId(),
          ctxNonMgmtVl.getVlProfile().getVirtualLinkProfileId());
      // Connect ctxVnf with RAN VL
      connectVnfToVL(ctxVnfInfo.getVnfProfile(),
          ctxVnfInfo.getDataVlcList().get(1).getCpdId().get(0),
          ranVlInfo.getVlProfile());
      log.debug("Created connection between vnfProfile {} and vlProfile {}",
          ctxVnfInfo.getVnfProfile().getVnfProfileId(),
          ranVlInfo.getVlProfile().getVirtualLinkProfileId());
      // Connect ctxVnf to expNsd mgmt VL
      if (!ctxVnfInfo.getMgmtVlcList().isEmpty()) {
        connectVnfToVL(ctxVnfInfo.getVnfProfile(),
            ctxVnfInfo.getMgmtVlcList().get(0).getCpdId().get(0),
            expMgmtVlInfo.getVlProfile());
        log.debug("Created connection between vnfProfile {} and vlProfile {}",
            ctxVnfInfo.getVnfProfile().getVnfProfileId(),
            expMgmtVlInfo.getVlProfile().getVirtualLinkProfileId());
      } else {
        log.warn("Could not find a management Cp for ctxVnf. Skip.");
      }
      // Cleanup unused cpds of ctxVnf (if any)
      for (int i = 2; i < ctxVnfInfo.getDataVlcList().size(); i++) {
        ctxVnfInfo.cleanUpVlc(ctxVnfInfo.getDataVlcList().get(i));
      }
    } catch (NotExistingEntityException e) {
      throw new NsdInvalidException("Error in connecting VNF to VL.", e);
    }
  }
}
