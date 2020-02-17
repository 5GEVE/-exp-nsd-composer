package it.cnit.blueprint.expbuilder.compose;

import it.cnit.blueprint.expbuilder.nsdgraph.ProfileVertex;
import it.cnit.blueprint.expbuilder.nsdgraph.VnfProfileVertex;
import it.cnit.blueprint.expbuilder.rest.Composer.CompositionStrat;
import it.cnit.blueprint.expbuilder.rest.Composer.DfIlKey;
import it.cnit.blueprint.expbuilder.rest.CtxComposeInfo;
import it.cnit.blueprint.expbuilder.rest.InvalidCtxComposeInfo;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.NotExistingEntityException;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Nsd;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.VnfProfile;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.VnfToLevelMapping;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.jgrapht.Graph;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Qualifier("connect")
@Slf4j
public class ConnectStrategy implements CompositionStrategy {

  @Override
  public void compose(Nsd nsd, String nsDfId, String nsLevelId, CtxComposeInfo composeInfo,
      Graph<ProfileVertex, String> graph) throws InvalidCtxComposeInfo {
    if (nsd == null) {
      throw new IllegalStateException("Can not compose. Nsd has not been set.");
    }
    if (composeInfo.getStrat() != CompositionStrat.CONNECT) {
      throw new InvalidCtxComposeInfo("Composition strategy is not 'CONNECT'");
    }
    if (composeInfo.getConnections().isEmpty()) {
      throw new InvalidCtxComposeInfo("Field 'connections' is empty");
    }

    log.info("Compose '{}' with '{}' for nsDfId='{}' and nsLevelId='{}' using CONNECT",
        nsd.getNsdIdentifier(), composeInfo.getNsd().getNsdIdentifier(), nsDfId, nsLevelId);
    for (Map.Entry<String, String> connection : composeInfo.getConnections().entrySet()) {
      // Search and prepare components
      VnfProfile ctxVnfProfile;
      try {
        ctxVnfProfile = composeInfo.getNsd().getNsDf().get(0).getVnfProfile(connection.getKey());
      } catch (NotExistingEntityException e) {
        String message = MessageFormatter
            .format("VnfProfile='{}' not found in '{}'. Abort composition.",
                connection.getKey(), composeInfo.getNsd().getNsdIdentifier()).getMessage();
        log.error(message);
        throw new InvalidCtxComposeInfo(message);
      }
      VnfToLevelMapping ctxVnfLvlMap;
      try {
        List<VnfToLevelMapping> mappings = composeInfo.getNsd().getNsDf().get(0)
            .getDefaultInstantiationLevel().getVnfToLevelMapping();
        Optional<VnfToLevelMapping> findMap = mappings.stream()
            .filter(m -> m.getVnfProfileId().equals(ctxVnfProfile.getVnfProfileId())).findFirst();
        if (findMap.isPresent()) {
          ctxVnfLvlMap = findMap.get();
        } else {
          throw new NotExistingEntityException();
        }
      } catch (NotExistingEntityException e) {
        String message = MessageFormatter
            .format("VnfToLevelMapping for VnfProfile='{}' not found in '{}'. Abort composition.",
                connection.getKey(), composeInfo.getNsd().getNsdIdentifier()).getMessage();
        log.error(message);
        throw new InvalidCtxComposeInfo(message);
      }

      // Update Nsd
      try {
        if (nsd.getVnfdId().stream().noneMatch(id -> id.equals(ctxVnfProfile.getVnfdId()))) {
          nsd.getVnfdId().add(ctxVnfProfile.getVnfdId());
        }
        if (nsd.getNsDeploymentFlavour(nsDfId).getVnfProfile().stream()
            .noneMatch(vp -> vp.getVnfProfileId().equals(ctxVnfProfile.getVnfProfileId()))) {
          nsd.getNsDeploymentFlavour(nsDfId).getVnfProfile().add(ctxVnfProfile);
        }
        nsd.getNsDeploymentFlavour(nsDfId).getNsLevel(nsLevelId).getVnfToLevelMapping()
            .add(ctxVnfLvlMap);
      } catch (NotExistingEntityException e) {
        log.error("Error while updating Nsd. This should not happen.", e);
      }
      //TODO check vnf connection to VL

      // Update Graph
      VnfProfileVertex ctxVnfVertex = new VnfProfileVertex(ctxVnfProfile);
      Optional<ProfileVertex> findVl = graph.vertexSet().stream()
          .filter(v -> v.getElementId().equals(connection.getValue())).findAny();
      ProfileVertex serviceVl;
      if (findVl.isPresent()) {
        serviceVl = findVl.get();
      } else {
        String message = MessageFormatter.arrayFormat(
            "Virtual Link '{}' not found in '{}' for nsDfId='{}' and nsLevelId='{}'. Abort composition.",
            new String[]{connection.getValue(), nsd.getNsdIdentifier(), nsDfId, nsLevelId})
            .getMessage();
        log.error(message);
        throw new InvalidCtxComposeInfo(message);
      }
      graph.addVertex(ctxVnfVertex);
      graph.addEdge(ctxVnfVertex, serviceVl);
    }
  }

}
