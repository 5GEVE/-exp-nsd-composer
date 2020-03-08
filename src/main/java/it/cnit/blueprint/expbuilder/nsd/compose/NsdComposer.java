package it.cnit.blueprint.expbuilder.nsd.compose;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import it.cnit.blueprint.expbuilder.nsd.graph.NsdGraphService;
import it.cnit.blueprint.expbuilder.nsd.graph.ProfileVertex;
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
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jgrapht.Graph;
import org.slf4j.helpers.MessageFormatter;

@Slf4j
@AllArgsConstructor
public abstract class NsdComposer {

  protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper(new YAMLFactory());

  protected NsdGraphService nsdGraphService;

  protected VnfProfile getVnfProfileByDescId(String vnfdId, NsDf nsDf)
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

  protected VirtualLinkProfile getVlProfile(String vlProfileId, NsDf nsDf)
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

  protected VirtualLinkProfile getVlProfile(NsVirtualLinkDesc vld, NsDf nsDf)
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

  protected VnfToLevelMapping getVnfLvlMapping(String vnfProfileId, NsLevel nsLvl)
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

  protected NsVirtualLinkDesc getVlDescriptor(String vlDescId, Nsd nsd)
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

  protected VirtualLinkToLevelMapping getVlLvlMapping(String vlProfileId, NsLevel nsLvl)
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

  protected void addVnf(VnfInfo vnfInfo, Nsd nsd, NsDf nsDf, NsLevel nsLevel) {
    String vnfdId = vnfInfo.getVfndId();
    if (nsd.getVnfdId().stream().noneMatch(id -> id.equals(vnfdId))) {
      nsd.getVnfdId().add(vnfdId);
    }
    VnfProfile vnfProfile = vnfInfo.getVnfProfile();
    if (nsDf.getVnfProfile().stream()
        .noneMatch(vp -> vp.getVnfProfileId().equals(vnfProfile.getVnfProfileId()))) {
      nsDf.getVnfProfile().add(vnfProfile);
    }
    VnfToLevelMapping vnfLvlMap = vnfInfo.getVnfToLevelMapping();
    if (nsLevel.getVnfToLevelMapping().stream()
        .noneMatch(lm -> lm.getVnfProfileId().equals(vnfProfile.getVnfProfileId()))) {
      nsLevel.getVnfToLevelMapping().add(vnfLvlMap);
    }
  }

  protected void addVirtualLink(VlInfo vlInfo, Nsd nsd, NsDf nsDf, NsLevel nsLevel) {
    NsVirtualLinkDesc vlDesc = vlInfo.getVlDescriptor();
    if (nsd.getVirtualLinkDesc().stream()
        .noneMatch(nsdVld -> nsdVld.getVirtualLinkDescId().equals(vlDesc.getVirtualLinkDescId()))) {
      nsd.getVirtualLinkDesc().add(vlDesc);
    }
    VirtualLinkProfile vlProfile = vlInfo.getVlProfile();
    if (nsDf.getVirtualLinkProfile().stream().noneMatch(
        nsdfVlP -> nsdfVlP.getVirtualLinkProfileId().equals(vlProfile.getVirtualLinkProfileId()))) {
      nsDf.getVirtualLinkProfile().add(vlProfile);
    }
    VirtualLinkToLevelMapping vlMap = vlInfo.getVlToLevelMapping();
    if (nsLevel.getVirtualLinkToLevelMapping().stream().noneMatch(
        nslevelMap -> nslevelMap.getVirtualLinkProfileId()
            .equals(vlMap.getVirtualLinkProfileId()))) {
      nsLevel.getVirtualLinkToLevelMapping().add(vlMap);
    }
  }

