package eu._5geve.experiment.vsbgraph;

import it.nextworks.nfvmano.catalogue.blueprint.elements.VsBlueprint;
import it.nextworks.nfvmano.catalogue.blueprint.elements.VsComponent;
import it.nextworks.nfvmano.catalogue.blueprint.elements.VsbLink;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jgrapht.Graph;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.io.Attribute;
import org.jgrapht.io.AttributeType;
import org.jgrapht.io.ComponentAttributeProvider;
import org.jgrapht.io.ComponentNameProvider;
import org.jgrapht.io.DOTExporter;
import org.jgrapht.io.DefaultAttribute;
import org.jgrapht.io.ExportException;
import org.jgrapht.io.GraphMLExporter;
import org.jgrapht.io.IntegerComponentNameProvider;
import org.jgrapht.io.StringComponentNameProvider;

public class VsbGraph {

  List<eu._5geve.experiment.vsbgraph.AtomicComponentVertex> aCVertices = new ArrayList<>();
  List<eu._5geve.experiment.vsbgraph.VsbLinkVertex> vLVertices = new ArrayList<>();
  private VsBlueprint vsB;
  private Graph<eu._5geve.experiment.vsbgraph.VsbVertex, String> g;

  public VsbGraph(VsBlueprint vsB) {
    this.vsB = vsB;
    this.g = new SimpleGraph<>(String.class);

    // vertices
    for (VsComponent vsc : vsB.getAtomicComponents()) {
      eu._5geve.experiment.vsbgraph.AtomicComponentVertex v = new eu._5geve.experiment.vsbgraph.AtomicComponentVertex(vsc);
      aCVertices.add(v);
      g.addVertex(v);
    }
    for (VsbLink vsl : vsB.getConnectivityServices()) {
      eu._5geve.experiment.vsbgraph.VsbLinkVertex v = new eu._5geve.experiment.vsbgraph.VsbLinkVertex(vsl);
      vLVertices.add(v);
      g.addVertex(v);
    }

    // edges
    for (eu._5geve.experiment.vsbgraph.AtomicComponentVertex v1 : aCVertices) {
      for (String vscEp : v1.getVsComponent().getEndPointsIds()) {
        for (eu._5geve.experiment.vsbgraph.VsbLinkVertex v2 : vLVertices) {
          for (String vslEp : v2.getVsbLink().getEndPointIds()) {
            if (vscEp.equals(vslEp)) {
              g.addEdge(v1, v2, vslEp);
            }
          }
        }
      }
    }
  }

  public String exportGraphViz() throws ExportException {
    ComponentNameProvider<eu._5geve.experiment.vsbgraph.VsbVertex> vertexIdProvider = eu._5geve.experiment.vsbgraph.VsbVertex::getId;
    ComponentNameProvider<eu._5geve.experiment.vsbgraph.VsbVertex> vertexLabelProvider = eu._5geve.experiment.vsbgraph.VsbVertex::toString;
    ComponentAttributeProvider<eu._5geve.experiment.vsbgraph.VsbVertex> vertexAttributeProvider = v -> {
      Map<String, Attribute> map = new LinkedHashMap<>();
      if (v instanceof eu._5geve.experiment.vsbgraph.VsbLinkVertex) {
        map.put("shape", DefaultAttribute.createAttribute("oval"));
        map.put("style", DefaultAttribute.createAttribute("filled"));
        map.put("fillcolor", DefaultAttribute.createAttribute("dodgerblue"));
      } else if (v instanceof eu._5geve.experiment.vsbgraph.AtomicComponentVertex) {
        map.put("shape", DefaultAttribute.createAttribute("box"));
        map.put("style", DefaultAttribute.createAttribute("filled"));
        map.put("fillcolor", DefaultAttribute.createAttribute("yellowgreen"));
      } else {
        map = null;
      }
      return map;
    };
    DOTExporter<eu._5geve.experiment.vsbgraph.VsbVertex, String> exporter = new DOTExporter<>(vertexIdProvider,
        vertexLabelProvider, new StringComponentNameProvider<>(), vertexAttributeProvider, null);
    exporter.putGraphAttribute("splines", "false");
    Writer writer = new StringWriter();
    exporter.exportGraph(g, writer);
    return writer.toString();
  }

  public String exportGraphML() throws ExportException {
    ComponentNameProvider<eu._5geve.experiment.vsbgraph.VsbVertex> vertexIdProvider = eu._5geve.experiment.vsbgraph.VsbVertex::getId;
    ComponentNameProvider<eu._5geve.experiment.vsbgraph.VsbVertex> vertexLabelProvider = eu._5geve.experiment.vsbgraph.VsbVertex::toString;
    ComponentAttributeProvider<eu._5geve.experiment.vsbgraph.VsbVertex> vertexAttributeProvider = v -> {
      Map<String, Attribute> map = new LinkedHashMap<>();
      if (v instanceof eu._5geve.experiment.vsbgraph.VsbLinkVertex) {
        map.put("color", DefaultAttribute.createAttribute("blue"));
      } else if (v instanceof eu._5geve.experiment.vsbgraph.AtomicComponentVertex) {
        map.put("color", DefaultAttribute.createAttribute("yellow"));
      } else {
        map = null;
      }
      return map;
    };
    GraphMLExporter<eu._5geve.experiment.vsbgraph.VsbVertex, String> exporter = new GraphMLExporter<>(vertexIdProvider,
        vertexLabelProvider, vertexAttributeProvider, new IntegerComponentNameProvider<>(),
        new StringComponentNameProvider<>(), null);
    exporter
        .registerAttribute("color", GraphMLExporter.AttributeCategory.NODE, AttributeType.STRING,
            "yellow");
    Writer writer = new StringWriter();
    exporter.exportGraph(g, writer);
    return writer.toString();
  }

  public VsBlueprint getVsB() {
    return vsB;
  }

  public Graph<eu._5geve.experiment.vsbgraph.VsbVertex, String> getG() {
    return g;
  }

  public List<eu._5geve.experiment.vsbgraph.AtomicComponentVertex> getaCVertices() {
    return aCVertices;
  }

  public List<eu._5geve.experiment.vsbgraph.VsbLinkVertex> getvLVertices() {
    return vLVertices;
  }
}
