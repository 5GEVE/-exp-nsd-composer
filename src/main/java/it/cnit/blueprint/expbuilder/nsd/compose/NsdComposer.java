package it.cnit.blueprint.expbuilder.nsd.compose;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import it.cnit.blueprint.expbuilder.nsd.graph.NsdGraphService;
import it.cnit.blueprint.expbuilder.nsd.graph.ProfileVertex;
import it.cnit.blueprint.expbuilder.nsd.graph.ProfileVertexNotFoundException;
import it.cnit.blueprint.expbuilder.nsd.graph.VirtualLinkProfileVertex;
import it.cnit.blueprint.expbuilder.nsd.graph.VnfProfileVertex;
import it.cnit.blueprint.expbuilder.rest.InvalidCtxComposeInfo;
import it.cnit.blueprint.expbuilder.rest.InvalidNsd;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.MalformattedElementException;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.NotExistingEntityException;
import it.nextworks.nfvmano.libs.ifa.descriptors.common.elements.VirtualLinkProfile;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsDf;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsLevel;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsVirtualLinkConnectivity;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsVirtualLinkDesc;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Nsd;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Sapd;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.VirtualLinkToLevelMapping;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.VnfProfile;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.VnfToLevelMapping;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

@Service
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
@Slf4j
@AllArgsConstructor
public class NsdComposer {

  private NsdGraphService nsdGraphService;
  static final ObjectMapper OBJECT_MAPPER = new ObjectMapper(new YAMLFactory());

  private VnfProfile getVnfProfileById(String vnfProfileId, NsDf nsDf)
      throws NotExistingEntityException {
    VnfProfile vnfProfile;
    try {
      vnfProfile = nsDf.getVnfProfile(vnfProfileId);
    } catch (NotExistingEntityException e) {
      String m = MessageFormatter
          .format("VnfProfile='{}' not found in nsDf='{}'", vnfProfileId, nsDf.getNsDfId())
          .getMessage();
      throw new NotExistingEntityException(m);
    }
    return vnfProfile;
  }

  private VnfProfile getVnfProfileByDescId(String vnfdId, NsDf nsDf)
      throws NotExistingEntityException {
    VnfProfile vnfProfile;
    Optional<VnfProfile> optVnfP = nsDf.getVnfProfile().stream()
        .filter(p -> p.getVnfdId().equals(vnfdId)).findFirst();
    if (optVnfP.isPresent()) {
      vnfProfile = optVnfP.get();
    } else {
      String m = MessageFormatter
          .format("No VnfProfile with vnfdId='{}' found in nsDf='{}'", vnfdId, nsDf.getNsDfId())
          .getMessage();
      throw new NotExistingEntityException(m);
    }
    return vnfProfile;
  }

  private VirtualLinkProfile getVlProfile(String vlProfileId, NsDf nsDf)
      throws NotExistingEntityException {
    VirtualLinkProfile vlProfile;
    try {
      vlProfile = nsDf.getVirtualLinkProfile(vlProfileId);
    } catch (NotExistingEntityException e) {
      String m = MessageFormatter
          .format("vlProfileId='{}' not found in nsDf='{}'.", vlProfileId, nsDf.getNsDfId())
          .getMessage();
      throw new NotExistingEntityException(m);
    }
    return vlProfile;
  }

  private VirtualLinkProfile getVlProfile(NsVirtualLinkDesc vld, NsDf nsDf)
      throws NotExistingEntityException {
    VirtualLinkProfile resultVlp;
    Optional<VirtualLinkProfile> optVlp = nsDf.getVirtualLinkProfile().stream()
        .filter(vlp -> vlp.getVirtualLinkDescId().equals(vld.getVirtualLinkDescId())).findFirst();
    if (optVlp.isPresent()) {
      resultVlp = optVlp.get();
    } else {
      String m = MessageFormatter
          .format("vlProfile with VirtualLinkDescId='{}' not found in nsDf='{}'.",
              vld.getVirtualLinkDescId(), nsDf.getNsDfId())
          .getMessage();
      throw new NotExistingEntityException(m);
    }
    return resultVlp;
  }

