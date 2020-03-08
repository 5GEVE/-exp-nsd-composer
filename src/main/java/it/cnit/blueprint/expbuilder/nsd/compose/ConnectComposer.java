package it.cnit.blueprint.expbuilder.nsd.compose;

import it.cnit.blueprint.expbuilder.nsd.graph.NsdGraphService;
import it.cnit.blueprint.expbuilder.nsd.graph.ProfileVertex;
import it.cnit.blueprint.expbuilder.rest.InvalidNsd;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsDf;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsLevel;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsVirtualLinkDesc;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Nsd;
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

  }
}
