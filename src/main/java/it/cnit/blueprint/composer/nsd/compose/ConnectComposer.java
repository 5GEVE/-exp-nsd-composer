package it.cnit.blueprint.composer.nsd.compose;

import it.cnit.blueprint.composer.nsd.graph.NsdGraphService;
import it.cnit.blueprint.composer.rest.InvalidNsdException;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.NotExistingEntityException;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsDf;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsLevel;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Nsd;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.VirtualLinkToLevelMapping;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.VnfToLevelMapping;
import java.util.Arrays;
import java.util.List;
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
@Qualifier("CONNECT")
public class ConnectComposer extends NsdComposer {

  public ConnectComposer(NsdGraphService nsdGraphService) {
    super(nsdGraphService);
  }

  private String getNonMgmtVlProfileId(NsLevel expNsLvl, String mgmtVlpId, String ranVlpId)
      throws NotExistingEntityException {
    for (VirtualLinkToLevelMapping m : expNsLvl.getVirtualLinkToLevelMapping()) {
      if (!m.getVirtualLinkProfileId().equals(mgmtVlpId)
          &&
          !m.getVirtualLinkProfileId().equals(ranVlpId)) {
        return m.getVirtualLinkProfileId();
      }
    }
    throw new NotExistingEntityException(
        "Can't find a non-mgmt VL in NsLevel " + expNsLvl.getNsLevelId());
  }

  private void addConnectVnfToVl(VnfInfo vnfInfo, VlInfo dataVlInfo, VlInfo mgmtVlInfo, Nsd expNsd,
      NsDf expNsDf, NsLevel expNsLvl)
      throws NotExistingEntityException {
    // Modify expNsd
    addVnf(vnfInfo, expNsd, expNsDf, expNsLvl);
    log.debug(
        "Added VnfProfile='{}' in service (if not present).",
        vnfInfo.getVnfProfile().getVnfProfileId());
    connectVnfToVL(vnfInfo.getVnfProfile(), vnfInfo.getDataVlcList().get(0).getCpdId().get(0),
        dataVlInfo.getVlProfile());
    log.debug(
        "Created connection between vnfProfile='{}' and vlProfile='{}'",
        vnfInfo.getVnfProfile().getVnfProfileId(),
        dataVlInfo.getVlProfile().getVirtualLinkProfileId());
    for (int i = 1; i < vnfInfo.getDataVlcList().size(); i++) {
      vnfInfo.cleanUpVlc(vnfInfo.getDataVlcList().get(i));
    }
    if (!vnfInfo.getMgmtVlcList().isEmpty()) {
      connectVnfToVL(vnfInfo.getVnfProfile(), vnfInfo.getMgmtVlcList().get(0).getCpdId().get(0),
          mgmtVlInfo.getVlProfile());
    } else {
      log.warn("Could not find a management Vlc for {}. Skip.",
          vnfInfo.getVnfProfile().getVnfProfileId());
    }
  }

  @Override
  public void composeWithStrategy(
      Map<String, String> connectInput,
      VlInfo ranVlInfo,
      VlInfo expMgmtVlInfo,
      VlInfo ctxMgmtVlInfo,
      Nsd expNsd,
      NsDf expNsDf,
      NsLevel expNsLvl,
      Nsd ctxNsd,
      NsDf ctxNsDf,
      NsLevel ctxNsLvl)
      throws InvalidNsdException {
    log.info("Compose with CONNECT.");
    List<String> mgmtVlProfileIds = Arrays.asList(
        ctxMgmtVlInfo.getVlProfile().getVirtualLinkProfileId(),
        expMgmtVlInfo.getVlProfile().getVirtualLinkProfileId());
    if (connectInput.isEmpty()) {
      // select all VNFs
      boolean first = true;
      for (VnfToLevelMapping vnfMap : ctxNsLvl.getVnfToLevelMapping()) {
        String vnfpId = vnfMap.getVnfProfileId();
        VnfInfo vnfInfo;
        try {
          vnfInfo = retrieveVnfInfoByProfileId(vnfpId, ctxNsd, ctxNsDf, ctxNsLvl);
          vnfInfo.setVlcLists(mgmtVlProfileIds);
        } catch (NotExistingEntityException e) {
          throw new InvalidNsdException(
              "Error retrieving VNF info for VNF profile ID " + vnfpId, e);
        }
        VlInfo vlInfo;
        if (first) {
          vlInfo = ranVlInfo;
          first = false;
        } else {
          try {
            String nonMgmtVlpId = getNonMgmtVlProfileId(
                expNsLvl,
                expMgmtVlInfo.getVlProfile().getVirtualLinkProfileId(),
                ranVlInfo.getVlProfile().getVirtualLinkProfileId());
            vlInfo = retrieveVlInfoByProfileId(nonMgmtVlpId, expNsd, expNsDf, expNsLvl);
          } catch (NotExistingEntityException e) {
            throw new InvalidNsdException("Error retrieving VL info for a non-management VL", e);
          }
        }
        try {
          addConnectVnfToVl(vnfInfo, vlInfo, expMgmtVlInfo, expNsd, expNsDf, expNsLvl);
        } catch (NotExistingEntityException e) {
          throw new InvalidNsdException(
              "Error connecting VNF profile " + vnfInfo.getVnfProfile().getVnfProfileId(), e);
        }
      }
    } else {
      // only VNFs in connectInput
      for (Entry<String, String> entry : connectInput.entrySet()) {
        VnfInfo vnfInfo;
        try {
          String vnfpId = getVnfProfileByDescId(entry.getKey(), ctxNsDf).getVnfProfileId();
          vnfInfo = retrieveVnfInfoByProfileId(vnfpId, ctxNsd, ctxNsDf, ctxNsLvl);
          vnfInfo.setVlcLists(mgmtVlProfileIds);
        } catch (NotExistingEntityException e) {
          throw new InvalidNsdException(
              "Error retrieving VNF info for VNFD ID " + entry.getKey(), e);
        }
        VlInfo vlInfo;
        try {
          String vlpId = getVlProfileByDescId(entry.getValue(), ctxNsDf).getVirtualLinkProfileId();
          vlInfo = retrieveVlInfoByProfileId(vlpId, ctxNsd, ctxNsDf, ctxNsLvl);
        } catch (NotExistingEntityException e) {
          throw new InvalidNsdException(
              "Error retrieving VL info for VLD ID " + entry.getValue(), e);
        }
        try {
          addConnectVnfToVl(vnfInfo, vlInfo, expMgmtVlInfo, expNsd, expNsDf, expNsLvl);
        } catch (NotExistingEntityException e) {
          throw new InvalidNsdException(
              "Error connecting VNF profile " + vnfInfo.getVnfProfile().getVnfProfileId(), e);
        }
      }
    }
  }
}
