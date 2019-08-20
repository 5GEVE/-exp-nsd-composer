package eu._5geve.experiment.graph;

import it.nextworks.nfvmano.libs.descriptors.common.elements.VirtualLinkProfile;
import it.nextworks.nfvmano.libs.descriptors.nsd.NsVirtualLinkConnectivity;
import it.nextworks.nfvmano.libs.descriptors.nsd.Nsd;
import it.nextworks.nfvmano.libs.descriptors.nsd.PnfProfile;
import it.nextworks.nfvmano.libs.descriptors.nsd.VnfProfile;
import java.io.StringWriter;
import java.io.Writer;
import org.jgrapht.Graph;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.io.ComponentNameProvider;
import org.jgrapht.io.DOTExporter;
import org.jgrapht.io.ExportException;
import org.jgrapht.io.GraphExporter;

public class NsdGraph {

  private Nsd nsd;
  private Graph<ProfileVertex, String> g;

  public NsdGraph(Nsd nsd) {
    this.nsd = nsd;
    this.g = new SimpleGraph<>(String.class);

    // vertices
    for (VnfProfile vp : nsd.getNsDf().get(0).getVnfProfile()) {
      g.addVertex(new VnfProfileVertex(vp));
    }
    for (PnfProfile pp : nsd.getNsDf().get(0).getPnfProfile()) {
      g.addVertex(new PnfProfileVertex(pp));
    }
    for (VirtualLinkProfile vlp : nsd.getNsDf().get(0).getVirtualLinkProfile()) {
      g.addVertex(new VirtualLinkProfileVertex(vlp));
    }

    // edges
    for (VnfProfile vp : nsd.getNsDf().get(0).getVnfProfile()) {
      for (NsVirtualLinkConnectivity vlc : vp.getNsVirtualLinkConnectivity()) {
        ProfileVertex v1 = g.vertexSet().stream()
            .filter(v -> v.getProfileId().equals(vp.getVnfProfileId())).findAny().get();
        ProfileVertex v2 = g.vertexSet().stream()
            .filter(v -> v.getProfileId().equals(vlc.getVirtualLinkProfileId())).findAny().get();
        g.addEdge(v1, v2, vlc.getCpdId().get(0));
      }
    }
    for (PnfProfile pnfp : nsd.getNsDf().get(0).getPnfProfile()) {
      for (NsVirtualLinkConnectivity vlc : pnfp.getNsVirtualLinkConnectivity()) {
        ProfileVertex v1 = g.vertexSet().stream()
            .filter(v -> v.getProfileId().equals(pnfp.getPnfProfileId())).findAny().get();
        ProfileVertex v2 = g.vertexSet().stream()
            .filter(v -> v.getProfileId().equals(vlc.getVirtualLinkProfileId())).findAny().get();
        g.addEdge(v1, v2, vlc.getCpdId().get(0));
      }
    }
  }

  public String exportGraphViz() throws ExportException {
    ComponentNameProvider<ProfileVertex> vertexIdProvider = ProfileVertex::getProfileId;
    ComponentNameProvider<ProfileVertex> vertexLabelProvider = ProfileVertex::toString;
    GraphExporter<ProfileVertex, String> exporter =
        new DOTExporter<>(vertexIdProvider, vertexLabelProvider, null);
    Writer writer = new StringWriter();
    exporter.exportGraph(g, writer);
    return writer.toString();
  }

  public Nsd getNsd() {
    return nsd;
  }

  public Graph<ProfileVertex, String> getG() {
    return g;
  }
}
