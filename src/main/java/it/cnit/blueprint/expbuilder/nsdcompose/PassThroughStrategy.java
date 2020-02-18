package it.cnit.blueprint.expbuilder.nsdcompose;

import it.cnit.blueprint.expbuilder.rest.NsdComposer.CompositionStrat;
import it.cnit.blueprint.expbuilder.rest.CtxComposeInfo;
import it.cnit.blueprint.expbuilder.rest.InvalidCtxComposeInfo;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsDf;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsLevel;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Nsd;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Qualifier("passthrough")
@Slf4j
public class PassThroughStrategy implements CompositionStrategy {

  @Override
  public void compose(Nsd nsd, NsDf nsDf, NsLevel nsLevel, CtxComposeInfo composeInfo)
      throws InvalidCtxComposeInfo {
    if (composeInfo.getStrat() != CompositionStrat.PASSTHROUGH) {
      throw new InvalidCtxComposeInfo("Composition strategy is not 'PASSTHROUGH'");
    }
    if (composeInfo.getSapId() == null) {
      throw new InvalidCtxComposeInfo("Field 'sapId' is empty");
    }
    // TODO handle other exceptions here

    log.info("Compose '{}' with '{}' for nsDfId '{}' and nsLevelId '{}' using PASSTHROUGH",
        nsd.getNsdIdentifier(), composeInfo.getNsd().getNsdIdentifier(), nsDf.getNsDfId(),
        nsLevel.getNsLevelId());

    // TODO update Nsd model when modifying the graph.

    // TODO remove this code
    // Get information on vertices and edges
//    ProfileVertex sapV = graph.vertexSet().stream()
//        .filter(v -> v.getElementId().equals(composeInfo.getSapId())).findAny().get();
//    ProfileVertex vlV = Graphs.neighborListOf(graph, sapV).get(0);
//    String edgeOld = graph.getEdge(sapV, vlV);
//
//    // Create new vertices to add
//    // We assume only one Df and one InstantiationLevel for contexts (one graph)
//    VnfProfileVertex contextV = new VnfProfileVertex(
//        composeInfo.getNsd().getNsDf().get(0).getVnfProfile().get(0));
//    VirtualLinkProfileVertex vlVnew = null;
//    try {
//      vlVnew = new VirtualLinkProfileVertex(
//          new VirtualLinkProfile(
//              nsd.getNsDeploymentFlavour(nsDfId),
//              "vl_profile_" + contextV.getVnfProfile().getVnfProfileId(),
//              "vl_" + contextV.getVnfProfile().getVnfProfileId(),
//              "vl_df_" + contextV.getVnfProfile().getVnfProfileId(), null, null,
//              new LinkBitrateRequirements("1", "1"), new LinkBitrateRequirements("1", "1")));
//    } catch (NotExistingEntityException e) {
//      e.printStackTrace();
//    }
//
//    // Add vertices
//    graph.addVertex(contextV);
//    graph.addVertex(vlVnew);
//
//    // Modify edges
//    graph.addEdge(sapV, vlVnew, edgeOld + "_new");
//    graph.addEdge(vlVnew, contextV,
//        String.format("cp_%s_in", contextV.getVnfProfile().getVnfProfileId()));
//    graph.addEdge(contextV, vlV,
//        String.format("cp_%s_out", contextV.getVnfProfile().getVnfProfileId()));
//    graph.removeEdge(edgeOld);

  }
}
