package it.cnit.blueprint.composer.nsd.compose;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import it.cnit.blueprint.composer.nsd.graph.NsdGraphService;
import it.cnit.blueprint.composer.nsd.graph.ProfileVertex;
import it.cnit.blueprint.composer.rest.InvalidNsdException;
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
import java.util.Map;
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

  protected VnfProfile getVnfProfileById(String vnfProfileId, NsDf nsDf)
      throws NotExistingEntityException {
    for (VnfProfile vp : nsDf.getVnfProfile()) {
      if (vp.getVnfProfileId().equals(vnfProfileId)) {
        return vp;
      }
    }
    throw new NotExistingEntityException("VNF profile not found for ID " + vnfProfileId);
  }

  protected VnfProfile getVnfProfileByDescId(String vnfdId, NsDf nsDf)
      throws NotExistingEntityException {
    for (VnfProfile vp : nsDf.getVnfProfile()) {
      if (vp.getVnfdId().equals(vnfdId)) {
        return vp;
      }
    }
    throw new NotExistingEntityException("VNF profile not found for VNFD ID " + vnfdId);
  }

  protected VirtualLinkProfile getVlProfileById(String vlProfileId, NsDf nsDf)
      throws NotExistingEntityException {
    for (VirtualLinkProfile vp : nsDf.getVirtualLinkProfile()) {
      if (vp.getVirtualLinkProfileId().equals(vlProfileId)) {
        return vp;
      }
    }
    throw new NotExistingEntityException("VL profile not found for ID " + vlProfileId);
  }

  protected VirtualLinkProfile getVlProfileByDescId(String vldId, NsDf nsDf)
      throws NotExistingEntityException {
    for (VirtualLinkProfile vl : nsDf.getVirtualLinkProfile()) {
      if (vl.getVirtualLinkDescId().equals(vldId)) {
        return vl;
      }
    }
    throw new NotExistingEntityException("VL profile not found for VLD ID " + vldId);
  }

  protected VnfToLevelMapping getVnfLvlMapping(String vnfProfileId, NsLevel nsLvl)
      throws NotExistingEntityException {
    for (VnfToLevelMapping m: nsLvl.getVnfToLevelMapping()){
      if (m.getVnfProfileId().equals(vnfProfileId)){
        return m;
      }
    }
    throw new NotExistingEntityException("Mapping not found for VNF profile ID " + vnfProfileId);
  }

  protected VirtualLinkToLevelMapping getVlLvlMapping(String vlProfileId, NsLevel nsLvl)
      throws NotExistingEntityException {
    for (VirtualLinkToLevelMapping m: nsLvl.getVirtualLinkToLevelMapping()){
      if (m.getVirtualLinkProfileId().equals(vlProfileId)){
        return m;
      }
    }
    throw new NotExistingEntityException("Mapping not found for VL profile ID " + vlProfileId);
  }

  protected String getVnfDescId(String vnfdId, Nsd nsd)
      throws NotExistingEntityException {
    for (String id: nsd.getVnfdId()){
      if (id.equals(vnfdId)){
        return id;
      }
    }
    throw new NotExistingEntityException("VNFD ID not found for ID " + vnfdId);
  }

  protected NsVirtualLinkDesc getVlDescriptor(String vldId, Nsd nsd)
      throws NotExistingEntityException {
    for (NsVirtualLinkDesc v: nsd.getVirtualLinkDesc()){
      if (v.getVirtualLinkDescId().equals(vldId)){
        return v;
      }
    }
    throw new NotExistingEntityException("Descriptor not found for VLD ID " + vldId);
  }

  protected VnfInfo retrieveVnfInfoByProfileId(String vnfProfileId, Nsd nsd, NsDf nsDf,
      NsLevel nsLevel)
      throws NotExistingEntityException {
    VnfToLevelMapping vnfLvlMap = getVnfLvlMapping(vnfProfileId, nsLevel);
    VnfProfile vnfProfile = getVnfProfileById(vnfProfileId, nsDf);;
    String vnfdId = getVnfDescId(vnfProfile.getVnfdId(), nsd);
    return new VnfInfo(vnfdId, vnfProfile, vnfLvlMap);
  }

  protected VlInfo retrieveVlInfo(String vlProfileId, Nsd nsd, NsDf nsDf, NsLevel nsLevel)
      throws InvalidNsdException, VlNotFoundInLvlMapping {
    VirtualLinkToLevelMapping vlMap;
    try {
      vlMap = getVlLvlMapping(vlProfileId, nsLevel);
    } catch (NotExistingEntityException e) {
      throw new VlNotFoundInLvlMapping(e.getMessage());
    }
    VirtualLinkProfile vlProfile;
    try {
      vlProfile = getVlProfileById(vlProfileId, nsDf);
    } catch (NotExistingEntityException e) {
      throw new InvalidNsdException(e.getMessage());
    }
    NsVirtualLinkDesc vlDesc;
    try {
      vlDesc = getVlDescriptor(vlProfile.getVirtualLinkDescId(), nsd);
    } catch (NotExistingEntityException e) {
      throw new InvalidNsdException(e.getMessage());
    }
    return new VlInfo(vlMap, vlProfile, vlDesc);
  }

  protected VlInfo retrieveVlInfo(NsVirtualLinkDesc vld, NsDf nsDf, NsLevel nsLevel)
      throws InvalidNsdException, VlNotFoundInLvlMapping {
    VirtualLinkProfile vlProfile;
    try {
      vlProfile = getVlProfileByDescId(vld.getVirtualLinkDescId(), nsDf);
    } catch (NotExistingEntityException e) {
      throw new InvalidNsdException(e.getMessage());
    }
    VirtualLinkToLevelMapping vlMap;
    try {
      vlMap = getVlLvlMapping(vlProfile.getVirtualLinkProfileId(), nsLevel);
    } catch (NotExistingEntityException e) {
      throw new VlNotFoundInLvlMapping(e.getMessage());
    }
    return new VlInfo(vlMap, vlProfile, vld);
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

  public NsVirtualLinkDesc getRanVlDesc(Sapd ranSapd, Nsd expNsd) throws InvalidNsdException {
    NsVirtualLinkDesc ranVld;
    try {
      ranVld = getVlDescriptor(ranSapd.getNsVirtualLinkDescId(), expNsd);
    } catch (NotExistingEntityException e) {
      log.error(e.getMessage());
      throw new InvalidNsdException(e.getMessage());
    }
    return ranVld;
  }

  @SneakyThrows(JsonProcessingException.class)
  public void compose(Map<String, String> connectInput, NsVirtualLinkDesc ranVld,
      NsVirtualLinkDesc expMgmtVld, Nsd expNsd, NsVirtualLinkDesc ctxMgmtVld, Nsd ctxNsd)
      throws InvalidNsdException {
    // We assume only one NsDf for the context
    NsDf ctxNsDf = ctxNsd.getNsDf().get(0);
    // We assume only one NsLevel for the context
    NsLevel ctxNsLvl = ctxNsDf.getNsInstantiationLevel().get(0);
    Graph<ProfileVertex, String> ctxG = nsdGraphService
        .buildGraph(ctxNsd.getSapd(), ctxNsDf, ctxNsLvl);
    log.debug("ctxG graph:\n{}", nsdGraphService.export(ctxG));

    log.info("Composing '{}' with <{}, {}, {}>.",
        expNsd.getNsdIdentifier(), ctxNsd.getNsdIdentifier(), ctxNsDf.getNsDfId(),
        ctxNsLvl.getNsLevelId());
    log.debug("Nsd BEFORE composition:\n{}", OBJECT_MAPPER.writeValueAsString(expNsd));

    expNsd.setNsdName(expNsd.getNsdName() + " + " + ctxNsd.getNsdName());
    for (NsDf expNsDf : expNsd.getNsDf()) {
      for (NsLevel expNsLvl : expNsDf.getNsInstantiationLevel()) {
        log.info("Start composition for nsDf='{}' and nsLvl='{}'",
            expNsDf.getNsDfId(), expNsLvl.getNsLevelId());
        Graph<ProfileVertex, String> expG = nsdGraphService
            .buildGraph(expNsd.getSapd(), expNsDf, expNsLvl);
        log.debug("expG BEFORE composition :\n{}", nsdGraphService.export(expG));
        if (!nsdGraphService.isConnected(expG)) {
          String m = MessageFormatter.format(
              "Network topology for NsLevel='{}' is not connected", expNsLvl.getNsLevelId())
              .getMessage();
          log.error(m);
          throw new InvalidNsdException(m);
        }

        VlInfo ranVlInfo;
        VlInfo expMgmtVlInfo;
        VlInfo ctxMgmtVlInfo;
        try {
          ranVlInfo = retrieveVlInfo(ranVld, expNsDf, expNsLvl);
          log.debug("Found VlInfo for ranVld='{}' in expNsd.", ranVld.getVirtualLinkDescId());
          expMgmtVlInfo = retrieveVlInfo(expMgmtVld, expNsDf, expNsLvl);
          log.debug("Found VlInfo for expMgmtVld='{}' in expNsd.",
              expMgmtVld.getVirtualLinkDescId());
          ctxMgmtVlInfo = retrieveVlInfo(ctxMgmtVld, ctxNsDf, ctxNsLvl);
          log.debug("Found VlInfo for ctxMgmtVld='{}' in ctxNsd.",
              ctxMgmtVld.getVirtualLinkDescId());
        } catch (InvalidNsdException | VlNotFoundInLvlMapping e) {
          log.error(e.getMessage());
          throw new InvalidNsdException(e.getMessage());
        }
        composeWithStrategy(connectInput, ranVlInfo, expMgmtVlInfo, ctxMgmtVlInfo,
            expNsd, expNsDf, expNsLvl,
            ctxNsd, ctxNsDf, ctxNsLvl);

        // Nsd validation and logging
        try {
          expNsd.isValid();
        } catch (MalformattedElementException e) {
          String m = "Nsd looks not valid after composition";
          log.error(m, e);
          throw new InvalidNsdException(m);
        }
        expG = nsdGraphService.buildGraph(expNsd.getSapd(), expNsDf, expNsLvl);
        log.debug("Graph AFTER composition with {}:\n{}",
            ctxNsd.getNsdIdentifier(), nsdGraphService.export(expG));
        if (!nsdGraphService.isConnected(expG)) {
          String m = MessageFormatter.format("Network topology not connected after composition",
              expNsDf.getNsDfId(), expNsLvl.getNsLevelId())
              .getMessage();
          log.error(m);
          throw new InvalidNsdException(m);
        }
        log.info("Completed composition for nsDf='{}' and nsLvl='{}'",
            expNsDf.getNsDfId(), expNsLvl.getNsLevelId());
      }
    }
    log.debug("Nsd AFTER composition with {}:\n{}",
        ctxNsd.getNsdIdentifier(), OBJECT_MAPPER.writeValueAsString(expNsd));
    log.info("Completed composition of '{}' with <{}, {}, {}>.",
        expNsd.getNsdIdentifier(), ctxNsd.getNsdIdentifier(), ctxNsDf.getNsDfId(),
        ctxNsLvl.getNsLevelId());
  }

  public abstract void composeWithStrategy(
      Map<String, String> connectInput, VlInfo ranVlInfo, VlInfo expMgmtVlInfo,
      VlInfo ctxMgmtVlInfo,
      Nsd expNsd, NsDf expNsDf, NsLevel expNsLvl,
      Nsd ctxNsd, NsDf ctxNsDf, NsLevel ctxNsLvl
  ) throws InvalidNsdException;

}
