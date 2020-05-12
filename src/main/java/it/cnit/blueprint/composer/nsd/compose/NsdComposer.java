package it.cnit.blueprint.composer.nsd.compose;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import it.cnit.blueprint.composer.exceptions.NsdInvalidException;
import it.cnit.blueprint.composer.nsd.graph.NsdGraphService;
import it.cnit.blueprint.composer.nsd.graph.ProfileVertex;
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
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jgrapht.Graph;

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
    for (VnfToLevelMapping m : nsLvl.getVnfToLevelMapping()) {
      if (m.getVnfProfileId().equals(vnfProfileId)) {
        return m;
      }
    }
    throw new NotExistingEntityException("Mapping not found for VNF profile ID " + vnfProfileId);
  }

  protected VirtualLinkToLevelMapping getVlLvlMapping(String vlProfileId, NsLevel nsLvl)
      throws NotExistingEntityException {
    for (VirtualLinkToLevelMapping m : nsLvl.getVirtualLinkToLevelMapping()) {
      if (m.getVirtualLinkProfileId().equals(vlProfileId)) {
        return m;
      }
    }
    throw new NotExistingEntityException("Mapping not found for VL profile ID " + vlProfileId);
  }

  protected String getVnfDescId(String vnfdId, Nsd nsd)
      throws NotExistingEntityException {
    for (String id : nsd.getVnfdId()) {
      if (id.equals(vnfdId)) {
        return id;
      }
    }
    throw new NotExistingEntityException("VNFD ID not found for ID " + vnfdId);
  }

  protected NsVirtualLinkDesc getVlDescriptor(String vldId, Nsd nsd)
      throws NotExistingEntityException {
    for (NsVirtualLinkDesc v : nsd.getVirtualLinkDesc()) {
      if (v.getVirtualLinkDescId().equals(vldId)) {
        return v;
      }
    }
    throw new NotExistingEntityException("Descriptor not found for VLD ID " + vldId);
  }

  protected VnfInfo retrieveVnfInfoByProfileId(String vnfProfileId, Nsd nsd, NsDf nsDf,
      NsLevel nsLevel)
      throws NotExistingEntityException {
    VnfToLevelMapping vnfLvlMap = getVnfLvlMapping(vnfProfileId, nsLevel);
    VnfProfile vnfProfile = getVnfProfileById(vnfProfileId, nsDf);
    String vnfdId = getVnfDescId(vnfProfile.getVnfdId(), nsd);
    return new VnfInfo(vnfdId, vnfProfile, vnfLvlMap);
  }

  protected VlInfo retrieveVlInfoByProfileId(String vlProfileId, Nsd nsd, NsDf nsDf,
      NsLevel nsLevel)
      throws NotExistingEntityException {
    VirtualLinkToLevelMapping vlMap = getVlLvlMapping(vlProfileId, nsLevel);
    VirtualLinkProfile vlProfile = getVlProfileById(vlProfileId, nsDf);
    NsVirtualLinkDesc vlDesc = getVlDescriptor(vlProfile.getVirtualLinkDescId(), nsd);
    return new VlInfo(vlDesc, vlProfile, vlMap);
  }

  protected VlInfo retrieveVlInfoByDesc(NsVirtualLinkDesc vld, NsDf nsDf, NsLevel nsLevel)
      throws NotExistingEntityException {
    VirtualLinkProfile vlProfile = getVlProfileByDescId(vld.getVirtualLinkDescId(), nsDf);
    VirtualLinkToLevelMapping vlMap = getVlLvlMapping(vlProfile.getVirtualLinkProfileId(), nsLevel);
    return new VlInfo(vld, vlProfile, vlMap);
  }

  protected void addVnf(VnfInfo vnfInfo, Nsd nsd, NsDf nsDf, NsLevel nsLevel) {
    String vnfdId = vnfInfo.getVnfdId();
    if (nsd.getVnfdId().stream().noneMatch(id -> id.equals(vnfdId))) {
      nsd.getVnfdId().add(vnfdId);
    }
    VnfProfile vnfp = vnfInfo.getVnfProfile();
    if (nsDf.getVnfProfile().stream()
        .noneMatch(vp -> vp.getVnfProfileId().equals(vnfp.getVnfProfileId()))) {
      nsDf.getVnfProfile().add(vnfp);
    }
    VnfToLevelMapping vnfMap = vnfInfo.getVnfToLevelMapping();
    if (nsLevel.getVnfToLevelMapping().stream()
        .noneMatch(lm -> lm.getVnfProfileId().equals(vnfp.getVnfProfileId()))) {
      nsLevel.getVnfToLevelMapping().add(vnfMap);
    }
  }

  protected void addVirtualLink(VlInfo vlInfo, Nsd nsd, NsDf nsDf, NsLevel nsLevel) {
    NsVirtualLinkDesc vld = vlInfo.getVlDescriptor();
    if (nsd.getVirtualLinkDesc().stream()
        .noneMatch(nsdVld -> nsdVld.getVirtualLinkDescId().equals(vld.getVirtualLinkDescId()))) {
      nsd.getVirtualLinkDesc().add(vld);
    }
    VirtualLinkProfile vlp = vlInfo.getVlProfile();
    if (nsDf.getVirtualLinkProfile().stream()
        .noneMatch(vp -> vp.getVirtualLinkProfileId().equals(vlp.getVirtualLinkProfileId()))) {
      nsDf.getVirtualLinkProfile().add(vlp);
    }
    VirtualLinkToLevelMapping vlMap = vlInfo.getVlToLevelMapping();
    if (nsLevel.getVirtualLinkToLevelMapping().stream()
        .noneMatch(lm -> lm.getVirtualLinkProfileId().equals(vlMap.getVirtualLinkProfileId()))) {
      nsLevel.getVirtualLinkToLevelMapping().add(vlMap);
    }
  }

  protected void connectVnfToVL(VnfProfile vnfp, String cpdId, VirtualLinkProfile vlp)
      throws NotExistingEntityException {
    for (NsVirtualLinkConnectivity nsVlC : vnfp.getNsVirtualLinkConnectivity()) {
      if (nsVlC.getCpdId().contains(cpdId)) {
        nsVlC.setVirtualLinkProfileId(vlp.getVirtualLinkProfileId());
        return;
      }
    }
    throw new NotExistingEntityException("VL connectivity not found for CPD ID " + cpdId);
  }

  public NsVirtualLinkDesc getRanVlDesc(Sapd ranSapd, Nsd expNsd) throws NsdInvalidException {
    try {
      return getVlDescriptor(ranSapd.getNsVirtualLinkDescId(), expNsd);
    } catch (NotExistingEntityException e) {
      log.error(e.getMessage());
      throw new NsdInvalidException(expNsd.getNsdIdentifier(),
          "VLD not found for SAP " + ranSapd.getCpdId(), e);
    }
  }

  @SneakyThrows(JsonProcessingException.class)
  public void compose(Map<String, String> connectInput, NsVirtualLinkDesc ranVld,
      NsVirtualLinkDesc expMgmtVld, Nsd expNsd, NsVirtualLinkDesc ctxMgmtVld, Nsd ctxNsd)
      throws NsdInvalidException {
    // We assume only one NsDf for the context
    NsDf ctxNsDf = ctxNsd.getNsDf().get(0);
    // We assume only one NsLevel for the context
    NsLevel ctxNsLvl = ctxNsDf.getNsInstantiationLevel().get(0);
    Graph<ProfileVertex, String> ctxG = nsdGraphService
        .buildGraph(ctxNsd.getSapd(), ctxNsDf, ctxNsLvl);
    log.debug("ctxG graph:\n{}", nsdGraphService.export(ctxG));

    log.info("Composing {} with <{}, {}, {}>.",
        expNsd.getNsdIdentifier(), ctxNsd.getNsdIdentifier(), ctxNsDf.getNsDfId(),
        ctxNsLvl.getNsLevelId());
    log.debug("Nsd BEFORE composition:\n{}", OBJECT_MAPPER.writeValueAsString(expNsd));

    expNsd.setNsdName(expNsd.getNsdName() + " + " + ctxNsd.getNsdName());
    for (NsDf expNsDf : expNsd.getNsDf()) {
      for (NsLevel expNsLvl : expNsDf.getNsInstantiationLevel()) {
        log.info("Start composition for nsDf {} and nsLvl {}",
            expNsDf.getNsDfId(), expNsLvl.getNsLevelId());
        Graph<ProfileVertex, String> expG = nsdGraphService
            .buildGraph(expNsd.getSapd(), expNsDf, expNsLvl);
        log.debug("expG BEFORE composition :\n{}", nsdGraphService.export(expG));
        if (!nsdGraphService.isConnected(expG)) {
          throw new NsdInvalidException(expNsd.getNsdIdentifier(),
              "Network topology not connected for NsDf " + expNsDf.getNsDfId() + " and NsLevel "
                  + expNsLvl.getNsLevelId());
        }

        VlInfo ranVlInfo;
        try {
          ranVlInfo = retrieveVlInfoByDesc(ranVld, expNsDf, expNsLvl);
          log.debug("Found VlInfo for ranVld {} in expNsd.", ranVld.getVirtualLinkDescId());
        } catch (NotExistingEntityException e) {
          throw new NsdInvalidException(expNsd.getNsdIdentifier(),
              "Error retrieving RAN VL info for VLD " + ranVld, e);
        }
        VlInfo expMgmtVlInfo;
        try {
          expMgmtVlInfo = retrieveVlInfoByDesc(expMgmtVld, expNsDf, expNsLvl);
          log.debug("Found VlInfo for expMgmtVld {} in expNsd.",
              expMgmtVld.getVirtualLinkDescId());
        } catch (NotExistingEntityException e) {
          throw new NsdInvalidException(expNsd.getNsdIdentifier(),
              "Error retrieving Experiment Management VL info for VLD " + ranVld
                  .getVirtualLinkDescId(), e);
        }
        VlInfo ctxMgmtVlInfo;
        try {
          ctxMgmtVlInfo = retrieveVlInfoByDesc(ctxMgmtVld, ctxNsDf, ctxNsLvl);
          log.debug("Found VlInfo for ctxMgmtVld {} in ctxNsd.",
              ctxMgmtVld.getVirtualLinkDescId());
        } catch (NotExistingEntityException e) {
          throw new NsdInvalidException(ctxNsd.getNsdIdentifier(),
              "Error retrieving Context Management VL info for VLD " + ranVld, e);
        }
        composeWithStrategy(connectInput, ranVlInfo, expMgmtVlInfo, ctxMgmtVlInfo,
            expNsd, expNsDf, expNsLvl,
            ctxNsd, ctxNsDf, ctxNsLvl);

        // Nsd validation and logging
        try {
          expNsd.isValid();
        } catch (MalformattedElementException e) {
          throw new NsdInvalidException(expNsd.getNsdIdentifier(),
              "Nsd not valid after composition", e);
        }
        expG = nsdGraphService.buildGraph(expNsd.getSapd(), expNsDf, expNsLvl);
        log.debug("Graph AFTER composition with {}:\n{}",
            ctxNsd.getNsdIdentifier(), nsdGraphService.export(expG));
        if (!nsdGraphService.isConnected(expG)) {
          throw new NsdInvalidException(expNsd.getNsdIdentifier(),
              "Network topology not connected for NsDf " + expNsDf.getNsDfId() + " and NsLevel "
                  + expNsLvl.getNsLevelId());
        }
        log.info("Completed composition for nsDf {} and nsLvl {}",
            expNsDf.getNsDfId(), expNsLvl.getNsLevelId());
      }
    }
    log.debug("Nsd AFTER composition with {}:\n{}",
        ctxNsd.getNsdIdentifier(), OBJECT_MAPPER.writeValueAsString(expNsd));
    log.info("Completed composition of {} with <{}, {}, {}>.",
        expNsd.getNsdIdentifier(), ctxNsd.getNsdIdentifier(), ctxNsDf.getNsDfId(),
        ctxNsLvl.getNsLevelId());
  }

  public abstract void composeWithStrategy(
      Map<String, String> connectInput, VlInfo ranVlInfo, VlInfo expMgmtVlInfo,
      VlInfo ctxMgmtVlInfo,
      Nsd expNsd, NsDf expNsDf, NsLevel expNsLvl,
      Nsd ctxNsd, NsDf ctxNsDf, NsLevel ctxNsLvl
  ) throws NsdInvalidException;

}
