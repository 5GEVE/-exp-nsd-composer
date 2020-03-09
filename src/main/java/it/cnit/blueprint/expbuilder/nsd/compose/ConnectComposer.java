package it.cnit.blueprint.expbuilder.nsd.compose;

import it.cnit.blueprint.expbuilder.nsd.graph.NsdGraphService;
import it.cnit.blueprint.expbuilder.rest.InvalidNsd;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.NotExistingEntityException;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsDf;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsLevel;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsVirtualLinkConnectivity;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsVirtualLinkDesc;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Nsd;
import java.util.Map;
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
    // Retrieve ctx VNFs
    VnfInfo srcVnfInfo;
    VnfInfo dstVnfInfo;
    // Assumption: src is 0 and dst is 1
    try {
      String srcVnfdId = ctxNsd.getVnfdId().get(0);
      srcVnfInfo = retrieveVnfInfoByDescId(srcVnfdId, ctxNsd, ctxNsDf, ctxNsLvl);
      String dstVnfdId = ctxNsd.getVnfdId().get(1);
      dstVnfInfo = retrieveVnfInfoByDescId(dstVnfdId, ctxNsd, ctxNsDf, ctxNsLvl);
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
      dstCpds = getMgmtDataCpds(srcVnfInfo, vsbMgmtVlInfo, ctxMgmtVlInfo);
    } catch (Exception e) {
      throw new InvalidNsd(e.getMessage());
    }

    // TODO handle custom VL input
    // Retrieve src VL
    VlInfo srcVlInfo = ranVlInfo;
    // Retrieve dst VL
    VlInfo dstVlInfo;
    Optional<NsVirtualLinkDesc> optDstVld = vsbNsd.getVirtualLinkDesc().stream()
        .filter(vld -> !vld.getVirtualLinkDescId()
            .equals(vsbMgmtVlInfo.getVlDescriptor().getVirtualLinkDescId()))
        .findFirst();
    try {
      if (optDstVld.isPresent()) {
        dstVlInfo = retrieveVlInfo(optDstVld.get(), vsbNsDf, vsbNsLvl);
        log.debug("Found non-mgmt VlInfo.");
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
    log.debug("Added Vnfd='{}' in service (if not present).", srcVnfInfo.getVfndId());
    addVnf(dstVnfInfo, vsbNsd, vsbNsDf, vsbNsLvl);
    log.debug("Added Vnfd='{}' in service (if not present).", dstVnfInfo.getVfndId());
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
    } catch (NotExistingEntityException e) {
      log.error(e.getMessage());
      throw new InvalidNsd(e.getMessage());
    }
  }

}
