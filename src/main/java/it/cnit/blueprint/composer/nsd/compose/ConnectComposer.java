package it.cnit.blueprint.composer.nsd.compose;

import it.cnit.blueprint.composer.rest.ConnectInput;
import it.cnit.blueprint.composer.nsd.graph.NsdGraphService;
import it.cnit.blueprint.composer.rest.InvalidNsdException;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.NotExistingEntityException;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsDf;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsLevel;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsVirtualLinkConnectivity;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Nsd;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.VirtualLinkToLevelMapping;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.VnfToLevelMapping;
import it.nextworks.nfvmano.libs.ifa.descriptors.vnfd.Vnfd;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
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

  private VirtualLinkToLevelMapping getNonMgmtVlMap(NsLevel expNsLvl, VlInfo expMgmtVlInfo,
      VlInfo ranVlInfo) throws NotExistingEntityException {
    Optional<VirtualLinkToLevelMapping> optLvlMap =
        expNsLvl.getVirtualLinkToLevelMapping().stream()
            .filter(
                m ->
                    !m.getVirtualLinkProfileId()
                        .equals(expMgmtVlInfo.getVlProfile().getVirtualLinkProfileId())
                        && !m.getVirtualLinkProfileId()
                        .equals(ranVlInfo.getVlProfile().getVirtualLinkProfileId()))
            .findFirst();
    if (optLvlMap.isPresent()) {
      return optLvlMap.get();
    } else {
      throw new NotExistingEntityException(
          "Can't find a non-mgmt VL in NsLevel " + expNsLvl.getNsLevelId());
    }
  }

  private void addConnectVnfToVl(VnfInfo vnfInfo, Map<String, NsVirtualLinkConnectivity> cpds,
      VlInfo dataVlInfo, VlInfo mgmtVlInfo, Nsd expNsd, NsDf expNsDf, NsLevel expNsLvl)
      throws NotExistingEntityException {
    // Modify expNsd
    addVnf(vnfInfo, expNsd, expNsDf, expNsLvl);
    log.debug(
        "Added VnfProfile='{}' in service (if not present).",
        vnfInfo.getVnfProfile().getVnfProfileId());
    connectVnfToVL(
        vnfInfo.getVnfProfile(), cpds.get("data0").getCpdId().get(0), dataVlInfo.getVlProfile());
    log.debug(
        "Created connection between vnfProfile='{}' and vlProfile='{}'",
        vnfInfo.getVnfProfile().getVnfProfileId(),
        dataVlInfo.getVlProfile().getVirtualLinkProfileId());
    if (cpds.get("mgmt") != null) {
      connectVnfToVL(
          vnfInfo.getVnfProfile(), cpds.get("mgmt").getCpdId().get(0), mgmtVlInfo.getVlProfile());
    } else {
      log.warn("Could not find a management Cp for srcVnf. Skip.");
    }
    for (Entry<String, NsVirtualLinkConnectivity> cpd : cpds.entrySet()) {
      if (!cpd.getKey().equals("data0") && !cpd.getKey().equals("mgmt")) {
        vnfInfo.getVnfProfile().getNsVirtualLinkConnectivity().remove(cpd.getValue());
      }
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
    // Retrieve ctx VNFs
    List<VnfInfo> vnfToConnect = new ArrayList<>();
    try {
      if (connectInput.isEmpty()) {
        // select all VNFs
        boolean first = true;
        for (VnfToLevelMapping vnfMap : ctxNsLvl.getVnfToLevelMapping()) {
          String vnfpId = vnfMap.getVnfProfileId();
          VnfInfo vnfInfo = retrieveVnfInfoByProfileId(vnfpId, ctxNsd, ctxNsDf, ctxNsLvl);
          Map<String, NsVirtualLinkConnectivity> cpds =
              getMgmtDataCpds(vnfInfo, expMgmtVlInfo, ctxMgmtVlInfo);
          VlInfo vlInfo;
          if (first) {
            vlInfo = ranVlInfo;
            first = false;
          } else {
            vlInfo =
                retrieveVlInfo(
                    getNonMgmtVlMap(expNsLvl, expMgmtVlInfo, ranVlInfo).getVirtualLinkProfileId(),
                    expNsd,
                    expNsDf,
                    expNsLvl);
          }
          addConnectVnfToVl(vnfInfo, cpds, vlInfo, expMgmtVlInfo, expNsd, expNsDf, expNsLvl);
        }
      } else {
        // only VNFs in connectInput
        for (Entry<String, String> entry : connectInput.entrySet()) {
          String vnfpId = getVnfProfileByDescId(entry.getKey(), ctxNsDf).getVnfProfileId();
          VnfInfo vnfInfo = retrieveVnfInfoByProfileId(vnfpId, ctxNsd, ctxNsDf, ctxNsLvl);
          Map<String, NsVirtualLinkConnectivity> cpds =
              getMgmtDataCpds(vnfInfo, expMgmtVlInfo, ctxMgmtVlInfo);
          String vlpId = getVlProfileByDescId(entry.getValue(), ctxNsDf).getVirtualLinkProfileId();
          VlInfo vlInfo = retrieveVlInfo(vlpId, ctxNsd, ctxNsDf, ctxNsLvl);
          addConnectVnfToVl(vnfInfo, cpds, vlInfo, expMgmtVlInfo, expNsd, expNsDf, expNsLvl);
        }
      }
    } catch (NotExistingEntityException | VnfNotFoundInLvlMapping | VlNotFoundInLvlMapping e) {
      log.error(e.getMessage());
      throw new InvalidNsdException("Nsd " + ctxNsd.getNsdIdentifier(), e);
    }
  }
}
