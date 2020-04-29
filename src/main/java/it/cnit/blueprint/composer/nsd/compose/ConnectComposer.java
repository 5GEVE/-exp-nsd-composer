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
    try {
      if (connectInput.isEmpty()) {
        // select all VNFs
        boolean first = true;
        for (VnfToLevelMapping vnfMap : ctxNsLvl.getVnfToLevelMapping()) {
          String vnfpId = vnfMap.getVnfProfileId();
          VnfInfo vnfInfo = retrieveVnfInfoByProfileId(vnfpId, ctxNsd, ctxNsDf, ctxNsLvl);
          vnfInfo.setVlcLists(mgmtVlProfileIds);
          VlInfo vlInfo;
          if (first) {
            vlInfo = ranVlInfo;
            first = false;
          } else {
            String nonMgmtVlpId = getNonMgmtVlProfileId(
                expNsLvl,
                expMgmtVlInfo.getVlProfile().getVirtualLinkProfileId(),
                ranVlInfo.getVlProfile().getVirtualLinkProfileId());
            vlInfo = retrieveVlInfo(nonMgmtVlpId, expNsd, expNsDf, expNsLvl);
          }
          addConnectVnfToVl(vnfInfo, vlInfo, expMgmtVlInfo, expNsd, expNsDf, expNsLvl);
        }
      } else {
        // only VNFs in connectInput
        for (Entry<String, String> entry : connectInput.entrySet()) {
          String vnfpId = getVnfProfileByDescId(entry.getKey(), ctxNsDf).getVnfProfileId();
          VnfInfo vnfInfo = retrieveVnfInfoByProfileId(vnfpId, ctxNsd, ctxNsDf, ctxNsLvl);
          vnfInfo.setVlcLists(mgmtVlProfileIds);
          String vlpId = getVlProfileByDescId(entry.getValue(), ctxNsDf).getVirtualLinkProfileId();
          VlInfo vlInfo = retrieveVlInfo(vlpId, ctxNsd, ctxNsDf, ctxNsLvl);
          addConnectVnfToVl(vnfInfo, vlInfo, expMgmtVlInfo, expNsd, expNsDf, expNsLvl);
        }
      }
    } catch (NotExistingEntityException | VnfNotFoundInLvlMapping | VlNotFoundInLvlMapping e) {
      log.error(e.getMessage());
      throw new InvalidNsdException("Nsd " + ctxNsd.getNsdIdentifier(), e);
    }
  }
}
