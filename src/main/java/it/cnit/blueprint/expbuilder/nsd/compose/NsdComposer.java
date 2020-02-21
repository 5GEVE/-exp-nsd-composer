package it.cnit.blueprint.expbuilder.nsd.compose;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import it.cnit.blueprint.expbuilder.nsd.graph.NsdGraphService;
import it.cnit.blueprint.expbuilder.nsd.graph.ProfileVertex;
import it.cnit.blueprint.expbuilder.rest.CtxComposeInfo;
import it.cnit.blueprint.expbuilder.rest.InvalidCtxComposeInfo;
import it.cnit.blueprint.expbuilder.rest.InvalidNsd;
import it.cnit.blueprint.expbuilder.rest.VnfConnection;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.MalformattedElementException;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.NotExistingEntityException;
import it.nextworks.nfvmano.libs.ifa.descriptors.common.elements.VirtualLinkProfile;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsDf;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsLevel;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsVirtualLinkConnectivity;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsVirtualLinkDesc;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Nsd;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.VirtualLinkToLevelMapping;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.VnfProfile;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.VnfToLevelMapping;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jgrapht.Graph;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Qualifier;
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

  @Qualifier("connect")
  private CompositionStrategy connectStrategy;

  @Qualifier("passthrough")
  private CompositionStrategy passThroughStrategy;

  public enum CompositionStrat {
    CONNECT,
    PASSTHROUGH
  }

  private VnfProfile getVnfProfile(String vnfProfileId, NsDf nsDf)
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

  private void addVnf(Nsd nsd, NsDf nsDf, NsLevel nsLevel, VnfWrapper vnfWrapper) {
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

  private void addVirtualLink(Nsd nsd, NsDf nsDf, NsLevel nsLevel, VlWrapper vlWrapper) {
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

  private VnfWrapper retrieveVnfInfo(String vnfProfileId, String cpdId, Nsd nsd, NsDf nsDf,
      NsLevel nsLevel) throws InvalidNsd, InvalidCtxComposeInfo, VnfNotFoundInLvlMapping {
    VnfToLevelMapping vnfLvlMap;
    try {
      vnfLvlMap = getVnfLvlMapping(vnfProfileId, nsLevel);
    } catch (NotExistingEntityException e) {
      throw new VnfNotFoundInLvlMapping(e.getMessage());
    }
    VnfProfile vnfProfile;
    try {
      vnfProfile = getVnfProfile(vnfProfileId, nsDf);
    } catch (NotExistingEntityException e) {
      throw new InvalidNsd(e.getMessage());
    }
    NsVirtualLinkConnectivity vlC;
    try {
      vlC = getVlConnectivity(cpdId, vnfProfile);
    } catch (NotExistingEntityException e) {
      throw new InvalidCtxComposeInfo(e.getMessage());
    }
    String vnfdId;
    try {
      vnfdId = getVnfdId(vnfProfile.getVnfdId(), nsd);
    } catch (NotExistingEntityException e) {
      log.error(e.getMessage());
      throw new InvalidNsd(e.getMessage());
    }
    return new VnfWrapper(vnfLvlMap, vnfProfile, vlC, vnfdId);
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

  @SuppressWarnings("DuplicatedCode")
  @SneakyThrows(JsonProcessingException.class)
  public void compose(Nsd vsNsd, CtxComposeInfo[] ctxComposeInfos)
      throws InvalidCtxComposeInfo, InvalidNsd {
    ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
    for (CtxComposeInfo ctxComposeInfo : ctxComposeInfos) {
      Nsd ctxNsd = ctxComposeInfo.getCtxBReq().getNsds().get(0);
      // We assume only one NsDf for the context
      NsDf ctxNsDf = ctxNsd.getNsDf().get(0);
      // We assume only one NsLevel for the context
      NsLevel ctxNsLvl = ctxNsDf.getNsInstantiationLevel().get(0);
      for (NsDf vsNsDf : vsNsd.getNsDf()) {
        for (NsLevel vsNsLvl : vsNsDf.getNsInstantiationLevel()) {
          log.debug("Nsd before:\n{}", objectMapper.writeValueAsString(vsNsd));
          Graph<ProfileVertex, String> g = nsdGraphService
              .buildGraph(vsNsd.getSapd(), vsNsDf, vsNsLvl);
          log.debug("Graph export before:\n{}", nsdGraphService.export(g));
          for (VnfConnection ctxC : ctxComposeInfo.getCtxConnections()) {
            VnfWrapper vnfWrapper;
            VlWrapper vlWrapper;

            // Retrieve the VNF from context Nsd
            try {
              vnfWrapper = retrieveVnfInfo(ctxC.getVnfProfileId(), ctxC.getCpdId(),
                  ctxNsd, ctxNsDf, ctxNsLvl);
            } catch (VnfNotFoundInLvlMapping e) {
              log.warn(e.getMessage() + " Skip.");
              continue;
            } catch (InvalidNsd | InvalidCtxComposeInfo e) {
              log.error(e.getMessage());
              throw e;
            }

            // Retrieve the VirtualLink from vertical Nsd
            try {
              vlWrapper = retrieveVlInfo(ctxC.getVlProfileId(), vsNsd, vsNsDf, vsNsLvl);
            } catch (VlNotFoundInLvlMapping e) {
              log.warn(e.getMessage() + " Trying in context.");
              // Retrieve the VirtualLink from context Nsd (and add it)
              try {
                vlWrapper = retrieveVlInfo(ctxC.getVlProfileId(), ctxNsd, ctxNsDf, ctxNsLvl);
                addVirtualLink(vsNsd, vsNsDf, vsNsLvl, vlWrapper);
              } catch (VlNotFoundInLvlMapping vlNotFoundInLvlMapping) {
                log.warn(e.getMessage());
                String m = MessageFormatter
                    .format("vlProfile='{}' not found neither in context or vertical service",
                        ctxC.getVlProfileId()).getMessage();
                log.error(m);
                throw new InvalidCtxComposeInfo(m);
              }
            } catch (InvalidNsd e) {
              log.error(e.getMessage());
              throw e;
            }

            // Create connection between Vnf and VL
            vnfWrapper.getVlConnectivity()
                .setVirtualLinkProfileId(vlWrapper.getVlProfile().getVirtualLinkProfileId());
            addVnf(vsNsd, vsNsDf, vsNsLvl, vnfWrapper);
          }
          for (VnfConnection vsC : ctxComposeInfo.getVsConnections()) {
            VnfWrapper vnfWrapper;
            VlWrapper vlWrapper;

            // Retrieve the VNF from vertical Nsd
            try {
              vnfWrapper = retrieveVnfInfo(vsC.getVnfProfileId(), vsC.getCpdId(),
                  vsNsd, vsNsDf, vsNsLvl);
            } catch (VnfNotFoundInLvlMapping e) {
              log.warn(e.getMessage() + " Skip.");
              continue;
            } catch (InvalidNsd | InvalidCtxComposeInfo e) {
              log.error(e.getMessage());
              throw e;
            }

            // Retrieve the VirtualLink from context Nsd (and add it)
            try {
              vlWrapper = retrieveVlInfo(vsC.getVlProfileId(), ctxNsd, ctxNsDf, ctxNsLvl);
              addVirtualLink(vsNsd, vsNsDf, vsNsLvl, vlWrapper);
            } catch (VlNotFoundInLvlMapping e) {
              log.warn(e.getMessage());
              String m = MessageFormatter
                  .format("vlProfile='{}' not found in context.", vsC.getVlProfileId())
                  .getMessage();
              log.error(m);
              throw new InvalidCtxComposeInfo(m);
            } catch (InvalidNsd e) {
              log.error(e.getMessage());
              throw e;
            }
            // Create connection between Vnf and VL
            vnfWrapper.getVlConnectivity()
                .setVirtualLinkProfileId(vlWrapper.getVlProfile().getVirtualLinkProfileId());
          }
          log.debug("Nsd after:\n{}", objectMapper.writeValueAsString(vsNsd));
          g = nsdGraphService.buildGraph(vsNsd.getSapd(), vsNsDf, vsNsLvl);
          log.debug("Graph export after:\n{}", nsdGraphService.export(g));
          try {
            vsNsd.isValid();
          } catch (MalformattedElementException e) {
            String m = "Nsd looks not valid after composition";
            log.error(m, e);
            throw new InvalidNsd(m);
          }
        }
      }
    }
  }

}
