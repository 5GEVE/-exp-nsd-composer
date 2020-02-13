package it.cnit.blueprint.expbuilder.rest;

import it.cnit.blueprint.expbuilder.nsdgraph.GraphExporter;
import it.cnit.blueprint.expbuilder.nsdgraph.PnfProfileVertex;
import it.cnit.blueprint.expbuilder.nsdgraph.ProfileVertex;
import it.cnit.blueprint.expbuilder.nsdgraph.SapVertex;
import it.cnit.blueprint.expbuilder.nsdgraph.VirtualLinkProfileVertex;
import it.cnit.blueprint.expbuilder.nsdgraph.VnfProfileVertex;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.NotExistingEntityException;
import it.nextworks.nfvmano.libs.ifa.descriptors.common.elements.LinkBitrateRequirements;
import it.nextworks.nfvmano.libs.ifa.descriptors.common.elements.VirtualLinkProfile;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsDf;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsLevel;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsVirtualLinkConnectivity;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Nsd;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.PnfProfile;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Sapd;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.VirtualLinkToLevelMapping;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.VnfToLevelMapping;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.NotImplementedException;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.SimpleGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class Composer {

  private Logger LOG = LoggerFactory.getLogger(this.getClass());

  //TODO check for uninitialized Nsd.
  private Nsd nsd;

  private Map<DfIlKey, Graph<ProfileVertex, String>> graphMap = new HashMap<>();

  @Autowired
  public GraphExporter graphExporter;

  public Composer(Nsd nsd, GraphExporter graphExporter) {
    this.nsd = nsd;
    buildGraphs();
    this.graphExporter = graphExporter;
  }

  private void buildGraphs() {
    try {
      for (NsDf df : nsd.getNsDf()) {
        for (NsLevel l : df.getNsInstantiationLevel()) {
          Graph<ProfileVertex, String> g = new SimpleGraph<>(String.class);
          List<VnfProfileVertex> vnfPVertices = new ArrayList<>();
          List<PnfProfileVertex> pnfPVertices = new ArrayList<>();
          List<VirtualLinkProfileVertex> vlPVertices = new ArrayList<>();
          List<SapVertex> sapVertices = new ArrayList<>();

          //vertices
          for (VnfToLevelMapping vnfToLevelMapping : l.getVnfToLevelMapping()) {
            VnfProfileVertex v = new VnfProfileVertex(
                df.getVnfProfile(vnfToLevelMapping.getVnfProfileId()));
            // TODO handle the number of instances to build the graph
            vnfPVertices.add(v);
            g.addVertex(v);
          }
          for (PnfProfile pp : df.getPnfProfile()) {
            PnfProfileVertex v = new PnfProfileVertex(pp);
            pnfPVertices.add(v);
            g.addVertex(v);
          }
          for (VirtualLinkToLevelMapping vlToLevelMapping : l.getVirtualLinkToLevelMapping()) {
            VirtualLinkProfileVertex v = new VirtualLinkProfileVertex(
                df.getVirtualLinkProfile(vlToLevelMapping.getVirtualLinkProfileId()));
            vlPVertices.add(v);
            g.addVertex(v);
          }
          for (Sapd s : nsd.getSapd()) {
            SapVertex v = new SapVertex(s);
            sapVertices.add(v);
            g.addVertex(v);
          }

          // edges
          for (VnfProfileVertex v1 : vnfPVertices) {
            for (NsVirtualLinkConnectivity vlc : v1.getVnfProfile()
                .getNsVirtualLinkConnectivity()) {
              for (VirtualLinkProfileVertex v2 : vlPVertices) {
                if (vlc.getVirtualLinkProfileId()
                    .equals(v2.getVlProfile().getVirtualLinkProfileId())) {
                  g.addEdge(v1, v2, vlc.getCpdId().get(0));
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
                  g.addEdge(v1, v2, vlc.getCpdId().get(0));
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

          graphMap.put(new DfIlKey(df.getNsDfId(), l.getNsLevelId()), g);
        }
      }
    } catch (NotExistingEntityException e) {
      LOG.error("Error: {}", e.getMessage());
    }
  }

  public void composeWith(CtxComposeInfo[] ctxRArray) throws NotExistingEntityException {
    for (CtxComposeInfo ctxR : ctxRArray) {
      for (Map.Entry<DfIlKey, Graph<ProfileVertex, String>> entry : graphMap.entrySet()) {
        if (ctxR.getStrat() == CompositionStrat.CONNECT) {
          this.composeWithConnect(ctxR);
        } else if (ctxR.getStrat() == CompositionStrat.PASSTHROUGH) {
          this.composeWithPassthrough(ctxR);
        } else {
          throw new NotImplementedException(
              String.format("Composition strategy %s not implemented", ctxR.getStrat()));
        }
      }
    }
  }

  public void composeWithConnect(CtxComposeInfo ctxR) throws NotExistingEntityException {
    if (ctxR.getStrat() != CompositionStrat.CONNECT) {
      LOG.error("Composition strategy is not 'CONNECT'. Doing nothing.");
      throw new IllegalArgumentException();
    }
    // TODO handle other exceptions here

    for (Map.Entry<DfIlKey, Graph<ProfileVertex, String>> entry : graphMap.entrySet()) {
      LOG.info("Compose '{}' with '{}' for nsDfId '{}' and nsLevelId '{}' using CONNECT",
          nsd.getNsdIdentifier(), ctxR.getNsd().getNsdIdentifier(), entry.getKey().nsDfId,
          entry.getKey().nsIlId);
      LOG.debug("Export before:\n{}", export(entry.getKey()));

      for (Map.Entry<String, String> vnfVl : ctxR.getVirtualLinkIds().entrySet()) {
        // Create new vertices to add
        VnfProfileVertex v1 = new VnfProfileVertex(
            ctxR.getNsd().getNsDf().get(0).getVnfProfile(vnfVl.getKey()));
        VirtualLinkProfileVertex v2 = new VirtualLinkProfileVertex(
            ctxR.getNsd().getNsDf().get(0).getVirtualLinkProfile(vnfVl.getValue()));
        // Add vertices
        entry.getValue().addVertex(v1);
        entry.getValue().addVertex(v2);
        // Modify edges
        entry.getValue().addEdge(v1, v2);
      }

      LOG.debug("Export after:\n{}", export(entry.getKey()));
    }
  }

  public void composeWithPassthrough(CtxComposeInfo ctxR) throws NotExistingEntityException {
    if (ctxR.getStrat() != CompositionStrat.PASSTHROUGH) {
      LOG.error("Composition strategy is not 'PASSTHROUGH'. Doing nothing.");
      throw new IllegalArgumentException();
    }
    // TODO handle other exceptions here

    for (Map.Entry<DfIlKey, Graph<ProfileVertex, String>> entry : graphMap.entrySet()) {
      LOG.info("Compose '{}' with '{}' for nsDfId '{}' and nsLevelId '{}' using PASSTHROUGH",
          nsd.getNsdIdentifier(), ctxR.getNsd().getNsdIdentifier(), entry.getKey().nsDfId,
          entry.getKey().nsIlId);
      LOG.debug("Export before:\n{}", export(entry.getKey()));

      // TODO update Nsd model when modifying the graph.

      // Get information on vertices and edges
      ProfileVertex sapV = entry.getValue().vertexSet().stream()
          .filter(v -> v.getElementId().equals(ctxR.getSapId())).findAny().get();
      ProfileVertex vlV = Graphs.neighborListOf(entry.getValue(), sapV).get(0);
      String edgeOld = entry.getValue().getEdge(sapV, vlV);

      // Create new vertices to add
      // We assume only one Df and one InstantiationLevel for contexts (one graph)
      VnfProfileVertex contextV = new VnfProfileVertex(
          ctxR.getNsd().getNsDf().get(0).getVnfProfile().get(0));
      VirtualLinkProfileVertex vlVnew = new VirtualLinkProfileVertex(
          new VirtualLinkProfile(
              nsd.getNsDeploymentFlavour(entry.getKey().nsDfId),
              "vl_profile_" + contextV.getVnfProfile().getVnfProfileId(),
              "vl_" + contextV.getVnfProfile().getVnfProfileId(),
              "vl_df_" + contextV.getVnfProfile().getVnfProfileId(), null, null,
              new LinkBitrateRequirements("1", "1"), new LinkBitrateRequirements("1", "1")));

      // Add vertices
      entry.getValue().addVertex(contextV);
      entry.getValue().addVertex(vlVnew);

      // Modify edges
      entry.getValue().addEdge(sapV, vlVnew, edgeOld + "_new");
      entry.getValue().addEdge(vlVnew, contextV,
          String.format("cp_%s_in", contextV.getVnfProfile().getVnfProfileId()));
      entry.getValue().addEdge(contextV, vlV,
          String.format("cp_%s_out", contextV.getVnfProfile().getVnfProfileId()));
      entry.getValue().removeEdge(edgeOld);

      LOG.debug("Export after:\n{}", export(entry.getKey()));
    }
  }

  public void setGraphExporter(GraphExporter exporter) {
    this.graphExporter = exporter;
  }

  public String export(DfIlKey key) {
    if (!graphMap.containsKey(key)) {
      LOG.error("Graph key '{}' not found.", key.toString());
      throw new IllegalArgumentException("");
    }
    return graphExporter.export(graphMap.get(key));
  }

  public Set<DfIlKey> getGraphMapKeys() {
    return graphMap.keySet();
  }

  public static class DfIlKey {

    private final String nsDfId;
    private final String nsIlId;

    private DfIlKey(String nsDfId, String nsIlId) {
      this.nsDfId = nsDfId;
      this.nsIlId = nsIlId;
    }

    public String getNsDfId() {
      return nsDfId;
    }

    public String getNsIlId() {
      return nsIlId;
    }

    @Override
    public String toString() {
      return String.format("(%s,%s)", nsDfId, nsIlId);
    }
  }

  public enum CompositionStrat {
    CONNECT,
    PASSTHROUGH
  }

}
