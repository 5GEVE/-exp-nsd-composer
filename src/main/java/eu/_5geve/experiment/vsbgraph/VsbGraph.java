package eu._5geve.experiment.vsbgraph;

import eu._5geve.blueprint.vsb.VsBlueprint;
import eu._5geve.blueprint.vsb.VsComponent;
import eu._5geve.blueprint.vsb.VsbLink;
import eu._5geve.experiment.nsdgraph.ProfileVertex;
import it.nextworks.nfvmano.libs.descriptors.nsd.NsVirtualLinkConnectivity;
import it.nextworks.nfvmano.libs.descriptors.nsd.PnfProfile;
import it.nextworks.nfvmano.libs.descriptors.nsd.VnfProfile;
import java.io.StringWriter;
import java.io.Writer;
import java.util.LinkedHashMap;
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

  private VsBlueprint vsB;
  private Graph<VsbVertex, String> g;

  public VsbGraph(VsBlueprint vsB) {
    this.vsB = vsB;
    this.g = new SimpleGraph<>(String.class);

    // vertices
    for (VsComponent vsc : vsB.getAtomicComponents()) {
      g.addVertex(new AtomicComponentVertex(vsc));
    }
    for (VsbLink vsl : vsB.getConnectivityServices()) {
      g.addVertex(new VsbLinkVertex(vsl));
    }

    // edges
    for (VsbLink vsl : vsB.getConnectivityServices()) {
      for (String vslEp : vsl.getEndPointIds()) {
        for (VsComponent vsc : vsB.getAtomicComponents()) {
          for (String vscEp : vsc.getEndPointsIds()) {
            if (vslEp.equals(vscEp)) {
              VsbVertex v1 = g.vertexSet().stream()
                  .filter(v -> v.getId().equals(vsl.toString())).findAny().get();
              VsbVertex v2 = g.vertexSet().stream()
                  .filter(v -> v.getId().equals(vsc.getComponentId())).findAny().get();
              g.addEdge(v1, v2, vslEp);
            }
          }
        }
      }
    }

  }

  public String exportGraphViz() throws ExportException {
    ComponentNameProvider<VsbVertex> vertexIdProvider = VsbVertex::getId;
    ComponentNameProvider<VsbVertex> vertexLabelProvider = VsbVertex::toString;
    ComponentAttributeProvider<VsbVertex> vertexAttributeProvider = v -> {
      Map<String, Attribute> map = new LinkedHashMap<>();
      if (v.getType().equals("vsbLink")) {
        map.put("shape", DefaultAttribute.createAttribute("oval"));
        map.put("style", DefaultAttribute.createAttribute("filled"));
        map.put("fillcolor", DefaultAttribute.createAttribute("dodgerblue"));
      } else if (v.getType().equals("atomicComponent")) {
        map.put("shape", DefaultAttribute.createAttribute("box"));
        map.put("style", DefaultAttribute.createAttribute("filled"));
        map.put("fillcolor", DefaultAttribute.createAttribute("yellowgreen"));
      } else {
        map = null;
      }
      return map;
    };
    DOTExporter<VsbVertex, String> exporter = new DOTExporter<>(vertexIdProvider,
        vertexLabelProvider, new StringComponentNameProvider<>(), vertexAttributeProvider, null);
    Writer writer = new StringWriter();
    exporter.exportGraph(g, writer);
    return writer.toString();
  }

  public String exportGraphML() throws ExportException {
    ComponentNameProvider<VsbVertex> vertexIdProvider = VsbVertex::getId;
    ComponentNameProvider<VsbVertex> vertexLabelProvider = VsbVertex::toString;
    ComponentAttributeProvider<VsbVertex> vertexAttributeProvider = v -> {
      Map<String, Attribute> map = new LinkedHashMap<>();
      if (v.getType().equals("vsbLink")) {
        map.put("color", DefaultAttribute.createAttribute("blue"));
      } else if (v.getType().equals("atomicComponent")) {
        map.put("color", DefaultAttribute.createAttribute("yellow"));
      } else {
        map = null;
      }
      return map;
    };
    GraphMLExporter<VsbVertex, String> exporter = new GraphMLExporter<>(vertexIdProvider,
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

  public Graph<VsbVertex, String> getG() {
    return g;
  }
}
