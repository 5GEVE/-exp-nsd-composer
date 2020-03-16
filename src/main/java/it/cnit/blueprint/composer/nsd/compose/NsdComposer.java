package it.cnit.blueprint.composer.nsd.compose;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import it.cnit.blueprint.composer.nsd.graph.NsdGraphService;
import it.cnit.blueprint.composer.nsd.graph.ProfileVertex;
import it.cnit.blueprint.composer.rest.ConnectInput;
import it.cnit.blueprint.composer.rest.InvalidNsd;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
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

  protected VnfProfile getVnfProfileByDescId(String vnfdId, NsDf nsDf, NsLevel nsLvl)
      throws NotExistingEntityException, VnfNotFoundInLvlMapping {
    VnfProfile vnfProfile = null;
    List<VnfProfile> filterVnfp = nsDf.getVnfProfile().stream()
        .filter(p -> p.getVnfdId().equals(vnfdId)).collect(Collectors.toList());
    if (filterVnfp.isEmpty()) {
      String m = MessageFormatter
          .format("No VnfProfile with vnfdId='{}' found in nsDf='{}'", vnfdId, nsDf.getNsDfId())
          .getMessage();
      throw new NotExistingEntityException(m);
    }
    for (VnfProfile vnfp : filterVnfp) {
      if (nsLvl.getVnfToLevelMapping().stream()
          .anyMatch(m -> m.getVnfProfileId().equals(vnfp.getVnfProfileId()))) {
        vnfProfile = vnfp;
        break;
      }
    }
    if (vnfProfile == null) {
      String m = MessageFormatter
          .format("No VnfProfile with vnfdId='{}' found in nsLvl='{}'", vnfdId,
              nsLvl.getNsLevelId())
          .getMessage();
      throw new VnfNotFoundInLvlMapping(m);
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

  protected VnfInfo retrieveVnfInfoByProfileId(String vnfProfileId, Nsd nsd, NsDf nsDf,
      NsLevel nsLevel)
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
    Optional<String> optVnfdId = nsd.getVnfdId().stream()
        .filter(id -> id.equals(vnfProfile.getVnfdId())).findFirst();
    if (!optVnfdId.isPresent()) {
      String m = MessageFormatter
          .format("vnfdId='{}' not found in nsd='{}'.", vnfProfile.getVnfdId(),
              nsd.getNsdIdentifier())
          .getMessage();
      throw new InvalidNsd(m);
    }
    return new VnfInfo(vnfProfile.getVnfdId(), vnfProfile, vnfLvlMap);
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

  protected Map<String, NsVirtualLinkConnectivity> getMgmtDataCpds(VnfInfo vnfInfo,
      VlInfo vsbMgmtVlinfo, VlInfo ctxMgmtVlInfo)
      throws InvalidNsd {
    Map<String, NsVirtualLinkConnectivity> cpdIdMap = new HashMap<>();
    int dataCount = 0;
    for (NsVirtualLinkConnectivity vlc : vnfInfo.getVnfProfile().getNsVirtualLinkConnectivity()) {
      if (vlc.getVirtualLinkProfileId()
          .equals(vsbMgmtVlinfo.getVlProfile().getVirtualLinkProfileId())
          || vlc.getVirtualLinkProfileId()
          .equals(ctxMgmtVlInfo.getVlProfile().getVirtualLinkProfileId())) {
        cpdIdMap.put("mgmt", vlc);
      } else {
        cpdIdMap.put("data" + dataCount, vlc);
        dataCount++;
      }
    }
    if (!cpdIdMap.containsKey("mgmt")) {
      cpdIdMap.put("mgmt", null);
    }
    Optional<String> dataKey = cpdIdMap.keySet().stream().filter(k -> k.startsWith("data"))
        .findFirst();
    if (!dataKey.isPresent()) {
      throw new InvalidNsd(
          "No data cpd found for vnfProfile: '" + vnfInfo.getVnfProfile().getVnfProfileId() + "'.");
    }
    return cpdIdMap;
  }

  public NsVirtualLinkDesc getRanVlDesc(Sapd ranSapd, Nsd vsbNsd) throws InvalidNsd {
    NsVirtualLinkDesc ranVld;
    try {
      ranVld = getVlDescriptor(ranSapd.getNsVirtualLinkDescId(), vsbNsd);
    } catch (NotExistingEntityException e) {
      log.error(e.getMessage());
      throw new InvalidNsd(e.getMessage());
    }
    return ranVld;
  }

  @SneakyThrows(JsonProcessingException.class)
  public void compose(ConnectInput connectInput, NsVirtualLinkDesc ranVld,
      NsVirtualLinkDesc vsbMgmtVld, Nsd vsbNsd, NsVirtualLinkDesc ctxMgmtVld, Nsd ctxNsd)
      throws InvalidNsd {
    // We assume only one NsDf for the context
    NsDf ctxNsDf = ctxNsd.getNsDf().get(0);
    // We assume only one NsLevel for the context
    NsLevel ctxNsLvl = ctxNsDf.getNsInstantiationLevel().get(0);
    Graph<ProfileVertex, String> ctxG = nsdGraphService
        .buildGraph(ctxNsd.getSapd(), ctxNsDf, ctxNsLvl);
    log.debug("ctxG graph:\n{}", nsdGraphService.export(ctxG));

    log.info("Composing '{}' with <{}, {}, {}>.",
        vsbNsd.getNsdIdentifier(), ctxNsd.getNsdIdentifier(), ctxNsDf.getNsDfId(),
        ctxNsLvl.getNsLevelId());
    log.debug("Nsd BEFORE composition:\n{}", OBJECT_MAPPER.writeValueAsString(vsbNsd));

    vsbNsd.setNsdName(vsbNsd.getNsdName() + " + " + ctxNsd.getNsdName());
    for (NsDf vsbNsDf : vsbNsd.getNsDf()) {
      for (NsLevel vsbNsLvl : vsbNsDf.getNsInstantiationLevel()) {
        log.info("Start composition for nsDf='{}' and nsLvl='{}'",
            vsbNsDf.getNsDfId(), vsbNsLvl.getNsLevelId());
        Graph<ProfileVertex, String> vsbG = nsdGraphService
            .buildGraph(vsbNsd.getSapd(), vsbNsDf, vsbNsLvl);
        log.debug("vsbG BEFORE composition :\n{}", nsdGraphService.export(vsbG));

        VlInfo ranVlInfo;
        VlInfo vsbMgmtVlInfo;
        VlInfo ctxMgmtVlInfo;
        try {
          ranVlInfo = retrieveVlInfo(ranVld, vsbNsDf, vsbNsLvl);
          log.debug("Found VlInfo for ranVld='{}' in vsbNsd.", ranVld.getVirtualLinkDescId());
          vsbMgmtVlInfo = retrieveVlInfo(vsbMgmtVld, vsbNsDf, vsbNsLvl);
          log.debug("Found VlInfo for vsbMgmtVld='{}' in vsbNsd.",
              vsbMgmtVld.getVirtualLinkDescId());
          ctxMgmtVlInfo = retrieveVlInfo(ctxMgmtVld, ctxNsDf, ctxNsLvl);
          log.debug("Found VlInfo for ctxMgmtVld='{}' in ctxNsd.",
              ctxMgmtVld.getVirtualLinkDescId());
        } catch (InvalidNsd | VlNotFoundInLvlMapping e) {
          log.error(e.getMessage());
          throw new InvalidNsd(e.getMessage());
        }
        composeWithStrategy(connectInput, ranVlInfo, vsbMgmtVlInfo, ctxMgmtVlInfo,
            vsbNsd, vsbNsDf, vsbNsLvl,
            ctxNsd, ctxNsDf, ctxNsLvl);

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
    }
    log.debug("Nsd AFTER composition with {}:\n{}",
        ctxNsd.getNsdIdentifier(), OBJECT_MAPPER.writeValueAsString(vsbNsd));
    log.info("Completed composition of '{}' with <{}, {}, {}>.",
        vsbNsd.getNsdIdentifier(), ctxNsd.getNsdIdentifier(), ctxNsDf.getNsDfId(),
        ctxNsLvl.getNsLevelId());
  }

  public abstract void composeWithStrategy(
      ConnectInput connectInput, VlInfo ranVlInfo, VlInfo vsbMgmtVlInfo, VlInfo ctxMgmtVlInfo,
      Nsd vsbNsd, NsDf vsbNsDf, NsLevel vsbNsLvl,
      Nsd ctxNsd, NsDf ctxNsDf, NsLevel ctxNsLvl
  ) throws InvalidNsd;

}
