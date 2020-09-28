package it.cnit.blueprint.composer.vsb.graph;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import it.nextworks.nfvmano.catalogue.blueprint.elements.Blueprint;
import it.nextworks.nfvmano.catalogue.blueprint.elements.VsComponent;
import it.nextworks.nfvmano.catalogue.blueprint.elements.VsbEndpoint;
import it.nextworks.nfvmano.catalogue.blueprint.elements.VsbLink;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.io.Attribute;
import org.jgrapht.io.ComponentAttributeProvider;
import org.jgrapht.io.ComponentNameProvider;
import org.jgrapht.io.DOTExporter;
import org.jgrapht.io.DefaultAttribute;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class VsbGraphService {

  public Graph<VsbVertex, String> buildGraph(Blueprint b) {
    Graph<VsbVertex, String> g = new SimpleGraph<>(String.class);
    List<AtomicComponentVertex> compVList = new ArrayList<>();
    List<VsbLinkVertex> linkVList = new ArrayList<>();
    List<VsbSapVertex> sapVList = new ArrayList<>();

    // Vertices
    for (VsComponent c : b.getAtomicComponents()) {
      for (int i = 0; i < c.getServersNumber(); i++) {
        AtomicComponentVertex v = new AtomicComponentVertex(c, i);
        compVList.add(v);
        g.addVertex(v);
      }
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
              g.addEdge(v1, v2, vlEp + "_" + v1.getNumber());
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

  public String export(Graph<VsbVertex, String> g) {
    ComponentNameProvider<VsbVertex> vertexIdProvider = VsbVertex::getVertexId;
    ComponentNameProvider<VsbVertex> vertexLabelProvider = VsbVertex::toString;
    ComponentAttributeProvider<VsbVertex> vertexAttributeProvider = v -> {
      Map<String, Attribute> map = new LinkedHashMap<>();
      if (v instanceof VsbLinkVertex) {
        map.put("shape", DefaultAttribute.createAttribute("oval"));
        map.put("style", DefaultAttribute.createAttribute("filled"));
        map.put("fillcolor", DefaultAttribute.createAttribute("#dae8fc"));
        map.put("color", DefaultAttribute.createAttribute("#6c8ebf"));
      } else if (v instanceof AtomicComponentVertex) {
        map.put("shape", DefaultAttribute.createAttribute("box"));
        map.put("style", DefaultAttribute.createAttribute("filled"));
        map.put("fillcolor", DefaultAttribute.createAttribute("#d5e8d4"));
        map.put("color", DefaultAttribute.createAttribute("#82b366"));
      } else if (v instanceof VsbSapVertex) {
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
    DOTExporter<VsbVertex, String> exporter = new DOTExporter<>(
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

  public boolean isConnected(Graph<VsbVertex, String> g) {
    ConnectivityInspector<VsbVertex, String> inspector = new ConnectivityInspector<>(g);
    return inspector.isConnected();
  }

  public File writeImageFile(Blueprint b) throws IOException {
    File tempFile = Files.createTempFile(b.getBlueprintId() + "-", ".png").toFile();
    String dotGraph = export(buildGraph(b));
    Graphviz.fromString(dotGraph).width(1920).render(Format.PNG).toFile(tempFile);
    return tempFile;
  }
}


