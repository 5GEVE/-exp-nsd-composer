package it.cnit.blueprint.composer.vsb.graph;

import it.nextworks.nfvmano.catalogue.blueprint.elements.Blueprint;
import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.graph.SimpleGraph;

public class VsbGraphService {

  public Graph<VsbVertex, String> buildGraph(Blueprint b) {
    Graph<VsbVertex, String> g = new SimpleGraph<>(String.class);
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


