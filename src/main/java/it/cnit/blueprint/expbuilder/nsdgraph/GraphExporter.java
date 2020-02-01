package it.cnit.blueprint.expbuilder.nsdgraph;

import org.jgrapht.Graph;
import org.springframework.stereotype.Component;

@Component
public interface GraphExporter {

  String export(Graph<ProfileVertex, String> g);

}