  private NsVirtualLinkConnectivity getVlConnectivity(String cpdId, VnfProfile vnfProfile)
      throws NotExistingEntityException {
    NsVirtualLinkConnectivity nsVlC;
    Optional<NsVirtualLinkConnectivity> optVlC = vnfProfile.getNsVirtualLinkConnectivity()
        .stream().filter(vlc -> vlc.getCpdId().get(0).equals(cpdId)).findFirst();
    if (optVlC.isPresent()) {
      nsVlC = optVlC.get();
    } else {
      String m = MessageFormatter
          .format("NsVirtualLinkConnectivity for cpdId='{}' not found in vnfProfile='{}'",
              cpdId, vnfProfile.getVnfProfileId())
          .getMessage();
      throw new NotExistingEntityException(m);
    }
    return nsVlC;
  }

  private VnfToLevelMapping getVnfLvlMapping(String vnfProfileId, NsLevel nsLvl)
      throws NotExistingEntityException {
    VnfToLevelMapping vnfLvlMap;
    Optional<VnfToLevelMapping> optVnfLvlMap = nsLvl.getVnfToLevelMapping().stream()
        .filter(m -> m.getVnfProfileId().equals(vnfProfileId)).findFirst();
    if (optVnfLvlMap.isPresent()) {
      vnfLvlMap = optVnfLvlMap.get();
    } else {
      String m = MessageFormatter
          .format("vnfProfileId='{}' not found in nsLvl='{}'.", vnfProfileId, nsLvl.getNsLevelId())
          .getMessage();
      throw new NotExistingEntityException(m);
    }
    return vnfLvlMap;
  }

  private String getVnfdId(String vnfdId, Nsd nsd) throws NotExistingEntityException {
    String vnfdIdFound;
    Optional<String> optVnfdId = nsd.getVnfdId().stream()
        .filter(id -> id.equals(vnfdId)).findFirst();
    if (optVnfdId.isPresent()) {
      vnfdIdFound = optVnfdId.get();
    } else {
      String m = MessageFormatter
          .format("vnfdId='{}' not found in nsd='{}'.", vnfdId, nsd.getNsdIdentifier())
          .getMessage();
      throw new NotExistingEntityException(m);
    }
    return vnfdIdFound;

  }

  private NsVirtualLinkDesc getVlDescriptor(String vlDescId, Nsd nsd)
      throws NotExistingEntityException {
    NsVirtualLinkDesc vlDesc;
    Optional<NsVirtualLinkDesc> optVlDesc = nsd.getVirtualLinkDesc().stream()
        .filter(nsdVlDesc -> nsdVlDesc.getVirtualLinkDescId().equals(vlDescId)).findFirst();
    if (optVlDesc.isPresent()) {
      vlDesc = optVlDesc.get();
    } else {
      String m = MessageFormatter
          .format("vlDescId='{}' not found in nsd='{}'.", vlDescId, nsd.getNsdIdentifier())
          .getMessage();
      throw new NotExistingEntityException(m);
    }
    return vlDesc;
  }

  private VirtualLinkToLevelMapping getVlLvlMapping(String vlProfileId, NsLevel nsLvl)
      throws NotExistingEntityException {
    VirtualLinkToLevelMapping vlLvlMap;
    Optional<VirtualLinkToLevelMapping> optVlLvlMap = nsLvl.getVirtualLinkToLevelMapping().stream()
        .filter(m -> m.getVirtualLinkProfileId().equals(vlProfileId)).findFirst();
    if (optVlLvlMap.isPresent()) {
      vlLvlMap = optVlLvlMap.get();
    } else {
      String m = MessageFormatter
          .format("vlProfileId='{}' not found in nsLvl='{}'.", vlProfileId, nsLvl.getNsLevelId())
          .getMessage();
      throw new NotExistingEntityException(m);
    }
    return vlLvlMap;
  }

