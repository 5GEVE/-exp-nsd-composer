package it.cnit.blueprint.expbuilder.nsd.compose;

import it.cnit.blueprint.expbuilder.nsd.graph.NsdGraphService;
import it.cnit.blueprint.expbuilder.rest.InvalidNsd;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.NotExistingEntityException;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsDf;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsLevel;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsVirtualLinkConnectivity;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Nsd;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.VirtualLinkToLevelMapping;
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

  @Override
  public void composeWithStrategy(
      VlInfo ranVlInfo, VlInfo vsbMgmtVlInfo, VlInfo ctxMgmtVlInfo,
      Nsd vsbNsd, NsDf vsbNsDf, NsLevel vsbNsLvl,
      Nsd ctxNsd, NsDf ctxNsDf, NsLevel ctxNsLvl)
      throws InvalidNsd {
    log.info("Compose with CONNECT.");
    // Retrieve ctx VNFs
    VnfInfo srcVnfInfo;
    VnfInfo dstVnfInfo;
    // Assumption: src is 0 and dst is 1
    try {
      String srcVnfpId = ctxNsLvl.getVnfToLevelMapping().get(0).getVnfProfileId();
      srcVnfInfo = retrieveVnfInfoByProfileId(srcVnfpId, ctxNsd, ctxNsDf, ctxNsLvl);
      String dstVnfpId = ctxNsLvl.getVnfToLevelMapping().get(1).getVnfProfileId();
      dstVnfInfo = retrieveVnfInfoByProfileId(dstVnfpId, ctxNsd, ctxNsDf, ctxNsLvl);
    } catch (VnfNotFoundInLvlMapping e) {
      log.error(e.getMessage());
      throw new InvalidNsd(e.getMessage());
    }

    // Retrieve CpdId for src VNF
    Map<String, NsVirtualLinkConnectivity> srcCpds;
    try {
      srcCpds = getMgmtDataCpds(srcVnfInfo, vsbMgmtVlInfo, ctxMgmtVlInfo);
    } catch (Exception e) {
      throw new InvalidNsd(e.getMessage());
    }
    // Retrieve CpdId for dst VNF
    Map<String, NsVirtualLinkConnectivity> dstCpds;
    try {
      dstCpds = getMgmtDataCpds(dstVnfInfo, vsbMgmtVlInfo, ctxMgmtVlInfo);
    } catch (Exception e) {
      throw new InvalidNsd(e.getMessage());
    }

    // TODO handle custom VL input
    // Retrieve src VL
    VlInfo srcVlInfo = ranVlInfo;
    // Retrieve dst VL
    VlInfo dstVlInfo;
    Optional<VirtualLinkToLevelMapping> optDstLvlMap = vsbNsLvl.getVirtualLinkToLevelMapping()
        .stream()
        .filter(m -> !m.getVirtualLinkProfileId()
            .equals(vsbMgmtVlInfo.getVlProfile().getVirtualLinkProfileId())
            && !m.getVirtualLinkProfileId()
            .equals(ranVlInfo.getVlProfile().getVirtualLinkProfileId()))
        .findFirst();
    try {
      if (optDstLvlMap.isPresent()) {
        dstVlInfo = retrieveVlInfo(optDstLvlMap.get().getVirtualLinkProfileId(),
            vsbNsd, vsbNsDf, vsbNsLvl);
        log.debug("Found non-mgmt VlInfo='{}'.",
            dstVlInfo.getVlProfile().getVirtualLinkProfileId());
      } else {
        throw new InvalidNsd(
            "Can't find a non-mgmt VlInfo in vsbNsd: '" + vsbNsd.getNsdIdentifier() + "'");
      }
    } catch (InvalidNsd | VlNotFoundInLvlMapping e) {
      log.error(e.getMessage());
      throw new InvalidNsd(e.getMessage());
    }

    // Modify vsbNsd
    addVnf(srcVnfInfo, vsbNsd, vsbNsDf, vsbNsLvl);
    log.debug("Added VnfProfile='{}' in service (if not present).",
        srcVnfInfo.getVnfProfile().getVnfProfileId());
    addVnf(dstVnfInfo, vsbNsd, vsbNsDf, vsbNsLvl);
    log.debug("Added VnfProfile='{}' in service (if not present).",
        dstVnfInfo.getVnfProfile().getVnfProfileId());
    try {
      // Connect VNFs to src VL
      connectVnfToVL(srcVnfInfo.getVnfProfile(), srcCpds.get("data0").getCpdId().get(0),
          srcVlInfo.getVlProfile());
      log.debug("Created connection between vnfProfile='{}' and vlProfile='{}'",
          srcVnfInfo.getVnfProfile().getVnfProfileId(),
          srcVlInfo.getVlProfile().getVirtualLinkProfileId());
      // Connect VNFs to dst VL
      connectVnfToVL(dstVnfInfo.getVnfProfile(), dstCpds.get("data0").getCpdId().get(0),
          dstVlInfo.getVlProfile());
      log.debug("Created connection between vnfProfile='{}' and vlProfile='{}'",
          dstVnfInfo.getVnfProfile().getVnfProfileId(),
          dstVlInfo.getVlProfile().getVirtualLinkProfileId());
      // Connect VNFs to mgmt VL (if possible)
      if (srcCpds.get("mgmt") != null) {
        connectVnfToVL(srcVnfInfo.getVnfProfile(), srcCpds.get("mgmt").getCpdId().get(0),
            vsbMgmtVlInfo.getVlProfile());
      } else {
        log.warn("Could not find a management Cp for srcVnf. Skip.");
      }
      if (dstCpds.get("mgmt") != null) {
        connectVnfToVL(dstVnfInfo.getVnfProfile(), dstCpds.get("mgmt").getCpdId().get(0),
            vsbMgmtVlInfo.getVlProfile());
      } else {
        log.warn("Could not find a management Cp for dstVnf. Skip.");
      }
      // Cleanup unused cpds
      for (Entry<String, NsVirtualLinkConnectivity> cpd : srcCpds.entrySet()) {
        if (!cpd.getKey().equals("data0") && !cpd.getKey().equals("mgmt")) {
          srcVnfInfo.getVnfProfile().getNsVirtualLinkConnectivity().remove(cpd.getValue());
        }
      }
      for (Entry<String, NsVirtualLinkConnectivity> cpd : dstCpds.entrySet()) {
        if (!cpd.getKey().equals("data0") && !cpd.getKey().equals("mgmt")) {
          dstVnfInfo.getVnfProfile().getNsVirtualLinkConnectivity().remove(cpd.getValue());
        }
      }
    } catch (NotExistingEntityException e) {
      log.error(e.getMessage());
      throw new InvalidNsd(e.getMessage());
    }
  }

}
