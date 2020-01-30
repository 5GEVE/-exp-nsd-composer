package it.cnit.blueprint.expbuilder.composer;

import it.cnit.blueprint.expbuilder.nsdgraph.NsdGraph;
import it.cnit.blueprint.expbuilder.nsdgraph.ProfileVertex;
import it.cnit.blueprint.expbuilder.nsdgraph.VirtualLinkProfileVertex;
import it.cnit.blueprint.expbuilder.nsdgraph.VnfProfileVertex;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.NotExistingEntityException;
import it.nextworks.nfvmano.libs.ifa.descriptors.common.elements.LinkBitrateRequirements;
import it.nextworks.nfvmano.libs.ifa.descriptors.common.elements.VirtualLinkProfile;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Nsd;
import org.jgrapht.Graphs;

public class CtxComposePassthrough extends CtxCompose {

  private String sapId;

  public CtxComposePassthrough(Nsd ctxNsd, String sapId)
      throws NotExistingEntityException, PassthroughException {
    super(ctxNsd);
    if (this.ctxNsdG.getVnfPVertices().size() != 1) {
      throw new PassthroughException();
    }
    this.sapId = sapId;
  }

  @Override
  public void composeWith(NsdGraph serviceNsdG) {

    // TODO update Nsd model when modifying the graph.

    // Get information on vertices and edges
    ProfileVertex sapV = serviceNsdG.getG().vertexSet().stream()
        .filter(v -> v.getElementId().equals(sapId)).findAny().get();
    ProfileVertex vlV = Graphs.neighborListOf(serviceNsdG.getG(), sapV).get(0);
    String edgeOld = serviceNsdG.getG().getEdge(sapV, vlV);

    // Create new vertices to add
    VnfProfileVertex contextV = this.ctxNsdG.getVnfPVertices().get(0);
    VirtualLinkProfileVertex vlVnew = new VirtualLinkProfileVertex(
        new VirtualLinkProfile(serviceNsdG.getNsDf(),
            "vl_profile_" + contextV.getVnfProfile().getVnfProfileId(),
            "vl_" + contextV.getVnfProfile().getVnfProfileId(),
            "vl_df_" + contextV.getVnfProfile().getVnfProfileId(), null, null,
            new LinkBitrateRequirements("1", "1"), new LinkBitrateRequirements("1", "1")));

    // Add vertices
    serviceNsdG.getVnfPVertices().add(contextV);
    serviceNsdG.getG().addVertex(contextV);
    serviceNsdG.getVlPVertices().add(vlVnew);
    serviceNsdG.getG().addVertex(vlVnew);

    // Modify edges
    serviceNsdG.getG().addEdge(sapV, vlVnew, edgeOld + "_new");
    serviceNsdG.getG().addEdge(vlVnew, contextV,
        String.format("cp_%s_in", contextV.getVnfProfile().getVnfProfileId()));
    serviceNsdG.getG().addEdge(contextV, vlV,
        String.format("cp_%s_out", contextV.getVnfProfile().getVnfProfileId()));
    serviceNsdG.getG().removeEdge(edgeOld);

  }

  public static class PassthroughException extends Exception {

  }

}