  private void addVnf(VnfWrapper vnfWrapper, Nsd nsd, NsDf nsDf, NsLevel nsLevel) {
    String vnfdId = vnfWrapper.getVfndId();
    if (nsd.getVnfdId().stream().noneMatch(id -> id.equals(vnfdId))) {
      nsd.getVnfdId().add(vnfdId);
    }
    VnfProfile vnfProfile = vnfWrapper.getVnfProfile();
    if (nsDf.getVnfProfile().stream()
        .noneMatch(vp -> vp.getVnfProfileId().equals(vnfProfile.getVnfProfileId()))) {
      nsDf.getVnfProfile().add(vnfProfile);
    }
    VnfToLevelMapping vnfLvlMap = vnfWrapper.getVnfToLevelMapping();
    if (nsLevel.getVnfToLevelMapping().stream()
        .noneMatch(lm -> lm.getVnfProfileId().equals(vnfProfile.getVnfProfileId()))) {
      nsLevel.getVnfToLevelMapping().add(vnfLvlMap);
    }
  }

  private void addVirtualLink(VlWrapper vlWrapper, Nsd nsd, NsDf nsDf, NsLevel nsLevel) {
    NsVirtualLinkDesc vlDesc = vlWrapper.getVlDescriptor();
    if (nsd.getVirtualLinkDesc().stream()
        .noneMatch(nsdVld -> nsdVld.getVirtualLinkDescId().equals(vlDesc.getVirtualLinkDescId()))) {
      nsd.getVirtualLinkDesc().add(vlDesc);
    }
    VirtualLinkProfile vlProfile = vlWrapper.getVlProfile();
    if (nsDf.getVirtualLinkProfile().stream().noneMatch(
        nsdfVlP -> nsdfVlP.getVirtualLinkProfileId().equals(vlProfile.getVirtualLinkProfileId()))) {
      nsDf.getVirtualLinkProfile().add(vlProfile);
    }
    VirtualLinkToLevelMapping vlMap = vlWrapper.getVlToLevelMapping();
    if (nsLevel.getVirtualLinkToLevelMapping().stream().noneMatch(
        nslevelMap -> nslevelMap.getVirtualLinkProfileId()
            .equals(vlMap.getVirtualLinkProfileId()))) {
      nsLevel.getVirtualLinkToLevelMapping().add(vlMap);
    }
  }

  private VnfWrapper retrieveVnfInfo(String vnfProfileId, Nsd nsd, NsDf nsDf, NsLevel nsLevel)
      throws InvalidNsd, VnfNotFoundInLvlMapping {
    VnfToLevelMapping vnfLvlMap;
    try {
      vnfLvlMap = getVnfLvlMapping(vnfProfileId, nsLevel);
    } catch (NotExistingEntityException e) {
      throw new VnfNotFoundInLvlMapping(e.getMessage());
    }
    VnfProfile vnfProfile;
    try {
      vnfProfile = getVnfProfileById(vnfProfileId, nsDf);
    } catch (NotExistingEntityException e) {
      throw new InvalidNsd(e.getMessage());
    }
    String vnfdId;
    try {
      vnfdId = getVnfdId(vnfProfile.getVnfdId(), nsd);
    } catch (NotExistingEntityException e) {
      throw new InvalidNsd(e.getMessage());
    }
    return new VnfWrapper(vnfdId, vnfProfile, vnfLvlMap);
  }

  private VlWrapper retrieveVlInfo(String vlProfileId, Nsd nsd, NsDf nsDf, NsLevel nsLevel)
      throws InvalidNsd, VlNotFoundInLvlMapping {
    VirtualLinkToLevelMapping vlMap;
    try {
      vlMap = getVlLvlMapping(vlProfileId, nsLevel);
    } catch (NotExistingEntityException e) {
      throw new VlNotFoundInLvlMapping(e.getMessage());
    }
    VirtualLinkProfile vlProfile;
    try {
      vlProfile = getVlProfile(vlProfileId, nsDf);
    } catch (NotExistingEntityException e) {
      throw new InvalidNsd(e.getMessage());
    }
    NsVirtualLinkDesc vlDesc;
    try {
      vlDesc = getVlDescriptor(vlProfile.getVirtualLinkDescId(), nsd);
    } catch (NotExistingEntityException e) {
      throw new InvalidNsd(e.getMessage());
    }
    return new VlWrapper(vlMap, vlProfile, vlDesc);
  }

