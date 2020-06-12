package it.cnit.blueprint.composer.vsb.graph;

import it.nextworks.nfvmano.catalogue.blueprint.elements.VsBlueprint;
import it.nextworks.nfvmano.catalogue.blueprint.elements.VsComponent;
import it.nextworks.nfvmano.catalogue.blueprint.elements.VsbEndpoint;
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

//  List<AtomicComponentVertex> aCVertices = new ArrayList<>();
//  List<VsbLinkVertex> vLVertices = new ArrayList<>();
//  List<VsbSapVertex> sapVertices = new ArrayList<>();
//  private final VsBlueprint vsB;
//  private Graph<VsbVertex, String> g;
//
//  public VsbGraph(VsBlueprint vsB) {
//    this.vsB = vsB;
//    this.g = new SimpleGraph<>(String.class);
//
//    // vertices
//    for (VsComponent vsc : vsB.getAtomicComponents()) {
//      AtomicComponentVertex v = new AtomicComponentVertex(vsc);
//      aCVertices.add(v);
//      g.addVertex(v);
//    }
//    for (VsbLink vsl : vsB.getConnectivityServices()) {
//      VsbLinkVertex v = new VsbLinkVertex(vsl);
//      vLVertices.add(v);
//      g.addVertex(v);
//    }
//    // An insecure way to determine sap endpoints. We leave it this way as it is more important to
//    // compose the NSD instead of the VSB.
//    for (VsbEndpoint vse : vsB.getEndPoints()) {
//      if (vse.getEndPointId().contains("sap")) {
//        VsbSapVertex v = new VsbSapVertex(vse);
//        sapVertices.add(v);
//        g.addVertex(v);
//      }
//    }
//
//    // edges
//    for (AtomicComponentVertex v1 : aCVertices) {
//      for (String vscEp : v1.getVsComponent().getEndPointsIds()) {
//        for (VsbLinkVertex v2 : vLVertices) {
//          for (String vslEp : v2.getVsbLink().getEndPointIds()) {
//            if (!vscEp.contains("sap") && !vslEp.contains("sap") && vscEp.equals(vslEp)) {
//              g.addEdge(v1, v2, vslEp);
//            }
//          }
//        }
//      }
//    }
//    for (VsbSapVertex v1 : sapVertices) {
//      for (VsbLinkVertex v2 : vLVertices) {
//        for (String epId : v2.getVsbLink().getEndPointIds()) {
//          if (v1.getVsbEndpoint().getEndPointId().equals(epId)) {
//            g.addEdge(v1, v2, v1.getVsbEndpoint().getEndPointId());
//          }
//        }
//      }
//    }
//  }
//
//  public String exportGraphViz() throws ExportException {
//    ComponentNameProvider<VsbVertex> vertexIdProvider = VsbVertex::getId;
//    ComponentNameProvider<VsbVertex> vertexLabelProvider = VsbVertex::toString;
//    ComponentAttributeProvider<VsbVertex> vertexAttributeProvider = v -> {
//      Map<String, Attribute> map = new LinkedHashMap<>();
//      if (v instanceof VsbLinkVertex) {
//        map.put("shape", DefaultAttribute.createAttribute("oval"));
//        map.put("style", DefaultAttribute.createAttribute("filled"));
//        map.put("fillcolor", DefaultAttribute.createAttribute("dodgerblue"));
//      } else if (v instanceof AtomicComponentVertex) {
//        map.put("shape", DefaultAttribute.createAttribute("box"));
//        map.put("style", DefaultAttribute.createAttribute("filled"));
//        map.put("fillcolor", DefaultAttribute.createAttribute("yellowgreen"));
//      } else if (v instanceof VsbSapVertex) {
//        map.put("shape", DefaultAttribute.createAttribute("oval"));
//        map.put("style", DefaultAttribute.createAttribute("filled"));
//        map.put("fillcolor", DefaultAttribute.createAttribute("darksalmon"));
//      } else {
//        map = null;
//      }
//      return map;
//    };
//    DOTExporter<VsbVertex, String> exporter = new DOTExporter<>(vertexIdProvider,
//        vertexLabelProvider, new StringComponentNameProvider<>(), vertexAttributeProvider, null);
//    exporter.putGraphAttribute("splines", "false");
//    exporter.putGraphAttribute("overlap", "false");
//    exporter.putGraphAttribute("mindist", "2.0");
//    Writer writer = new StringWriter();
//    exporter.exportGraph(g, writer);
//    return writer.toString();
//  }
//
//  public String exportGraphML() throws ExportException {
//    ComponentNameProvider<VsbVertex> vertexIdProvider = VsbVertex::getId;
//    ComponentNameProvider<VsbVertex> vertexLabelProvider = VsbVertex::toString;
//    ComponentAttributeProvider<VsbVertex> vertexAttributeProvider = v -> {
//      Map<String, Attribute> map = new LinkedHashMap<>();
//      if (v instanceof VsbLinkVertex) {
//        map.put("color", DefaultAttribute.createAttribute("blue"));
//      } else if (v instanceof AtomicComponentVertex) {
//        map.put("color", DefaultAttribute.createAttribute("yellow"));
//      } else {
//        map = null;
//      }
//      return map;
//    };
//    GraphMLExporter<VsbVertex, String> exporter = new GraphMLExporter<>(vertexIdProvider,
//        vertexLabelProvider, vertexAttributeProvider, new IntegerComponentNameProvider<>(),
//        new StringComponentNameProvider<>(), null);
//    exporter
//        .registerAttribute("color", GraphMLExporter.AttributeCategory.NODE, AttributeType.STRING,
//            "yellow");
//    Writer writer = new StringWriter();
//    exporter.exportGraph(g, writer);
//    return writer.toString();
//  }
//
//  public VsBlueprint getVsB() {
//    return vsB;
//  }
//
//  public Graph<VsbVertex, String> getG() {
//    return g;
//  }
//
//  public List<AtomicComponentVertex> getaCVertices() {
//    return aCVertices;
//  }
//
//  public List<VsbLinkVertex> getvLVertices() {
//    return vLVertices;
//  }
}
