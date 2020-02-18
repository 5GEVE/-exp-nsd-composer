package it.cnit.blueprint.expbuilder.nsd.graph;

import java.io.StringWriter;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;
import org.jgrapht.Graph;
import org.jgrapht.io.Attribute;
import org.jgrapht.io.ComponentAttributeProvider;
import org.jgrapht.io.ComponentNameProvider;
import org.jgrapht.io.DOTExporter;
import org.jgrapht.io.DefaultAttribute;
import org.jgrapht.io.StringComponentNameProvider;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class GraphVizExporter implements GraphExporter {

  @Override
  public String export(Graph<ProfileVertex, String> g) {
    ComponentNameProvider<ProfileVertex> vertexIdProvider = ProfileVertex::getVertexId;
    ComponentNameProvider<ProfileVertex> vertexLabelProvider = ProfileVertex::toString;
    ComponentAttributeProvider<ProfileVertex> vertexAttributeProvider = v -> {
      Map<String, Attribute> map = new LinkedHashMap<>();
      if (v instanceof VirtualLinkProfileVertex) {
        map.put("shape", DefaultAttribute.createAttribute("oval"));
        map.put("style", DefaultAttribute.createAttribute("filled"));
        map.put("fillcolor", DefaultAttribute.createAttribute("dodgerblue"));
      } else if (v instanceof VnfProfileVertex
          || v instanceof PnfProfileVertex) {
        map.put("shape", DefaultAttribute.createAttribute("box"));
        map.put("style", DefaultAttribute.createAttribute("filled"));
        map.put("fillcolor", DefaultAttribute.createAttribute("yellowgreen"));
      } else if (v instanceof SapVertex) {
        map.put("shape", DefaultAttribute.createAttribute("oval"));
        map.put("style", DefaultAttribute.createAttribute("filled"));
        map.put("fillcolor", DefaultAttribute.createAttribute("darksalmon"));
      } else {
        map = null;
      }
      return map;
    };
    DOTExporter<ProfileVertex, String> exporter = new DOTExporter<>(
        vertexIdProvider,
        vertexLabelProvider, new StringComponentNameProvider<>(), vertexAttributeProvider, null);
    exporter.putGraphAttribute("splines", "false");
    exporter.putGraphAttribute("overlap", "false");
    exporter.putGraphAttribute("mindist", "2.0");
    Writer writer = new StringWriter();
    exporter.exportGraph(g, writer);
    return writer.toString();
  }
}
