package it.cnit.blueprint.composer.nsd.graph;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.engine.Renderer;
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
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class GraphVizExporter {

  public String export(Graph<ProfileVertex, String> g) {
    ComponentNameProvider<ProfileVertex> vertexIdProvider = ProfileVertex::getVertexId;
    ComponentNameProvider<ProfileVertex> vertexLabelProvider = ProfileVertex::toString;
    ComponentAttributeProvider<ProfileVertex> vertexAttributeProvider = v -> {
      Map<String, Attribute> map = new LinkedHashMap<>();
      if (v instanceof VirtualLinkProfileVertex) {
        map.put("shape", DefaultAttribute.createAttribute("oval"));
        map.put("style", DefaultAttribute.createAttribute("filled"));
        map.put("fillcolor", DefaultAttribute.createAttribute("#dae8fc"));
        map.put("color", DefaultAttribute.createAttribute("#6c8ebf"));
      } else if (v instanceof VnfProfileVertex
          || v instanceof PnfProfileVertex) {
        map.put("shape", DefaultAttribute.createAttribute("box"));
        map.put("style", DefaultAttribute.createAttribute("filled"));
        map.put("fillcolor", DefaultAttribute.createAttribute("#d5e8d4"));
        map.put("color", DefaultAttribute.createAttribute("#82b366"));
      } else if (v instanceof SapVertex) {
        map.put("shape", DefaultAttribute.createAttribute("circle"));
        map.put("style", DefaultAttribute.createAttribute("filled"));
        map.put("fillcolor", DefaultAttribute.createAttribute("#ffe6cc"));
        map.put("color", DefaultAttribute.createAttribute("#d79b00"));
        map.put("fixedsize", DefaultAttribute.createAttribute("true"));
      } else {
        map = null;
      }
      return map;
    };
    ComponentNameProvider<String> edgeProvider = component -> {
      if (component.toLowerCase().contains("sap")) {
        return "";
      } else {
        return component;
      }
    };
    ComponentAttributeProvider<String> edgeAttributeProvider = v -> {
      Map<String, Attribute> map = new LinkedHashMap<>();
      map.put("color", DefaultAttribute.createAttribute("#8f8f8f"));
      map.put("style", DefaultAttribute.createAttribute("bold"));
      return map;
    };
    DOTExporter<ProfileVertex, String> exporter = new DOTExporter<>(
        vertexIdProvider,
        vertexLabelProvider,
        edgeProvider,
        vertexAttributeProvider,
        edgeAttributeProvider);
    // This controls width
    exporter.putGraphAttribute("nodesep", "1");
    // This controls height
    exporter.putGraphAttribute("ranksep", "3");
    // Curved edges (better space for labels)
    exporter.putGraphAttribute("splines", "true");
    exporter.putGraphAttribute("overlap", "false");
    exporter.putGraphAttribute("mindist", "0.5");
    Writer writer = new StringWriter();
    exporter.exportGraph(g, writer);
    return writer.toString();
  }

  public Renderer renderSVG(String dot) {
    return Graphviz.fromString(dot).render(Format.SVG);
  }

  public Renderer renderPNG(String dot) {
    return Graphviz.fromString(dot).width(1920).render(Format.PNG);
  }

}
