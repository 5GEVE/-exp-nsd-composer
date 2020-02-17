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
  public void compose(Nsd nsd, Map<DfIlKey, Graph<ProfileVertex, String>> graphMap,
      CtxComposeInfo ctxR) throws InvalidCtxComposeInfo {
    if (nsd == null) {
      throw new IllegalStateException("Can not compose. Nsd has not been set.");
    }
    if (ctxR.getStrat() != CompositionStrat.CONNECT) {
      throw new InvalidCtxComposeInfo("Composition strategy is not 'CONNECT'");
    }
    if (ctxR.getConnections().isEmpty()) {
      throw new InvalidCtxComposeInfo("Field 'connections' is empty");
    }

    for (Map.Entry<DfIlKey, Graph<ProfileVertex, String>> entry : graphMap.entrySet()) {
      log.info("Compose '{}' with '{}' for nsDfId='{}' and nsLevelId='{}' using CONNECT",
          nsd.getNsdIdentifier(), ctxR.getNsd().getNsdIdentifier(), entry.getKey().getNsDfId(),
          entry.getKey().getNsIlId());
//      log.debug("Graph export before:\n{}", export(entry.getKey()));

      for (Map.Entry<String, String> connection : ctxR.getConnections().entrySet()) {
        VnfProfile ctxVnfProfile;
        try {
          ctxVnfProfile = ctxR.getNsd().getNsDf().get(0).getVnfProfile(connection.getKey());
        } catch (NotExistingEntityException e) {
          String message = MessageFormatter
              .format("VnfProfile='{}' not found in '{}'. Abort composition.",
                  connection.getKey(), ctxR.getNsd().getNsdIdentifier()).getMessage();
          log.error(message);
          throw new InvalidCtxComposeInfo(message);
        }
        VnfToLevelMapping ctxVnfLvlMap;
        try {
          List<VnfToLevelMapping> mappings = ctxR.getNsd().getNsDf().get(0)
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
                  connection.getKey(), ctxR.getNsd().getNsdIdentifier()).getMessage();
          log.error(message);
          throw new InvalidCtxComposeInfo(message);
        }
        // Create new vertices to add
        VnfProfileVertex ctxVnfVertex = new VnfProfileVertex(ctxVnfProfile);
        Optional<ProfileVertex> findVl = entry.getValue().vertexSet().stream()
            .filter(v -> v.getElementId().equals(connection.getValue())).findAny();
        ProfileVertex serviceVl;
        if (findVl.isPresent()) {
          serviceVl = findVl.get();
        } else {
          String message = MessageFormatter.arrayFormat(
              "Virtual Link '{}' not found in '{}' for nsDfId='{}' and nsLevelId='{}'. Abort composition.",
              new String[]{connection.getValue(), nsd.getNsdIdentifier(),
                  entry.getKey().getNsDfId(),
                  entry.getKey().getNsIlId()}).getMessage();
          log.error(message);
          throw new InvalidCtxComposeInfo(message);
        }
        entry.getValue().addVertex(ctxVnfVertex);
        // TODO add vnf to nsd
        if (nsd.getVnfdId().stream().noneMatch(id -> id.equals(ctxVnfProfile.getVnfdId()))) {
          nsd.getVnfdId().add(ctxVnfProfile.getVnfdId());
        }
        try {
          if (nsd.getNsDeploymentFlavour(entry.getKey().getNsDfId()).getVnfProfile().stream()
              .noneMatch(vp -> vp.getVnfProfileId().equals(ctxVnfProfile.getVnfProfileId()))) {
            nsd.getNsDeploymentFlavour(entry.getKey().getNsDfId()).getVnfProfile()
                .add(ctxVnfProfile);
          }
          nsd.getNsDeploymentFlavour(entry.getKey().getNsDfId())
              .getNsLevel(entry.getKey().getNsIlId()).getVnfToLevelMapping().add(ctxVnfLvlMap);
        } catch (NotExistingEntityException e) {
          e.printStackTrace();
        }

        entry.getValue().addEdge(ctxVnfVertex, serviceVl);
        // TODO verify edge

      }
//      log.debug("Graph export after:\n{}", export(entry.getKey()));
    }

  }
}
