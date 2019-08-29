package eu._5geve.experiment;

import eu._5geve.experiment.nsdgraph.NsdGraph;
import it.nextworks.nfvmano.libs.descriptors.nsd.Nsd;
import java.util.ArrayList;
import java.util.List;

public class Builder {

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

  public List<NsdGraph> buildExperiments() {
    return new ArrayList<>();
  }

  public NsdGraph getVerticalNsdGraph() {
    return verticalNsdGraph;
  }

  public List<NsdGraph> getContextNsdGraphs() {
    return contextNsdGraphs;
  }
}
