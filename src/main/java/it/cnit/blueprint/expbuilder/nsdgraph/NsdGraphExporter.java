package it.cnit.blueprint.expbuilder.nsdgraph;

import org.jgrapht.Graph;

public interface NsdGraphExporter {

  String export(Graph<ProfileVertex, String> g);

}
