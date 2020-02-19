package it.cnit.blueprint.expbuilder.nsd.compose;

import it.cnit.blueprint.expbuilder.nsd.compose.NsdComposer.CompositionStrat;
import it.cnit.blueprint.expbuilder.rest.CtxComposeInfo;
import it.cnit.blueprint.expbuilder.rest.InvalidCtxComposeInfo;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.NotExistingEntityException;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsDf;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsLevel;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Nsd;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.VnfProfile;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.VnfToLevelMapping;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Qualifier("connect")
@Slf4j
public class ConnectStrategy extends CompositionStrategy {

  @Override
  public void compose(Nsd nsd, NsDf nsDf, NsLevel nsLevel, CtxComposeInfo composeInfo)
      throws InvalidCtxComposeInfo {
//    if (composeInfo.getStrat() != CompositionStrat.CONNECT) {
//      throw new InvalidCtxComposeInfo("Composition strategy is not 'CONNECT'");
//    }
//    if (composeInfo.getConnections().isEmpty()) {
//      throw new InvalidCtxComposeInfo("Field 'connections' is empty");
//    }
//
//    log.info("Compose '{}' with '{}' for nsDfId='{}' and nsLevelId='{}' using CONNECT",
//        nsd.getNsdIdentifier(), composeInfo.getNsd().getNsdIdentifier(), nsDf.getNsDfId(),
//        nsLevel.getNsLevelId());
//    for (Map.Entry<String, String> connection : composeInfo.getConnections().entrySet()) {
//      // We assume only one DF for the context
//      String ctxVnfProfileId = connection.getKey();
//      VnfProfile ctxVnfProfile;
//      try {
//        ctxVnfProfile = composeInfo.getNsd().getNsDf().get(0).getVnfProfile(ctxVnfProfileId);
//      } catch (NotExistingEntityException e) {
//        String message = MessageFormatter
//            .format("VnfProfile='{}' not found in '{}'. Abort composition.",
//                ctxVnfProfileId, composeInfo.getNsd().getNsdIdentifier()).getMessage();
//        log.error(message);
//        throw new InvalidCtxComposeInfo(message);
//      }
//      String vertVLProfileId = connection.getValue();
//      try {
//        nsDf.getVirtualLinkProfile(vertVLProfileId);
//      } catch (NotExistingEntityException e) {
//        String message = MessageFormatter
//            .format("VirtualLinkProfile='{}' not found in '{}'. Abort composition.",
//                vertVLProfileId, nsd.getNsdIdentifier()).getMessage();
//        log.error(message);
//        throw new InvalidCtxComposeInfo(message);
//      }
//      // Assuming size of NsVirtualLinkConnectivity == 1
//      ctxVnfProfile.getNsVirtualLinkConnectivity().get(0).setVirtualLinkProfileId(vertVLProfileId);
//      // We assume only one IL for the context
//      VnfToLevelMapping ctxVnfLvlMap;
//      List<VnfToLevelMapping> mappings = composeInfo.getNsd().getNsDf().get(0)
//          .getNsInstantiationLevel().get(0).getVnfToLevelMapping();
//      Optional<VnfToLevelMapping> findMap = mappings.stream()
//          .filter(m -> m.getVnfProfileId().equals(ctxVnfProfileId)).findFirst();
//      if (findMap.isPresent()) {
//        ctxVnfLvlMap = findMap.get();
//      } else {
//        String message = MessageFormatter
//            .format("VnfToLevelMapping for VnfProfile='{}' not found in '{}'. Abort composition.",
//                connection.getKey(), composeInfo.getNsd().getNsdIdentifier()).getMessage();
//        log.error(message);
//        throw new InvalidCtxComposeInfo(message);
//      }
//
//      // Update Nsd
//      addVnf(nsd, nsDf, nsLevel, ctxVnfProfile, ctxVnfLvlMap);
      //TODO check vnf connection to VL

      // Update Graph
      // TODO remove this code
//      VnfProfileVertex ctxVnfVertex = new VnfProfileVertex(ctxVnfProfile);
//      Optional<ProfileVertex> findVl = graph.vertexSet().stream()
//          .filter(v -> v.getElementId().equals(connection.getValue())).findAny();
//      ProfileVertex serviceVl;
//      if (findVl.isPresent()) {
//        serviceVl = findVl.get();
//      } else {
//        String message = MessageFormatter.arrayFormat(
//            "Virtual Link '{}' not found in '{}' for nsDfId='{}' and nsLevelId='{}'. Abort composition.",
//            new String[]{connection.getValue(), nsd.getNsdIdentifier(), nsDfId, nsLevelId})
//            .getMessage();
//        log.error(message);
//        throw new InvalidCtxComposeInfo(message);
//      }
//      graph.addVertex(ctxVnfVertex);
//      graph.addEdge(ctxVnfVertex, serviceVl);
//    }
  }

}
