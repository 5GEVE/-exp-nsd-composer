package eu._5geve.experiment.nsdgraph;

import it.nextworks.nfvmano.libs.ifa.descriptors.common.elements.VirtualLinkProfile;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsDf;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsVirtualLinkConnectivity;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Nsd;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.PnfProfile;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Sapd;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.VnfProfile;
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

public class NsdGraph {

  List<eu._5geve.experiment.nsdgraph.VnfProfileVertex> vnfPVertices = new ArrayList<>();
  List<eu._5geve.experiment.nsdgraph.PnfProfileVertex> pnfPVertices = new ArrayList<>();
  List<eu._5geve.experiment.nsdgraph.VirtualLinkProfileVertex> vlPVertices = new ArrayList<>();
  List<eu._5geve.experiment.nsdgraph.SapVertex> sapVertices = new ArrayList<>();
  private Nsd nsd;
  private Graph<eu._5geve.experiment.nsdgraph.ProfileVertex, String> g;

  public NsdGraph(Nsd nsd) {
    this.nsd = nsd;
    this.g = new SimpleGraph<>(String.class);

    // vertices
    for (VnfProfile vp : nsd.getNsDf().get(0).getVnfProfile()) {
      eu._5geve.experiment.nsdgraph.VnfProfileVertex v = new eu._5geve.experiment.nsdgraph.VnfProfileVertex(vp);
      vnfPVertices.add(v);
      g.addVertex(v);
    }
    for (PnfProfile pp : nsd.getNsDf().get(0).getPnfProfile()) {
      eu._5geve.experiment.nsdgraph.PnfProfileVertex v = new eu._5geve.experiment.nsdgraph.PnfProfileVertex(pp);
      pnfPVertices.add(v);
      g.addVertex(v);
    }
    for (VirtualLinkProfile vlp : nsd.getNsDf().get(0).getVirtualLinkProfile()) {
      eu._5geve.experiment.nsdgraph.VirtualLinkProfileVertex v = new eu._5geve.experiment.nsdgraph.VirtualLinkProfileVertex(vlp);
      vlPVertices.add(v);
      g.addVertex(v);
    }
    for (Sapd s : nsd.getSapd()) {
      eu._5geve.experiment.nsdgraph.SapVertex v = new eu._5geve.experiment.nsdgraph.SapVertex(s);
      sapVertices.add(v);
      g.addVertex(v);
    }

    // edges
    for (eu._5geve.experiment.nsdgraph.VnfProfileVertex v1 : vnfPVertices) {
      for (NsVirtualLinkConnectivity vlc : v1.getVnfProfile().getNsVirtualLinkConnectivity()) {
        for (eu._5geve.experiment.nsdgraph.VirtualLinkProfileVertex v2 : vlPVertices) {
          if (vlc.getVirtualLinkProfileId().equals(v2.getVlProfile().getVirtualLinkProfileId())) {
            g.addEdge(v1, v2, vlc.getCpdId().get(0));
          }
        }
      }
    }
    for (eu._5geve.experiment.nsdgraph.PnfProfileVertex v1 : pnfPVertices) {
      for (NsVirtualLinkConnectivity vlc : v1.getPnfProfile().getNsVirtualLinkConnectivity()) {
        for (eu._5geve.experiment.nsdgraph.VirtualLinkProfileVertex v2 : vlPVertices) {
          if (vlc.getVirtualLinkProfileId().equals(v2.getVlProfile().getVirtualLinkProfileId())) {
            g.addEdge(v1, v2, vlc.getCpdId().get(0));
          }
        }
      }
    }
    for (eu._5geve.experiment.nsdgraph.SapVertex v1 : sapVertices) {
      for (eu._5geve.experiment.nsdgraph.VirtualLinkProfileVertex v2 : vlPVertices) {
        if (v1.getSapd().getNsVirtualLinkDescId()
            .equals(v2.getVlProfile().getVirtualLinkDescId())) {
          g.addEdge(v1, v2, v1.getSapd().getCpdId());
        }
      }
    }
  }