  protected VnfInfo retrieveVnfInfoByDescId(String vnfdId, Nsd nsd, NsDf nsDf,
      NsLevel nsLevel)
      throws InvalidNsd, VnfNotFoundInLvlMapping {
    Optional<String> optVnfdId = nsd.getVnfdId().stream()
        .filter(id -> id.equals(vnfdId)).findFirst();
    if (!optVnfdId.isPresent()) {
      String m = MessageFormatter
          .format("vnfdId='{}' not found in nsd='{}'.", vnfdId, nsd.getNsdIdentifier())
          .getMessage();
      throw new InvalidNsd(m);
    }
    VnfProfile vnfProfile;
    try {
      vnfProfile = getVnfProfileByDescId(vnfdId, nsDf);
    } catch (NotExistingEntityException e) {
      throw new InvalidNsd(e.getMessage());
    }
    VnfToLevelMapping vnfLvlMap;
    try {
      vnfLvlMap = getVnfLvlMapping(vnfProfile.getVnfProfileId(), nsLevel);
    } catch (NotExistingEntityException e) {
      throw new VnfNotFoundInLvlMapping(e.getMessage());
    }
    return new VnfInfo(vnfdId, vnfProfile, vnfLvlMap);
  }

  protected VlInfo retrieveVlInfo(String vlProfileId, Nsd nsd, NsDf nsDf, NsLevel nsLevel)
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
    return new VlInfo(vlMap, vlProfile, vlDesc);
  }

  protected VlInfo retrieveVlInfo(NsVirtualLinkDesc vld, NsDf nsDf, NsLevel nsLevel)
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
    return new VlInfo(vlMap, vlProfile, vld);
  }

  protected void connectVnfToVL(VnfProfile vnfp, String cpdId, VirtualLinkProfile vlp)
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
  public void compose(Sapd ranSapd, NsVirtualLinkDesc vsbMgmtVld, Nsd vsbNsd,
      NsVirtualLinkDesc ctxMgmtVld, Nsd ctxNsd)
      throws InvalidNsd {
    NsVirtualLinkDesc ranVld;
    try {
      ranVld = getVlDescriptor(ranSapd.getNsVirtualLinkDescId(), vsbNsd);
    } catch (NotExistingEntityException e) {
      log.error(e.getMessage());
      throw new InvalidNsd(e.getMessage());
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

      composeWithStrategy(ranVld, vsbMgmtVld, ctxMgmtVld,
          vsbNsd, vsbNsDf, vsbNsLvl, vsbG,
          ctxNsd, ctxNsDf, ctxNsLvl, ctxG);

      // Nsd validation and logging
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
    }
    log.debug("Nsd AFTER composition with {}:\n{}",
        ctxNsd.getNsdIdentifier(), OBJECT_MAPPER.writeValueAsString(vsbNsd));
    log.info("Completed composition of '{}' with <{}, {}, {}>.",
        vsbNsd.getNsdIdentifier(), ctxNsd.getNsdIdentifier(), ctxNsDf.getNsDfId(),
        ctxNsLvl.getNsLevelId());
  }

  public abstract void composeWithStrategy(
      NsVirtualLinkDesc ranVld, NsVirtualLinkDesc vsbMgmtVld, NsVirtualLinkDesc ctxMgmtVld,
      Nsd vsbNsd, NsDf vsbNsDf, NsLevel vsbNsLvl, Graph<ProfileVertex, String> vsbG,
      Nsd ctxNsd, NsDf ctxNsDf, NsLevel ctxNsLvl, Graph<ProfileVertex, String> ctxG
  ) throws InvalidNsd;

  @SneakyThrows(JsonProcessingException.class)
  public void composeConnectNoInput(Sapd ranSapd, NsVirtualLinkDesc vsbMgmtVld, Nsd vsbNsd,
      NsVirtualLinkDesc ctxMgmtVld, Nsd ctxNsd) {
  }

  @SneakyThrows(JsonProcessingException.class)
  public void composeConnect(NsVirtualLinkDesc srcVl, NsVirtualLinkDesc dstVl, Nsd vsbNsd,
      NsVirtualLinkDesc ctxMgmtVld, Nsd ctxNsd) {
  }
}
