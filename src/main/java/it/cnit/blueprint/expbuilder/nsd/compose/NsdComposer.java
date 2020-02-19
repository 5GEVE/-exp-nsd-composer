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
import javax.swing.text.html.Option;
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

  // TODO check exceptions of the private methods.

  private VnfProfile getVnfProfile(String vnfProfileId, NsDf nsDf) throws InvalidCtxComposeInfo {
    VnfProfile vnfProfile;
    try {
      vnfProfile = nsDf.getVnfProfile(vnfProfileId);
    } catch (NotExistingEntityException e) {
      String message = MessageFormatter
          .format("VnfProfile='{}' not found in nsDf='{}'", vnfProfileId, nsDf.getNsDfId())
          .getMessage();
      log.error(message);
      throw new InvalidCtxComposeInfo(message);
    }
    return vnfProfile;
  }

  private VirtualLinkProfile getVlProfile(String vlProfileId, NsDf nsDf)
      throws InvalidCtxComposeInfo {
    VirtualLinkProfile vlProfile;
    try {
      vlProfile = nsDf.getVirtualLinkProfile(vlProfileId);
    } catch (NotExistingEntityException e) {
      String message = MessageFormatter
          .format("VirtualLinkProfile='{}' not found in nsDf='{}'", vlProfileId, nsDf.getNsDfId())
          .getMessage();
      log.error(message);
      throw new InvalidCtxComposeInfo(message);
    }
    return vlProfile;
  }

  private NsVirtualLinkConnectivity getVlConnectivity(String cpdId, VnfProfile vnfProfile)
      throws InvalidCtxComposeInfo {
    NsVirtualLinkConnectivity nsVlC;
    Optional<NsVirtualLinkConnectivity> optVlC = vnfProfile.getNsVirtualLinkConnectivity()
        .stream().filter(vlc -> vlc.getCpdId().get(0).equals(cpdId)).findFirst();
    if (optVlC.isPresent()) {
      nsVlC = optVlC.get();
    } else {
      String message = MessageFormatter
          .format("NsVirtualLinkConnectivity for cpdId='{}' not found in vnfProfile='{}'",
              cpdId, vnfProfile.getVnfProfileId()).getMessage();
      log.error(message);
      throw new InvalidCtxComposeInfo(message);
    }
    return nsVlC;
  }

  private VnfToLevelMapping getVnfLvlMapping(String vnfProfileId, NsLevel nsLvl)
      throws InvalidCtxComposeInfo {
    VnfToLevelMapping vnfLvlMap;
    Optional<VnfToLevelMapping> optVnfLvlMap = nsLvl.getVnfToLevelMapping().stream()
        .filter(m -> m.getVnfProfileId().equals(vnfProfileId)).findFirst();
    if (optVnfLvlMap.isPresent()) {
      vnfLvlMap = optVnfLvlMap.get();
    } else {
      throw new InvalidCtxComposeInfo("a");
    }
    return vnfLvlMap;
  }

  private NsVirtualLinkDesc getVlDescriptor(String vlDescId, Nsd nsd) throws InvalidCtxComposeInfo {
    NsVirtualLinkDesc vlDesc;
    Optional<NsVirtualLinkDesc> optVlDesc = nsd.getVirtualLinkDesc().stream()
        .filter(nsdVlDesc -> nsdVlDesc.getVirtualLinkDescId().equals(vlDescId)).findFirst();
    if (optVlDesc.isPresent()) {
      vlDesc = optVlDesc.get();
    } else {
      throw new InvalidCtxComposeInfo("a");
    }
    return vlDesc;
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
          // TODO cycle through connections and modify the nsd
          for (VnfConnection c : ctxComposeInfo.getCtxConnections()) {
            VnfProfile vnfProfile = getVnfProfile(c.getVnfProfileId(), ctxNsDf);
            VnfToLevelMapping vnfLvlMap = getVnfLvlMapping(c.getVnfProfileId(), ctxNsLvl);
            NsVirtualLinkConnectivity vlC = getVlConnectivity(c.getCpdId(), vnfProfile);
            VirtualLinkProfile vlProfile;
            try {
              vlProfile = vsNsDf.getVirtualLinkProfile(c.getVlProfileId());
            } catch (NotExistingEntityException e) {
              try {
                vlProfile = ctxNsDf.getVirtualLinkProfile(c.getVlProfileId());
                NsVirtualLinkDesc vlDesc = getVlDescriptor(vlProfile.getVirtualLinkDescId(),
                    ctxNsd);
                // TODO add VL because it comes from the context.
              } catch (NotExistingEntityException ex) {
                continue;
              }
            }
            vlC.setVirtualLinkProfileId(vlProfile.getVirtualLinkProfileId());
            addVnf(vsNsd, vsNsDf, vsNsLvl, vnfProfile, vnfLvlMap);
          }
          // TODO cycle on vsConnections()
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