  public String exportGraphViz() throws ExportException {
    ComponentNameProvider<eu._5geve.experiment.nsdgraph.ProfileVertex> vertexIdProvider = eu._5geve.experiment.nsdgraph.ProfileVertex::getProfileId;
    ComponentNameProvider<eu._5geve.experiment.nsdgraph.ProfileVertex> vertexLabelProvider = eu._5geve.experiment.nsdgraph.ProfileVertex::toString;
    ComponentAttributeProvider<eu._5geve.experiment.nsdgraph.ProfileVertex> vertexAttributeProvider = v -> {
      Map<String, Attribute> map = new LinkedHashMap<>();
      if (v instanceof eu._5geve.experiment.nsdgraph.VirtualLinkProfileVertex) {
        map.put("shape", DefaultAttribute.createAttribute("oval"));
        map.put("style", DefaultAttribute.createAttribute("filled"));
        map.put("fillcolor", DefaultAttribute.createAttribute("dodgerblue"));
      } else if (v instanceof eu._5geve.experiment.nsdgraph.VnfProfileVertex
          || v instanceof eu._5geve.experiment.nsdgraph.PnfProfileVertex) {
        map.put("shape", DefaultAttribute.createAttribute("box"));
        map.put("style", DefaultAttribute.createAttribute("filled"));
        map.put("fillcolor", DefaultAttribute.createAttribute("yellowgreen"));
      } else if (v instanceof eu._5geve.experiment.nsdgraph.SapVertex) {
        map.put("shape", DefaultAttribute.createAttribute("oval"));
        map.put("style", DefaultAttribute.createAttribute("filled"));
        map.put("fillcolor", DefaultAttribute.createAttribute("darksalmon"));
      } else {
        map = null;
      }
      return map;
    };
    DOTExporter<eu._5geve.experiment.nsdgraph.ProfileVertex, String> exporter = new DOTExporter<>(vertexIdProvider,
        vertexLabelProvider, new StringComponentNameProvider<>(), vertexAttributeProvider, null);
    exporter.putGraphAttribute("splines", "false");
    Writer writer = new StringWriter();
    exporter.exportGraph(g, writer);
    return writer.toString();
  }

  public String exportGraphML() throws ExportException {
    ComponentNameProvider<eu._5geve.experiment.nsdgraph.ProfileVertex> vertexIdProvider = eu._5geve.experiment.nsdgraph.ProfileVertex::getProfileId;
    ComponentNameProvider<eu._5geve.experiment.nsdgraph.ProfileVertex> vertexLabelProvider = eu._5geve.experiment.nsdgraph.ProfileVertex::toString;
    ComponentAttributeProvider<eu._5geve.experiment.nsdgraph.ProfileVertex> vertexAttributeProvider = v -> {
      Map<String, Attribute> map = new LinkedHashMap<>();
      if (v instanceof eu._5geve.experiment.nsdgraph.VirtualLinkProfileVertex) {
        map.put("color", DefaultAttribute.createAttribute("blue"));
      } else if (v instanceof eu._5geve.experiment.nsdgraph.VnfProfileVertex
          || v instanceof eu._5geve.experiment.nsdgraph.PnfProfileVertex) {
        map.put("color", DefaultAttribute.createAttribute("yellow"));
      } else {
        map = null;
      }
      return map;
    };
    GraphMLExporter<eu._5geve.experiment.nsdgraph.ProfileVertex, String> exporter = new GraphMLExporter<>(vertexIdProvider,
        vertexLabelProvider, vertexAttributeProvider, new IntegerComponentNameProvider<>(),
        new StringComponentNameProvider<>(), null);
    exporter
        .registerAttribute("color", GraphMLExporter.AttributeCategory.NODE, AttributeType.STRING,
            "yellow");
    Writer writer = new StringWriter();
    exporter.exportGraph(g, writer);
    return writer.toString();
  }

  public Nsd getNsd() {
    return nsd;
  }