  private VlWrapper retrieveVlInfo(NsVirtualLinkDesc vld, NsDf nsDf, NsLevel nsLevel)
      throws InvalidNsd, VlNotFoundInLvlMapping {
    VirtualLinkProfile vlProfile;
    try {
      vlProfile = getVlProfile(vld, nsDf);
    } catch (NotExistingEntityException e) {
      throw new InvalidNsd(e.getMessage());
    }
    VirtualLinkToLevelMapping vlMap;
    try {
      vlMap = getVlLvlMapping(vlProfile.getVirtualLinkProfileId(), nsLevel);
    } catch (NotExistingEntityException e) {
      throw new VlNotFoundInLvlMapping(e.getMessage());
    }
    return new VlWrapper(vlMap, vlProfile, vld);
  }

  private NsVirtualLinkDesc findSapVld(Sapd sapd, Nsd nsd) throws InvalidNsd {
    NsVirtualLinkDesc vld;
    Optional<NsVirtualLinkDesc> optVld = nsd.getVirtualLinkDesc().stream()
        .filter(v -> v.getVirtualLinkDescId().equals(sapd.getNsVirtualLinkDescId())).findFirst();
    if (optVld.isPresent()) {
      vld = optVld.get();
    } else {
      String m = MessageFormatter
          .format("Vld with id='{}' not found.", sapd.getNsVirtualLinkDescId())
          .getMessage();
      throw new InvalidNsd(m);
    }
    return vld;
  }

