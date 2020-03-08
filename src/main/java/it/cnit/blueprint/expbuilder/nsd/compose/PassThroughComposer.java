package it.cnit.blueprint.expbuilder.nsd.compose;

import it.cnit.blueprint.expbuilder.nsd.graph.NsdGraphService;
import it.cnit.blueprint.expbuilder.nsd.graph.ProfileVertex;
import it.cnit.blueprint.expbuilder.nsd.graph.ProfileVertexNotFoundException;
import it.cnit.blueprint.expbuilder.nsd.graph.VnfProfileVertex;
import it.cnit.blueprint.expbuilder.rest.InvalidNsd;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.NotExistingEntityException;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsDf;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsLevel;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsVirtualLinkConnectivity;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Nsd;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
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
      VlInfo ranVlInfo, VlInfo vsbMgmtVlInfo, VlInfo ctxMgmtVlInfo,
      Nsd vsbNsd, NsDf vsbNsDf, NsLevel vsbNsLvl, Graph<ProfileVertex, String> vsbG,
      Nsd ctxNsd, NsDf ctxNsDf, NsLevel ctxNsLvl, Graph<ProfileVertex, String> ctxG
  ) throws InvalidNsd {
    // Retrieve ctx VNF
    String ctxVnfdId = ctxNsd.getVnfdId().get(0);
    VnfInfo ctxVnfInfo;
    try {
      ctxVnfInfo = retrieveVnfInfoByDescId(ctxVnfdId, ctxNsd, ctxNsDf, ctxNsLvl);
      log.debug("Found VnfInfo for vnfdId='{}' in context.", ctxVnfdId);
    } catch (VnfNotFoundInLvlMapping e) {
      log.error(e.getMessage());
      throw new InvalidNsd(e.getMessage());
    }
    addVnf(ctxVnfInfo, vsbNsd, vsbNsDf, vsbNsLvl);
    log.debug("Added Vnfd='{}' in service (if not present).", ctxVnfdId);

    // Retrieve non-management VLs from ctx
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
    addVirtualLink(ctxNonMgmtVl, vsbNsd, vsbNsDf, vsbNsLvl);
    log.debug("Added VirtualLinkDescriptor='{}' in service (if not present).",
        ctxNonMgmtVl.getVlDescriptor().getVirtualLinkDescId());

    // Retrieve RAN closest VNF information from vsb
    // Assumption: select the first VNF attached to the RAN VL
    ProfileVertex ranVlVertex;
    try {
      ranVlVertex = nsdGraphService
          .getVertexById(vsbG, ranVlInfo.getVlProfile().getVirtualLinkProfileId());
      log.debug("ranVlVertex: {}", ranVlVertex.toString());
    } catch (ProfileVertexNotFoundException e) {
      log.error(e.getMessage());
      throw new InvalidNsd(e.getMessage());
    }
    List<ProfileVertex> ranVlNeigh = Graphs.neighborListOf(vsbG, ranVlVertex);
    log.debug("ranVlVertex neighbors: {}", ranVlNeigh.toString());
    VnfProfileVertex ranVnfVertex;
    Optional<ProfileVertex> optV = ranVlNeigh.stream().filter(v -> v instanceof VnfProfileVertex)
        .findFirst();
    if (optV.isPresent()) {
      ranVnfVertex = (VnfProfileVertex) optV.get();
      log.debug("ranVnfVertex: {}", ranVnfVertex.toString());
    } else {
      throw new InvalidNsd(
          "No neighbor of type VnfProfileVertex found for '" + ranVlVertex.getVertexId() + "'.");
    }
    String ranVnfCpd = vsbG.getEdge(ranVlVertex, ranVnfVertex);
    log.debug("ranVnfCpd: {}", ranVnfCpd);

    // Connect ranVnf to the new VL coming from ctx
    try {
      connectVnfToVL(ranVnfVertex.getVnfProfile(), ranVnfCpd, ctxNonMgmtVl.getVlProfile());
      log.debug("Created connection between vnfProfile='{}' and vlProfile='{}'",
          ranVnfVertex.getVnfProfile().getVnfProfileId(),
          ctxNonMgmtVl.getVlProfile().getVirtualLinkProfileId());
    } catch (NotExistingEntityException e) {
      log.error(e.getMessage());
      throw new InvalidNsd(e.getMessage());
    }

    try {
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
        log.debug("qui");
      } else {
        log.warn("Could not find a management Cp for ctxVnf. Skip.");
      }

    } catch (NotExistingEntityException e) {
      log.error(e.getMessage());
      throw new InvalidNsd(e.getMessage());
    }

  }
}
