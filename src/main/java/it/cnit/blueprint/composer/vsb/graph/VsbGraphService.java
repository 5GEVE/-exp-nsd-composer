package it.cnit.blueprint.composer.vsb.graph;

import it.nextworks.nfvmano.catalogue.blueprint.elements.Blueprint;
import it.nextworks.nfvmano.catalogue.blueprint.elements.VsComponent;
import it.nextworks.nfvmano.catalogue.blueprint.elements.VsbEndpoint;
import it.nextworks.nfvmano.catalogue.blueprint.elements.VsbLink;
import java.util.ArrayList;
import java.util.List;
import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.graph.SimpleGraph;

public class VsbGraphService {

  public Graph<VsbVertex, String> buildGraph(Blueprint b) {
    Graph<VsbVertex, String> g = new SimpleGraph<>(String.class);
    List<AtomicComponentVertex> compVList = new ArrayList<>();
    List<VsbLinkVertex> linkVList = new ArrayList<>();
    List<VsbSapVertex> sapVList = new ArrayList<>();

    // Vertices
    for (VsComponent c : b.getAtomicComponents()) {
      AtomicComponentVertex v = new AtomicComponentVertex(c);
      // TODO handle 'serversNumber'
      compVList.add(v);
      g.addVertex(v);
    }
    for (VsbLink l : b.getConnectivityServices()) {
      VsbLinkVertex v = new VsbLinkVertex(l);
      linkVList.add(v);
      g.addVertex(v);
    }
    for (VsbEndpoint e : b.getEndPoints()) {
      // An insecure way to determine sap endpoints.
      if (e.isExternal() && e.getEndPointId().toLowerCase().contains("sap")) {
        VsbSapVertex v = new VsbSapVertex(e);
        sapVList.add(v);
        g.addVertex(v);
      }
    }

    // Edges
    for (AtomicComponentVertex v1 : compVList) {
      for (String acEp : v1.getVsComponent().getEndPointsIds()) {
        for (VsbLinkVertex v2 : linkVList) {
          for (String vlEp : v2.getVsbLink().getEndPointIds()) {
            if (!acEp.contains("sap") && !vlEp.contains("sap") && acEp.equals(vlEp)) {
              g.addEdge(v1, v2, vlEp);
            }
          }
        }
      }
    }
    for (VsbSapVertex v1 : sapVList) {
      String sapEp = v1.getVsbEndpoint().getEndPointId();
      for (VsbLinkVertex v2 : linkVList) {
        for (String vlEp : v2.getVsbLink().getEndPointIds()) {
          if (sapEp.equals(vlEp)) {
            g.addEdge(v1, v2, sapEp);
          }
        }
      }
    }
    return g;
  }

  public String export(Graph<VsbVertex, String> graph) {
    return "export";
  }

  public boolean isConnected(Graph<VsbVertex, String> g) {
    ConnectivityInspector<VsbVertex, String> inspector = new ConnectivityInspector<>(g);
    return inspector.isConnected();
  }
}


