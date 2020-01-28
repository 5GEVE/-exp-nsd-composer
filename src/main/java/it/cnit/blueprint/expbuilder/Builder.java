package it.cnit.blueprint.expbuilder;

import it.cnit.blueprint.expbuilder.nsdgraph.NsdGraph;
import it.cnit.blueprint.expbuilder.nsdgraph.UserMock;
import it.cnit.blueprint.expbuilder.nsdgraph.VirtualLinkProfileVertex;
import it.cnit.blueprint.expbuilder.nsdgraph.VnfProfileVertex;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.NotExistingEntityException;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Nsd;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Builder {

  private static Logger LOG = LoggerFactory
      .getLogger(MethodHandles.lookup().lookupClass().getSimpleName());
  private final NsdGraph verticalNsdGraph;
  private final List<NsdGraph> contextNsdGraphs;

  public Builder(NsdGraph verticalNsdGraph, List<NsdGraph> contextNsdGraphs) {
    this.verticalNsdGraph = verticalNsdGraph;
    this.contextNsdGraphs = contextNsdGraphs;
  }

  public Builder(Nsd verticalNsd, List<Nsd> contextNsds) throws NotExistingEntityException {
    this.verticalNsdGraph = new NsdGraph(verticalNsd, "nsDfId", "nsLevelId");
    this.contextNsdGraphs = new ArrayList<>();
    for (Nsd c : contextNsds) {
      this.contextNsdGraphs.add(new NsdGraph(c, "nsDfId", "nsLevelId"));
    }
  }

  public NsdGraph buildExperiment(CompositionStrat strat) throws NotExistingEntityException {
    NsdGraph expNsdGraph = new NsdGraph(verticalNsdGraph.getNsd(), "nsDfId", "nsLevelId");
    for (NsdGraph c : contextNsdGraphs) {
      // TODO compositionStrategy should depend on the context.
      if (strat == CompositionStrat.CONNECT) {
        for (VnfProfileVertex vnfP : c.getVnfPVertices()) {
          // TODO ask user to select a VitualLinkProfileVertex
          VirtualLinkProfileVertex vlP;
          if (vnfP.getId().contains("Src")) {
            vlP = UserMock.getVLPVertex1(expNsdGraph);
          } else { //if (vnfP.getProfileId().contains("Dst")) {
            vlP = UserMock.getVLPVertex2(expNsdGraph);
          }
          expNsdGraph.addVnfProfileVertex(vnfP, vlP);
        }
      } else if (strat == CompositionStrat.PASS) {
        boolean first = true;
        for (VnfProfileVertex vnfP : c.getVnfPVertices()) {
          // TODO ask user to select an edge
          String e = "";
          if (first) {
            e = UserMock.getEdge1(expNsdGraph);
            first = false;
          } else {
            e = UserMock.getEdge2(expNsdGraph);
          }
          expNsdGraph.addVnfProfileVertex(vnfP, e);
        }
      } else {
        throw new UnsupportedOperationException("Not Implemented yet.");
      }
    }
    return expNsdGraph;
  }

  public NsdGraph getVerticalNsdGraph() {
    return verticalNsdGraph;
  }

  public List<NsdGraph> getContextNsdGraphs() {
    return contextNsdGraphs;
  }

  public String toString() {
    StringBuilder b = new StringBuilder();
    b.append("Builder. Nsd: ").append(verticalNsdGraph.getNsd().getNsdIdentifier()).append(".");
    b.append(" Contexts: ");
    contextNsdGraphs.forEach(c -> b.append(c.getNsd().getNsdIdentifier()));
    return b.toString();
  }

  public enum CompositionStrat {
    CONNECT,
    PASS
  }
}