  private void connectVnfToVL(VnfProfile vnfp, String cpdId, VirtualLinkProfile vlp)
      throws NotExistingEntityException {
    Optional<NsVirtualLinkConnectivity> optVlConn = vnfp.getNsVirtualLinkConnectivity().stream()
        .filter(vlConn -> vlConn.getCpdId().get(0).equals(cpdId)).findFirst();
    if (optVlConn.isPresent()) {
      optVlConn.get().setVirtualLinkProfileId(vlp.getVirtualLinkProfileId());
    } else {
      String m = MessageFormatter
          .format("cpdId='{}' not found in vnfProfile='{}'.", cpdId, vnfp.getVnfProfileId())
          .getMessage();
      throw new NotExistingEntityException(m);
    }

  }

//  @SuppressWarnings("DuplicatedCode")
//  @SneakyThrows(JsonProcessingException.class)
//  public void compose(Nsd vsNsd, CtxComposeInfo[] ctxComposeInfos)
//      throws InvalidCtxComposeInfo, InvalidNsd {
//    ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
//    for (CtxComposeInfo ctxComposeInfo : ctxComposeInfos) {
//      Nsd ctxNsd = ctxComposeInfo.getCtxBReq().getNsds().get(0);
//      // We assume only one NsDf for the context
//      NsDf ctxNsDf = ctxNsd.getNsDf().get(0);
//      // We assume only one NsLevel for the context
//      NsLevel ctxNsLvl = ctxNsDf.getNsInstantiationLevel().get(0);
//      log.info("Composing '{}' with <{}, {}, {}>.",
//          vsNsd.getNsdIdentifier(), ctxNsd.getNsdIdentifier(), ctxNsDf.getNsDfId(),
//          ctxNsLvl.getNsLevelId());
//      log.debug("Nsd BEFORE composition:\n{}", objectMapper.writeValueAsString(vsNsd));
//      vsNsd.setNsdName(vsNsd.getNsdName() + " + " + ctxNsd.getNsdName());
//      for (NsDf vsNsDf : vsNsd.getNsDf()) {
//        for (NsLevel vsNsLvl : vsNsDf.getNsInstantiationLevel()) {
//          log.info("Start composition for nsDf='{}' and nsLvl='{}'",
//              vsNsDf.getNsDfId(), vsNsLvl.getNsLevelId());
//          Graph<ProfileVertex, String> g = nsdGraphService
//              .buildGraph(vsNsd.getSapd(), vsNsDf, vsNsLvl);
//          log.debug("Graph BEFORE composition :\n{}", nsdGraphService.export(g));
//          for (VnfConnection ctxC : ctxComposeInfo.getCtxConnections()) {
//            log.info("ctxConnection: {}", ctxC);
//            VnfWrapper vnfWrapper;
//            VlWrapper vlWrapper;
//
//            // Retrieve the VNF from context Nsd
//            try {
//              vnfWrapper = retrieveVnfInfo(ctxC.getVnfProfileId(), ctxC.getCpdId(),
//                  ctxNsd, ctxNsDf, ctxNsLvl);
//              log.debug("Found vnfProfile='{}' in context.", ctxC.getVnfProfileId());
//            } catch (VnfNotFoundInLvlMapping e) {
//              log.warn(e.getMessage() + " Skip.");
//              continue;
//            } catch (InvalidNsd e) {
//              log.error(e.getMessage());
//              throw e;
//            }
//
//            // Retrieve the VirtualLink from context Nsd (and add it)
//            try {
//              vlWrapper = retrieveVlInfo(ctxC.getVlProfileId(), ctxNsd, ctxNsDf, ctxNsLvl);
//              log.debug("Found vlProfile='{}' in context.", ctxC.getVlProfileId());
//              addVirtualLink(vsNsd, vsNsDf, vsNsLvl, vlWrapper);
//              log.debug("Added vlProfile='{}' in service (if not present).", ctxC.getVlProfileId());
//            } catch (VlNotFoundInLvlMapping e) {
//              log.debug(e.getMessage() + " Trying in service.");
//              // Retrieve the VirtualLink from vertical Nsd
//              try {
//                vlWrapper = retrieveVlInfo(ctxC.getVlProfileId(), vsNsd, vsNsDf, vsNsLvl);
//                log.debug("Found vlProfile='{}' in vertical service.", ctxC.getVlProfileId());
//              } catch (VlNotFoundInLvlMapping vlNotFoundInLvlMapping) {
//                String m = MessageFormatter
//                    .format("vlProfile='{}' not found neither in context or vertical service.",
//                        ctxC.getVlProfileId()).getMessage();
//                log.error(m, e);
//                throw new InvalidCtxComposeInfo(m);
//              }
//            } catch (InvalidNsd e) {
//              log.error(e.getMessage());
//              throw e;
//            }
//
//            // Create connection between Vnf and VL
//            vnfWrapper.getVlConnectivity()
//                .setVirtualLinkProfileId(vlWrapper.getVlProfile().getVirtualLinkProfileId());
//            log.debug("Created connection between vnfProfile='{}' and vlProfile='{}'",
//                ctxC.getVnfProfileId(), ctxC.getVlProfileId());
//            addVnf(vsNsd, vsNsDf, vsNsLvl, vnfWrapper);
//            log.debug("Added vnfProfile='{}' in service (if not present).", ctxC.getVnfProfileId());
//          }
//          for (VnfConnection vsC : ctxComposeInfo.getVsConnections()) {
//            log.info("vsConnection: {}", vsC);
//            VnfWrapper vnfWrapper;
//            VlWrapper vlWrapper;
//
//            // Retrieve the VNF from vertical Nsd
//            try {
//              vnfWrapper = retrieveVnfInfo(vsC.getVnfProfileId(), vsC.getCpdId(),
//                  vsNsd, vsNsDf, vsNsLvl);
//              log.debug("Found vnfProfile='{}' in vertical service.", vsC.getVnfProfileId());
//            } catch (VnfNotFoundInLvlMapping e) {
//              log.warn(e.getMessage() + " Skip.");
//              continue;
//            } catch (InvalidNsd e) {
//              log.error(e.getMessage());
//              throw e;
//            }
//
//            // Retrieve the VirtualLink from context Nsd (and add it)
//            try {
//              vlWrapper = retrieveVlInfo(vsC.getVlProfileId(), ctxNsd, ctxNsDf, ctxNsLvl);
//              log.debug("Found vlProfile='{}' in context.", vsC.getVlProfileId());
//              addVirtualLink(vsNsd, vsNsDf, vsNsLvl, vlWrapper);
//              log.info("Added vlProfile='{}' in service. (if not present).", vsC.getVlProfileId());
//            } catch (VlNotFoundInLvlMapping e) {
//              String m = MessageFormatter
//                  .format("vlProfile='{}' not found in context.", vsC.getVlProfileId())
//                  .getMessage();
//              log.error(m, e);
//              throw new InvalidCtxComposeInfo(m);
//            } catch (InvalidNsd e) {
//              log.error(e.getMessage());
//              throw e;
//            }
//
//            // Create connection between Vnf and VL
//            vnfWrapper.getVlConnectivity()
//                .setVirtualLinkProfileId(vlWrapper.getVlProfile().getVirtualLinkProfileId());
//            log.debug("Created connection between vnfProfile='{}' and vlProfile='{}'",
//                vsC.getVnfProfileId(), vsC.getVlProfileId());
//          }
//          try {
//            vsNsd.isValid();
//          } catch (MalformattedElementException e) {
//            String m = "Nsd looks not valid after composition";
//            log.error(m, e);
//            throw new InvalidNsd(m);
//          }
//          g = nsdGraphService.buildGraph(vsNsd.getSapd(), vsNsDf, vsNsLvl);
//          log.debug("Graph AFTER composition with {}:\n{}",
//              ctxNsd.getNsdIdentifier(), nsdGraphService.export(g));
//          log.info("Completed composition for nsDf='{}' and nsLvl='{}'",
//              vsNsDf.getNsDfId(), vsNsLvl.getNsLevelId());
//        }
//      }
//      vsNsd.setNsdIdentifier(UUID.randomUUID().toString());
//      vsNsd.setNsdInvariantId(UUID.randomUUID().toString());
//      log.debug("Nsd AFTER composition with {}:\n{}",
//          ctxNsd.getNsdIdentifier(), objectMapper.writeValueAsString(vsNsd));
//      log.info("Completed composition of '{}' with <{}, {}, {}>.",
//          vsNsd.getNsdIdentifier(), ctxNsd.getNsdIdentifier(), ctxNsDf.getNsDfId(),
//          ctxNsLvl.getNsLevelId());
//    }
//  }

