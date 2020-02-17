package it.cnit.blueprint.expbuilder.compose;

import it.cnit.blueprint.expbuilder.nsdgraph.ProfileVertex;
import it.cnit.blueprint.expbuilder.nsdgraph.VirtualLinkProfileVertex;
import it.cnit.blueprint.expbuilder.nsdgraph.VnfProfileVertex;
import it.cnit.blueprint.expbuilder.rest.Composer.CompositionStrat;
import it.cnit.blueprint.expbuilder.rest.CtxComposeInfo;
import it.cnit.blueprint.expbuilder.rest.InvalidCtxComposeInfo;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.NotExistingEntityException;
import it.nextworks.nfvmano.libs.ifa.descriptors.common.elements.LinkBitrateRequirements;
import it.nextworks.nfvmano.libs.ifa.descriptors.common.elements.VirtualLinkProfile;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Nsd;
import lombok.extern.slf4j.Slf4j;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Qualifier("passthrough")
@Slf4j
public class PassThroughStrategy implements CompositionStrategy {

  @Override
  public void compose(Nsd nsd, String nsDfId, String nsLevelId, CtxComposeInfo composeInfo,
      Graph<ProfileVertex, String> graph) throws InvalidCtxComposeInfo {
    if (composeInfo.getStrat() != CompositionStrat.PASSTHROUGH) {
      log.error("Composition strategy is not 'PASSTHROUGH'. Doing nothing.");
      throw new IllegalArgumentException();
    }
    // TODO handle other exceptions here

    log.info("Compose '{}' with '{}' for nsDfId '{}' and nsLevelId '{}' using PASSTHROUGH",
        nsd.getNsdIdentifier(), composeInfo.getNsd().getNsdIdentifier(), nsDfId, nsLevelId);

    // TODO update Nsd model when modifying the graph.

    // Get information on vertices and edges
    ProfileVertex sapV = graph.vertexSet().stream()
        .filter(v -> v.getElementId().equals(composeInfo.getSapId())).findAny().get();
    ProfileVertex vlV = Graphs.neighborListOf(graph, sapV).get(0);
    String edgeOld = graph.getEdge(sapV, vlV);

    // Create new vertices to add
    // We assume only one Df and one InstantiationLevel for contexts (one graph)
    VnfProfileVertex contextV = new VnfProfileVertex(
        composeInfo.getNsd().getNsDf().get(0).getVnfProfile().get(0));
    VirtualLinkProfileVertex vlVnew = null;
    try {
      vlVnew = new VirtualLinkProfileVertex(
          new VirtualLinkProfile(
              nsd.getNsDeploymentFlavour(nsDfId),
              "vl_profile_" + contextV.getVnfProfile().getVnfProfileId(),
              "vl_" + contextV.getVnfProfile().getVnfProfileId(),
              "vl_df_" + contextV.getVnfProfile().getVnfProfileId(), null, null,
              new LinkBitrateRequirements("1", "1"), new LinkBitrateRequirements("1", "1")));
    } catch (NotExistingEntityException e) {
      e.printStackTrace();
    }

    // Add vertices
    graph.addVertex(contextV);
    graph.addVertex(vlVnew);

    // Modify edges
    graph.addEdge(sapV, vlVnew, edgeOld + "_new");
    graph.addEdge(vlVnew, contextV,
        String.format("cp_%s_in", contextV.getVnfProfile().getVnfProfileId()));
    graph.addEdge(contextV, vlV,
        String.format("cp_%s_out", contextV.getVnfProfile().getVnfProfileId()));
    graph.removeEdge(edgeOld);

  }
}
