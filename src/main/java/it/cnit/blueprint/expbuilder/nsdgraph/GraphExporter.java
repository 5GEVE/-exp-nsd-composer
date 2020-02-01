package it.cnit.blueprint.expbuilder.nsdgraph;

import org.jgrapht.Graph;

public interface GraphExporter {

  String export(Graph<ProfileVertex, String> g);

}