  @SneakyThrows(JsonProcessingException.class)
  public void composePassThrough(Sapd ranSapd, Nsd vsbNsd, String ctxVnfdId,
      String ctxMgmtVldId, Nsd ctxNsd)
      throws InvalidNsd {
    NsVirtualLinkDesc ranVld;
    try {
      ranVld = findSapVld(ranSapd, vsbNsd);
    } catch (InvalidNsd e) {
      log.error(e.getMessage());
      throw e;
    }

    // We assume only one NsDf for the context
    NsDf ctxNsDf = ctxNsd.getNsDf().get(0);
    // We assume only one NsLevel for the context
    NsLevel ctxNsLvl = ctxNsDf.getNsInstantiationLevel().get(0);
    // We assume only one NsDf for the vertical service
    NsDf vsbNsDf = vsbNsd.getNsDf().get(0);
    Graph<ProfileVertex, String> ctxG = nsdGraphService
        .buildGraph(ctxNsd.getSapd(), ctxNsDf, ctxNsLvl);
    log.debug("ctxG graph:\n{}", nsdGraphService.export(ctxG));

    log.info("Composing '{}' with <{}, {}, {}>.",
        vsbNsd.getNsdIdentifier(), ctxNsd.getNsdIdentifier(), ctxNsDf.getNsDfId(),
        ctxNsLvl.getNsLevelId());
    log.debug("Nsd BEFORE composition:\n{}", OBJECT_MAPPER.writeValueAsString(vsbNsd));

    vsbNsd.setNsdName(vsbNsd.getNsdName() + " + " + ctxNsd.getNsdName());
    for (NsLevel vsbNsLvl : vsbNsDf.getNsInstantiationLevel()) {
      log.info("Start composition for nsDf='{}' and nsLvl='{}'",
          vsbNsDf.getNsDfId(), vsbNsLvl.getNsLevelId());
      Graph<ProfileVertex, String> vsbG = nsdGraphService
          .buildGraph(vsbNsd.getSapd(), vsbNsDf, vsbNsLvl);
      log.debug("vsbG BEFORE composition :\n{}", nsdGraphService.export(vsbG));

      // Retrieve ctx VNF
      VnfProfile ctxVnfProfile;
      try {
        ctxVnfProfile = getVnfProfileByDescId(ctxVnfdId, ctxNsDf);
        log.debug("Found vnfProfile='{}' in context.", ctxVnfProfile.getVnfProfileId());
      } catch (NotExistingEntityException e) {
        log.error(e.getMessage());
        throw new InvalidNsd(e.getMessage());
      }
      VnfWrapper ctxVnfWrapper;
      try {
        ctxVnfWrapper = retrieveVnfInfo(ctxVnfProfile.getVnfProfileId(),
            ctxNsd, ctxNsDf, ctxNsLvl);
        log.debug("Found VnfInfo for vnfProfile='{}' in context.", ctxVnfProfile.getVnfProfileId());
      } catch (VnfNotFoundInLvlMapping e) {
        log.error(e.getMessage());
        throw new InvalidNsd(e.getMessage());
      }
      addVnf(ctxVnfWrapper, vsbNsd, vsbNsDf, vsbNsLvl);
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
      LinkedHashMap<String, VlWrapper> ctxNonMgmtVls = new LinkedHashMap<>();
      try {
        for (ProfileVertex vlpV : ctxVnfNeigh) {
          if (vlpV instanceof VirtualLinkProfileVertex) {
            if (((VirtualLinkProfileVertex) vlpV).getVlProfile().getVirtualLinkDescId()
                .equals(ctxMgmtVldId)) {
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
      Iterator<Entry<String, VlWrapper>> ctxNonMgmtVLIter = ctxNonMgmtVls.entrySet().iterator();
      Entry<String, VlWrapper> ctxPrimaryConn = ctxNonMgmtVLIter.next();
      addVirtualLink(ctxPrimaryConn.getValue(), vsbNsd, vsbNsDf, vsbNsLvl);
      log.debug("Added VirtualLinkDescriptor='{}' in service (if not present).",
          ctxPrimaryConn.getValue().getVlDescriptor().getVirtualLinkDescId());

      // Retrieve RAN VL information from vsb
      VlWrapper ranVlWrapper;
      try {
        ranVlWrapper = retrieveVlInfo(ranVld, vsbNsDf, vsbNsLvl);
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
            .getVertexById(vsbG, ranVlWrapper.getVlProfile().getVirtualLinkProfileId());
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
      Entry<String, VlWrapper> ctxSecondaryConn = ctxNonMgmtVLIter.next();
      try {
        connectVnfToVL(ctxVnfWrapper.getVnfProfile(), ctxSecondaryConn.getKey(),
            ranVlWrapper.getVlProfile());
        log.debug("Created connection between vnfProfile='{}' and vlProfile='{}'",
            ctxVnfWrapper.getVnfProfile(), ranVlWrapper.getVlProfile());
      } catch (NotExistingEntityException e) {
        log.error(e.getMessage());
        throw new InvalidNsd(e.getMessage());
      }

      // Connect ctxVnf to vsbNsd mgmt VL
      NsVirtualLinkDesc vsbMgmtVld = null;
      if (ctxMgmtCpdId != null) {
        try {
          connectVnfToVL(ctxVnfWrapper.getVnfProfile(), ctxMgmtCpdId,
              retrieveVlInfo(vsbMgmtVld, vsbNsDf, vsbNsLvl));
        } catch (VlNotFoundInLvlMapping | NotExistingEntityException e) {
          log.error(e.getMessage());
          throw new InvalidNsd(e.getMessage());
        }
      } else {
        log.warn("Could not find a management Cp for ctxVnf. Skip.");
      }

      try {
        vsbNsd.isValid();
      } catch (MalformattedElementException e) {
        String m = "Nsd looks not valid after composition";
        log.error(m, e);
        throw new InvalidNsd(m);
      }
      vsbG = nsdGraphService.buildGraph(vsbNsd.getSapd(), vsbNsDf, vsbNsLvl);
      log.debug("Graph AFTER composition with {}:\n{}",
          ctxNsd.getNsdIdentifier(), nsdGraphService.export(vsbG));
      log.info("Completed composition for nsDf='{}' and nsLvl='{}'",
          vsbNsDf.getNsDfId(), vsbNsLvl.getNsLevelId());
      log.debug("Nsd AFTER composition with {}:\n{}",
          ctxNsd.getNsdIdentifier(), OBJECT_MAPPER.writeValueAsString(vsbNsd));
      log.info("Completed composition of '{}' with <{}, {}, {}>.",
          vsbNsd.getNsdIdentifier(), ctxNsd.getNsdIdentifier(), ctxNsDf.getNsDfId(),
          ctxNsLvl.getNsLevelId());
    }
  }

}
