package it.cnit.blueprint.expbuilder.nsd.compose;

import it.cnit.blueprint.expbuilder.nsd.graph.NsdGraphService;
import it.cnit.blueprint.expbuilder.nsd.graph.ProfileVertex;
import it.cnit.blueprint.expbuilder.nsd.graph.ProfileVertexNotFoundException;
import it.cnit.blueprint.expbuilder.nsd.graph.VirtualLinkProfileVertex;
import it.cnit.blueprint.expbuilder.nsd.graph.VnfProfileVertex;
import it.cnit.blueprint.expbuilder.rest.InvalidNsd;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.NotExistingEntityException;
import it.nextworks.nfvmano.libs.ifa.descriptors.common.elements.VirtualLinkProfile;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsDf;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsLevel;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsVirtualLinkDesc;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Nsd;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.VnfProfile;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
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
      NsVirtualLinkDesc ranVld, NsVirtualLinkDesc vsbMgmtVld, NsVirtualLinkDesc ctxMgmtVld,
      Nsd vsbNsd, NsDf vsbNsDf, NsLevel vsbNsLvl, Graph<ProfileVertex, String> vsbG,
      Nsd ctxNsd, NsDf ctxNsDf, NsLevel ctxNsLvl, Graph<ProfileVertex, String> ctxG
  ) throws InvalidNsd {
    // Retrieve ctx VNF
    String ctxVnfdId = ctxNsd.getVnfdId().get(0);
    VnfProfile ctxVnfProfile;
    try {
      ctxVnfProfile = getVnfProfileByDescId(ctxVnfdId, ctxNsDf);
      log.debug("Found vnfProfile='{}' in context.", ctxVnfProfile.getVnfProfileId());
    } catch (NotExistingEntityException e) {
      log.error(e.getMessage());
      throw new InvalidNsd(e.getMessage());
    }
    VnfInfo ctxVnfInfo;
    try {
      ctxVnfInfo = retrieveVnfInfo(ctxVnfProfile.getVnfProfileId(),
          ctxNsd, ctxNsDf, ctxNsLvl);
      log.debug("Found VnfInfo for vnfProfile='{}' in context.", ctxVnfProfile.getVnfProfileId());
    } catch (VnfNotFoundInLvlMapping e) {
      log.error(e.getMessage());
      throw new InvalidNsd(e.getMessage());
    }
    addVnf(ctxVnfInfo, vsbNsd, vsbNsDf, vsbNsLvl);
    log.debug("Added Vnfd='{}' in service (if not present).", ctxVnfdId);

    // Retrieve non-management VLs from ctx
    ProfileVertex ctxVnfPVertex;
    try {
      ctxVnfPVertex = nsdGraphService.getVertexById(ctxG, ctxVnfProfile.getVnfProfileId());
      log.debug("ctxVnfPVertex: {}", ctxVnfPVertex.toString());
    } catch (ProfileVertexNotFoundException e) {
      log.error(e.getMessage());
      throw new InvalidNsd(e.getMessage());
    }
    List<ProfileVertex> ctxVnfNeigh = Graphs.neighborListOf(ctxG, ctxVnfPVertex);
    log.debug("ctxVnfPVertex neighbors: {}", ctxVnfNeigh.toString());
    String ctxMgmtCpdId = null;
    LinkedHashMap<String, VlInfo> ctxNonMgmtVls = new LinkedHashMap<>();
    try {
      for (ProfileVertex vlpV : ctxVnfNeigh) {
        if (vlpV instanceof VirtualLinkProfileVertex) {
          if (((VirtualLinkProfileVertex) vlpV).getVlProfile().getVirtualLinkDescId()
              .equals(ctxMgmtVld.getVirtualLinkDescId())) {
            ctxMgmtCpdId = ctxG.getEdge(ctxVnfPVertex, vlpV);
          } else {
            VirtualLinkProfile vlProfile = ((VirtualLinkProfileVertex) vlpV).getVlProfile();
            ctxNonMgmtVls.put(ctxG.getEdge(ctxVnfPVertex, vlpV),
                retrieveVlInfo(vlProfile.getVirtualLinkProfileId(), ctxNsd, ctxNsDf, ctxNsLvl));
          }
        }
      }
      if (ctxNonMgmtVls.isEmpty()) {
        throw new InvalidNsd("Can't find a non-management VL in Ctx.");
      }
    } catch (InvalidNsd | VlNotFoundInLvlMapping e) {
      log.error(e.getMessage());
      throw new InvalidNsd(e.getMessage());
    }
    log.debug("ctxNonMgmtVls: {}", ctxNonMgmtVls.toString());
    Iterator<Entry<String, VlInfo>> ctxNonMgmtVLIter = ctxNonMgmtVls.entrySet().iterator();
    Entry<String, VlInfo> ctxPrimaryConn = ctxNonMgmtVLIter.next();
    addVirtualLink(ctxPrimaryConn.getValue(), vsbNsd, vsbNsDf, vsbNsLvl);
    log.debug("Added VirtualLinkDescriptor='{}' in service (if not present).",
        ctxPrimaryConn.getValue().getVlDescriptor().getVirtualLinkDescId());

    // Retrieve RAN VL information from vsb
    VlInfo ranVlInfo;
    try {
      ranVlInfo = retrieveVlInfo(ranVld, vsbNsDf, vsbNsLvl);
      log.debug("Found VlInfo for ranVld='{}' in context.", ranVld.getVirtualLinkDescId());
    } catch (InvalidNsd | VlNotFoundInLvlMapping e) {
      log.error(e.getMessage());
      throw new InvalidNsd(e.getMessage());
    }

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
      connectVnfToVL(ranVnfVertex.getVnfProfile(), ranVnfCpd,
          ctxPrimaryConn.getValue().getVlProfile());
      log.debug("Created connection between vnfProfile='{}' and vlProfile='{}'",
          ranVnfVertex.getVnfProfile(), ctxPrimaryConn.getValue().getVlProfile());
    } catch (NotExistingEntityException e) {
      log.error(e.getMessage());
      throw new InvalidNsd(e.getMessage());
    }

    // Connect ctxVnf with RAN VL
    Entry<String, VlInfo> ctxSecondaryConn = ctxNonMgmtVLIter.next();
    try {
      connectVnfToVL(ctxVnfInfo.getVnfProfile(), ctxSecondaryConn.getKey(),
          ranVlInfo.getVlProfile());
      log.debug("Created connection between vnfProfile='{}' and vlProfile='{}'",
          ctxVnfInfo.getVnfProfile(), ranVlInfo.getVlProfile());
    } catch (NotExistingEntityException e) {
      log.error(e.getMessage());
      throw new InvalidNsd(e.getMessage());
    }

    // Connect ctxVnf to vsbNsd mgmt VL
    VlInfo vsbMgmtVlInfo;
    try {
      vsbMgmtVlInfo = retrieveVlInfo(vsbMgmtVld, vsbNsDf, vsbNsLvl);
      if (ctxMgmtCpdId != null) {
        try {
          connectVnfToVL(ctxVnfInfo.getVnfProfile(), ctxMgmtCpdId,
              vsbMgmtVlInfo.getVlProfile());
        } catch (NotExistingEntityException e) {
          log.error(e.getMessage());
          throw new InvalidNsd(e.getMessage());
        }
      } else {
        log.warn("Could not find a management Cp for ctxVnf. Skip.");
      }
    } catch (VlNotFoundInLvlMapping e) {
      log.warn(e.getMessage() + " Skip.");
    }

  }
}
