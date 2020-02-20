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
      String message = MessageFormatter
          .format("VnfProfile='{}' not found in nsDf='{}'", vnfProfileId, nsDf.getNsDfId())
          .getMessage();
      throw new NotExistingEntityException(message);
    }
    return vnfProfile;
  }

  private VirtualLinkProfile getVlProfile(String vlProfileId, NsDf nsDf)
      throws NotExistingEntityException {
    VirtualLinkProfile vlProfile;
    try {
      vlProfile = nsDf.getVirtualLinkProfile(vlProfileId);
    } catch (NotExistingEntityException e) {
      String message = MessageFormatter
          .format("vlProfileId='{}' not found in nsDf='{}'.", vlProfileId, nsDf.getNsDfId())
          .getMessage();
      throw new NotExistingEntityException(message);
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
      String message = MessageFormatter
          .format("NsVirtualLinkConnectivity for cpdId='{}' not found in vnfProfile='{}'",
              cpdId, vnfProfile.getVnfProfileId()).getMessage();
      throw new NotExistingEntityException(message);
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
      String message = MessageFormatter
          .format("vnfProfileId='{}' not found in nsLvl='{}'.", vnfProfileId, nsLvl.getNsLevelId())
          .getMessage();
      throw new NotExistingEntityException(message);
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
      String message = MessageFormatter
          .format("vnfdId='{}' not found in nsd='{}'.", vnfdId, nsd.getNsdIdentifier())
          .getMessage();
      throw new NotExistingEntityException(message);
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
      String message = MessageFormatter
          .format("vlDescId='{}' not found in nsd='{}'.", vlDescId, nsd.getNsdIdentifier())
          .getMessage();
      throw new NotExistingEntityException(message);
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
      String message = MessageFormatter
          .format("vlProfileId='{}' not found in nsLvl='{}'.", vlProfileId, nsLvl.getNsLevelId())
          .getMessage();
      throw new NotExistingEntityException(message);
    }
    return vlLvlMap;
  }

  private void addVnf(Nsd nsd, NsDf nsDf, NsLevel nsLevel, VnfProfile vnfProfile,
      VnfToLevelMapping vnfLvlMap) {
    if (nsd.getVnfdId().stream().noneMatch(id -> id.equals(vnfProfile.getVnfdId()))) {
      nsd.getVnfdId().add(vnfProfile.getVnfdId());
    }
    if (nsDf.getVnfProfile().stream()
        .noneMatch(vp -> vp.getVnfProfileId().equals(vnfProfile.getVnfProfileId()))) {
      nsDf.getVnfProfile().add(vnfProfile);
    }
    if (nsLevel.getVnfToLevelMapping().stream()
        .noneMatch(lm -> lm.getVnfProfileId().equals(vnfProfile.getVnfProfileId()))) {
      nsLevel.getVnfToLevelMapping().add(vnfLvlMap);
    }
  }

  private void addVirtualLink(Nsd nsd, NsDf nsDf, NsLevel nsLevel, NsVirtualLinkDesc vlDesc,
      VirtualLinkProfile vlProfile, VirtualLinkToLevelMapping vlMap) {
    if (nsd.getVirtualLinkDesc().stream()
        .noneMatch(nsdVld -> nsdVld.getVirtualLinkDescId().equals(vlDesc.getVirtualLinkDescId()))) {
      nsd.getVirtualLinkDesc().add(vlDesc);
    }
    if (nsDf.getVirtualLinkProfile().stream().noneMatch(
        nsdfVlP -> nsdfVlP.getVirtualLinkProfileId().equals(vlProfile.getVirtualLinkProfileId()))) {
      nsDf.getVirtualLinkProfile().add(vlProfile);
    }
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
          log.debug("Nsd after:\n{}", objectMapper.writeValueAsString(vsNsd));
          for (VnfConnection ctxC : ctxComposeInfo.getCtxConnections()) {
            // Retrieve the VNF from context Nsd
            VnfWrapper vnfWrapper;
            try {
              vnfWrapper = retrieveVnfInfo(ctxC.getVnfProfileId(), ctxC.getCpdId(),
                  ctxNsd, ctxNsDf,ctxNsLvl);
            } catch (VnfNotFoundInLvlMapping e) {
              log.warn(e.getMessage() + " Skip.");
              continue;
            } catch (InvalidNsd | InvalidCtxComposeInfo e){
              log.error(e.getMessage());
              throw e;
            }

            // Retrieve the VirtualLink from context Nsd
            VirtualLinkToLevelMapping vlMap;
            try {
              vlMap = getVlLvlMapping(ctxC.getVlProfileId(), ctxNsLvl);
            } catch (NotExistingEntityException e) {
              log.warn(e.getMessage() + " Skip.");
              continue;
            }
            VirtualLinkProfile vlProfile;
            try {
              vlProfile = getVlProfile(ctxC.getVlProfileId(), ctxNsDf);
            } catch (NotExistingEntityException e) {
              log.error(e.getMessage());
              throw new InvalidNsd(e.getMessage());
            }
            NsVirtualLinkDesc vlDesc;
            try {
              vlDesc = getVlDescriptor(vnfWrapper.getVnfProfile().getVnfdId(), ctxNsd);
            } catch (NotExistingEntityException e) {
              log.error(e.getMessage());
              throw new InvalidNsd(e.getMessage());
            }
            addVirtualLink(vsNsd, vsNsDf, vsNsLvl, vlDesc, vlProfile, vlMap);

            // Retrieve the VirtualLink from vertical service Nsd
            try {
              vlMap = getVlLvlMapping(ctxC.getVlProfileId(), vsNsLvl);
            } catch (NotExistingEntityException e) {
              log.warn(e.getMessage() + " Skip.");
              continue;
            }
            try {
              vlProfile = getVlProfile(ctxC.getVlProfileId(), vsNsDf);
            } catch (NotExistingEntityException e) {
              log.error(e.getMessage());
              throw new InvalidNsd(e.getMessage());
            }
            try {
              vlDesc = getVlDescriptor(vnfWrapper.getVnfProfile().getVnfdId(), vsNsd);
            } catch (NotExistingEntityException e) {
              log.error(e.getMessage());
              throw new InvalidNsd(e.getMessage());
            }
            // All ok but no need to add virtual link.

//            try {
//              getVlLvlMapping(ctxC.getVlProfileId(), vsNsLvl);
//              log.info("vlProfileId='{}' found in vertical NsLevelId='{}'.",
//                  ctxC.getVlProfileId(), vsNsLvl.getNsLevelId());
//              try {
//                vlProfile = getVlProfile(ctxC.getVlProfileId(), vsNsDf);
//              } catch (NotExistingEntityException e) {
//                log.error(e.getMessage());
//                throw new InvalidNsd(e.getMessage());
//              }
//            } catch (NotExistingEntityException e) {
//              log.warn(e.getMessage() + " Trying in context.");
//              try {
//                VirtualLinkToLevelMapping vlMap = getVlLvlMapping(ctxC.getVlProfileId(), ctxNsLvl);
//                vlProfile = getVlProfile(ctxC.getVlProfileId(), ctxNsDf);
//                NsVirtualLinkDesc vlDesc = getVlDescriptor(vlProfile.getVirtualLinkDescId(),
//                    ctxNsd);
//                log.info("vlProfileId='{}' found in context. Adding to vertical Nsd.",
//                    ctxC.getVlProfileId());
//              } catch (NotExistingEntityException ex) {
//                log.warn(ex.getMessage() + " Skip.");
//                continue;
//              }
//            }
            vnfWrapper.getVlConnectivity().setVirtualLinkProfileId(vlProfile.getVirtualLinkProfileId());
            // TODO change method to get a VnfWrapper
            addVnf(vsNsd, vsNsDf, vsNsLvl, vnfWrapper.getVnfProfile(), vnfWrapper.getVnfToLevelMapping());
          }
          for (VnfConnection vsC : ctxComposeInfo.getVsConnections()) {
            // Retrieve the VNF from vertical service Nsd
            // TODO use VnfWrapper here.
            try {
              getVnfLvlMapping(vsC.getVnfProfileId(), vsNsLvl);
            } catch (NotExistingEntityException e) {
              log.warn(e.getMessage() + " Skip.");
              continue;
            }
            VnfProfile vnfProfile;
            try {
              vnfProfile = getVnfProfile(vsC.getVnfProfileId(), vsNsDf);
            } catch (NotExistingEntityException e) {
              log.error(e.getMessage());
              throw new InvalidNsd(e.getMessage());
            }
            NsVirtualLinkConnectivity vlC;
            try {
              vlC = getVlConnectivity(vsC.getCpdId(), vnfProfile);
            } catch (NotExistingEntityException e) {
              log.error(e.getMessage());
              throw new InvalidCtxComposeInfo(e.getMessage());
            }

            // Retrieve the VirtualLink from context Nsd
            VirtualLinkToLevelMapping vlMap;
            try {
              vlMap = getVlLvlMapping(vsC.getVlProfileId(), ctxNsLvl);
            } catch (NotExistingEntityException e) {
              log.warn(e.getMessage() + " Skip.");
              continue;
            }
            VirtualLinkProfile vlProfile;
            try {
              vlProfile = getVlProfile(vsC.getVlProfileId(), ctxNsDf);
            } catch (NotExistingEntityException e) {
              log.error(e.getMessage());
              throw new InvalidNsd(e.getMessage());
            }
            NsVirtualLinkDesc vlDesc;
            try {
              vlDesc = getVlDescriptor(vnfProfile.getVnfdId(), ctxNsd);
            } catch (NotExistingEntityException e) {
              log.error(e.getMessage());
              throw new InvalidNsd(e.getMessage());
            }

            // Update vertical service Nsd
            addVirtualLink(vsNsd, vsNsDf, vsNsLvl, vlDesc, vlProfile, vlMap);
            vlC.setVirtualLinkProfileId(vlProfile.getVirtualLinkProfileId());
          }
          g = nsdGraphService.buildGraph(vsNsd.getSapd(), vsNsDf, vsNsLvl);
          log.debug("Graph export after:\n{}", nsdGraphService.export(g));
          try {
            vsNsd.isValid();
          } catch (MalformattedElementException e) {
            String message = "Nsd looks not valid after composition";
            log.error(message, e);
            throw new InvalidNsd(message);
          }
        }
      }
    }
  }

}
