package it.cnit.blueprint.composer.nsd.graph;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.engine.Renderer;
import it.cnit.blueprint.composer.exceptions.NsdInvalidException;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.NotExistingEntityException;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsDf;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsLevel;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsVirtualLinkConnectivity;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.PnfProfile;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Sapd;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.VirtualLinkToLevelMapping;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.VnfProfile;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.VnfToLevelMapping;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import org.slf4j.helpers.MessageFormatter;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class NsdGraphService {

  public Graph<ProfileVertex, String> buildGraph(List<Sapd> sapdList, NsDf nsDf, NsLevel nsLevel)
      throws NsdInvalidException {
    Graph<ProfileVertex, String> g = new SimpleGraph<>(String.class);
    List<VnfProfileVertex> vnfPVertices = new ArrayList<>();
    List<PnfProfileVertex> pnfPVertices = new ArrayList<>();
    List<VirtualLinkProfileVertex> vlPVertices = new ArrayList<>();
    List<SapVertex> sapVertices = new ArrayList<>();

    // Vertices
    for (VnfToLevelMapping vnfToLevelMapping : nsLevel.getVnfToLevelMapping()) {
      VnfProfile vnfProfile;
      try {
        vnfProfile = nsDf.getVnfProfile(vnfToLevelMapping.getVnfProfileId());
      } catch (NotExistingEntityException e) {
        String message = MessageFormatter.arrayFormat(
            "vnfProfileId='{}' not found for nsDfId='{}' and nsLevelId='{}'.",
            new String[]{vnfToLevelMapping.getVnfProfileId(), nsDf.getNsDfId(),
                nsLevel.getNsLevelId()}).getMessage();
        log.error(message);
        throw new NsdInvalidException(nsDf.getNsDfId(), message);
      }
      for (int i = 0; i < vnfToLevelMapping.getNumberOfInstances(); i++) {
        VnfProfileVertex v = new VnfProfileVertex(vnfProfile, i);
        vnfPVertices.add(v);
        g.addVertex(v);
      }
    }
    for (PnfProfile pp : nsDf.getPnfProfile()) {
      PnfProfileVertex v = new PnfProfileVertex(pp);
      pnfPVertices.add(v);
      g.addVertex(v);
    }
    for (VirtualLinkToLevelMapping vlToLevelMapping : nsLevel.getVirtualLinkToLevelMapping()) {
      VirtualLinkProfileVertex v;
      try {
        v = new VirtualLinkProfileVertex(
            nsDf.getVirtualLinkProfile(vlToLevelMapping.getVirtualLinkProfileId()));
      } catch (NotExistingEntityException e) {
        String message = MessageFormatter.arrayFormat(
            "virtualLinkProfileId='{}' not found for nsDfId='{}' and nsLevelId='{}'.",
            new String[]{vlToLevelMapping.getVirtualLinkProfileId(), nsDf.getNsDfId(),
                nsLevel.getNsLevelId()}).getMessage();
        log.error(message);
        throw new NsdInvalidException(nsDf.getNsDfId(), message);
      }
      vlPVertices.add(v);
      g.addVertex(v);
    }
    for (Sapd s : sapdList) {
      SapVertex v = new SapVertex(s);
      sapVertices.add(v);
      g.addVertex(v);
    }

    // Edges
    for (VnfProfileVertex v1 : vnfPVertices) {
      for (NsVirtualLinkConnectivity vlc : v1.getVnfProfile()
          .getNsVirtualLinkConnectivity()) {
        for (VirtualLinkProfileVertex v2 : vlPVertices) {
          if (vlc.getVirtualLinkProfileId()
              .equals(v2.getVlProfile().getVirtualLinkProfileId())) {
            g.addEdge(v1, v2, v1.getElementId() + "_" + vlc.getCpdId().get(0));
          }
        }
      }
    }
    for (PnfProfileVertex v1 : pnfPVertices) {
      for (NsVirtualLinkConnectivity vlc : v1.getPnfProfile()
          .getNsVirtualLinkConnectivity()) {
        for (VirtualLinkProfileVertex v2 : vlPVertices) {
          if (vlc.getVirtualLinkProfileId()
              .equals(v2.getVlProfile().getVirtualLinkProfileId())) {
            g.addEdge(v1, v2, v1.getElementId() + "_" + vlc.getCpdId().get(0));
          }
        }
      }
    }
    for (SapVertex v1 : sapVertices) {
      for (VirtualLinkProfileVertex v2 : vlPVertices) {
        if (v1.getSapd().getNsVirtualLinkDescId()
            .equals(v2.getVlProfile().getVirtualLinkDescId())) {
          g.addEdge(v1, v2, v1.getSapd().getCpdId());
        }
      }
    }
    return g;
  }

  public String export(Graph<ProfileVertex, String> graph) {
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
    exporter.exportGraph(graph, writer);
    return writer.toString();
  }

  public Renderer renderSVG(String dot) {
    return Graphviz.fromString(dot).render(Format.SVG);
  }

  public Renderer renderPNG(String dot) {
    return Graphviz.fromString(dot).width(1920).render(Format.PNG);
  }

  public ProfileVertex getVertexById(Graph<ProfileVertex, String> g, String vertexId)
      throws ProfileVertexNotFoundException {
    Optional<ProfileVertex> optVertex = g.vertexSet().stream()
        .filter(v -> v.getElementId().equals(vertexId))
        .findFirst();
    if (optVertex.isPresent()) {
      return optVertex.get();
    } else {
      throw new ProfileVertexNotFoundException(
          "ProfileVertex with id='" + vertexId + "' not found.");
    }
  }

  public boolean isConnected(Graph<ProfileVertex, String> g) {
    ConnectivityInspector<ProfileVertex, String> inspector = new ConnectivityInspector<>(g);
    return inspector.isConnected();
  }

}
