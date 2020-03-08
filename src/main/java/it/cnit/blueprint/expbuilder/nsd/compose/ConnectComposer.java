package it.cnit.blueprint.expbuilder.nsd.compose;

import it.cnit.blueprint.expbuilder.nsd.graph.NsdGraphService;
import it.cnit.blueprint.expbuilder.nsd.graph.ProfileVertex;
import it.cnit.blueprint.expbuilder.rest.InvalidNsd;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsDf;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsLevel;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsVirtualLinkDesc;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Nsd;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.jgrapht.Graph;
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
      NsVirtualLinkDesc ranVld, NsVirtualLinkDesc vsbMgmtVld, NsVirtualLinkDesc ctxMgmtVld,
      Nsd vsbNsd, NsDf vsbNsDf, NsLevel vsbNsLvl, Graph<ProfileVertex, String> vsbG,
      Nsd ctxNsd, NsDf ctxNsDf, NsLevel ctxNsLvl, Graph<ProfileVertex, String> ctxG)
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
    addVnf(srcVnfInfo, vsbNsd, vsbNsDf, vsbNsLvl);
    log.debug("Added Vnfd='{}' in service (if not present).", srcVnfInfo.getVfndId());
    addVnf(dstVnfInfo, vsbNsd, vsbNsDf, vsbNsLvl);
    log.debug("Added Vnfd='{}' in service (if not present).", dstVnfInfo.getVfndId());

    // Retrieve src VL
    // Retrieve RAN VL information from vsb
    VlInfo srcVlInfo;
    try {
      srcVlInfo = retrieveVlInfo(ranVld, vsbNsDf, vsbNsLvl);
      log.debug("Found VlInfo for ranVld='{}' in vsbNsd.", ranVld.getVirtualLinkDescId());
    } catch (InvalidNsd | VlNotFoundInLvlMapping e) {
      log.error(e.getMessage());
      throw new InvalidNsd(e.getMessage());
    }
    // Retrieve dst VL
    VlInfo dstVlInfo;
    Optional<NsVirtualLinkDesc> optdstVld = vsbNsd.getVirtualLinkDesc().stream()
        .filter(vld -> vld.getVirtualLinkDescId().equals(vsbMgmtVld.getVirtualLinkDescId()))
        .findFirst();
    try {
      if (optdstVld.isPresent()) {
        dstVlInfo = retrieveVlInfo(optdstVld.get(), vsbNsDf, vsbNsLvl);
        log.debug("Found non-mgmt VlInfo.");
      } else {
        throw new InvalidNsd(
            "Can't find a non-mgmt VlInfo in vsbNsd: '" + vsbNsd.getNsdIdentifier() + "'");
      }
    } catch (InvalidNsd | VlNotFoundInLvlMapping e) {
      log.error(e.getMessage());
      throw new InvalidNsd(e.getMessage());
    }

    // Connect src VNF
    connectVnfToVL(srcVnfInfo.getVnfProfile(), "???", srcVlInfo.getVlProfile());

    // Connect dst VNF
    connectVnfToVL(dstVnfInfo.getVnfProfile(), "???", dstVlInfo.getVlProfile());

  }
}