  public Graph<eu._5geve.experiment.nsdgraph.ProfileVertex, String> getG() {
    return g;
  }

  public List<eu._5geve.experiment.nsdgraph.VnfProfileVertex> getVnfPVertices() {
    return vnfPVertices;
  }

  public List<eu._5geve.experiment.nsdgraph.PnfProfileVertex> getPnfPVertices() {
    return pnfPVertices;
  }

  public List<eu._5geve.experiment.nsdgraph.VirtualLinkProfileVertex> getVlPVertices() {
    return vlPVertices;
  }

  public List<eu._5geve.experiment.nsdgraph.SapVertex> getSapVertices() {
    return sapVertices;
  }

  /**
   * Add a 'normal' VNF of a context Nsd. The new vertex is simply connected to the
   * VirtualLinkProfileVertex in the input.
   *
   * @param contextV A context VNF vertex.
   * @param vlP The vertex where the VNF should be attached to.
   */
  public void addVnfProfileVertex(
      eu._5geve.experiment.nsdgraph.VnfProfileVertex contextV, eu._5geve.experiment.nsdgraph.VirtualLinkProfileVertex vlP) {
    // TODO update Nsd model

    // Update Graph
    vnfPVertices.add(contextV);
    g.addVertex(contextV);
    g.addEdge(contextV, vlP,
        contextV.getVnfProfile().getNsVirtualLinkConnectivity().get(0).getCpdId().get(0));
  }

  /**
   * Add a 'passthrough' VNF of a context Nsd. A new VirtualLinkProfileVertex is created as well as
   * necessary edges. The edge in input is removed from the graph.
   *
   * @param contextV A context VNF vertex.
   * @param edge The edge where the VNF should be placed.
   */
  public void addVnfProfileVertex(eu._5geve.experiment.nsdgraph.VnfProfileVertex contextV, String edge) {
    // TODO update Nsd model
    VirtualLinkProfile vlpNew = new VirtualLinkProfile(new NsDf(),
        "vl_profile_" + contextV.getProfileId(), "vl_" + contextV.getProfileId(),
        "vl_df_" + contextV.getProfileId(), null, null, null, null);

    // Update graph
    vnfPVertices.add(contextV);
    g.addVertex(contextV);
    eu._5geve.experiment.nsdgraph.VirtualLinkProfileVertex vlpNewV = new eu._5geve.experiment.nsdgraph.VirtualLinkProfileVertex(vlpNew);
    vlPVertices.add(vlpNewV);
    g.addVertex(vlpNewV);

    eu._5geve.experiment.nsdgraph.ProfileVertex srcV = g.getEdgeSource(edge);
    eu._5geve.experiment.nsdgraph.ProfileVertex tarV = g.getEdgeTarget(edge);
    if (srcV instanceof eu._5geve.experiment.nsdgraph.VnfProfileVertex
        && tarV instanceof eu._5geve.experiment.nsdgraph.VirtualLinkProfileVertex) {
      g.addEdge(srcV, vlpNewV, edge + "_new");
      g.addEdge(vlpNewV, contextV,
          contextV.getVnfProfile().getNsVirtualLinkConnectivity().get(0).getCpdId().get(0));
      g.addEdge(contextV, tarV,
          contextV.getVnfProfile().getNsVirtualLinkConnectivity().get(1).getCpdId().get(0));
    } else if (srcV instanceof eu._5geve.experiment.nsdgraph.VirtualLinkProfileVertex
        && tarV instanceof eu._5geve.experiment.nsdgraph.VnfProfileVertex) {
      g.addEdge(srcV, contextV,
          contextV.getVnfProfile().getNsVirtualLinkConnectivity().get(0).getCpdId().get(0));
      g.addEdge(contextV, vlpNewV,
          contextV.getVnfProfile().getNsVirtualLinkConnectivity().get(1).getCpdId().get(0));
      g.addEdge(vlpNewV, tarV, edge + "_new");
    } else {
      throw new IllegalArgumentException("Graph is not valid");
    }
    g.removeEdge(edge);
  }
}
