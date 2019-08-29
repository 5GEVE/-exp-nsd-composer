package eu._5geve.experiment;

import eu._5geve.experiment.nsdgraph.NsdGraph;
import eu._5geve.experiment.nsdgraph.VirtualLinkProfileVertex;
import it.nextworks.nfvmano.libs.descriptors.nsd.Nsd;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
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

  public Builder(Nsd verticalNsd, List<Nsd> contextNsds) {
    this.verticalNsdGraph = new NsdGraph(verticalNsd);
    this.contextNsdGraphs = new ArrayList<>();
    contextNsds.forEach(c -> this.contextNsdGraphs.add(new NsdGraph(c)));
  }

  static <E> void permK(List<E> p, int i, int k, List<List<E>> out) {
    if (i == k) {
      List<E> l = new ArrayList<>(p.subList(0, k));
      out.add(l);
    } else {
      for (int j = i; j < p.size(); j++) {
        Collections.swap(p, i, j);
        permK(p, i + 1, k, out);
        Collections.swap(p, i, j);
      }
    }
  }

  public List<NsdGraph> buildExperiments() {
    // Creating just one experiment (simple case)
    NsdGraph expNsdGraph = new NsdGraph(verticalNsdGraph.getNsd());

    // context with 2 elements. Compute k-permutations of virtual links
    List<List<VirtualLinkProfileVertex>> perm = new ArrayList<>();
    permK(expNsdGraph.getVlPVertices(), 0, 2, perm);
    perm.forEach(
        p -> LOG.info("[" + p.get(0).getProfileId() + "," + p.get(1).getProfileId() + "]"));

    return new ArrayList<>();
  }

  public NsdGraph getVerticalNsdGraph() {
    return verticalNsdGraph;
  }

  public List<NsdGraph> getContextNsdGraphs() {
    return contextNsdGraphs;
  }

  public String toString() {
    StringBuilder b = new StringBuilder();
    b.append("Builder. Nsd: " + verticalNsdGraph.getNsd().getNsdIdentifier() + ".");
    b.append(" Contexts: ");
    contextNsdGraphs.forEach(c -> b.append(c.getNsd().getNsdIdentifier()));
    return b.toString();
  }
}
