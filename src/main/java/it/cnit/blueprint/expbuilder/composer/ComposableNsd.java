package it.cnit.blueprint.expbuilder.composer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import it.cnit.blueprint.expbuilder.nsdgraph.GraphVizExporter;
import it.cnit.blueprint.expbuilder.nsdgraph.NsdGraphExporter;
import it.cnit.blueprint.expbuilder.nsdgraph.PnfProfileVertex;
import it.cnit.blueprint.expbuilder.nsdgraph.ProfileVertex;
import it.cnit.blueprint.expbuilder.nsdgraph.SapVertex;
import it.cnit.blueprint.expbuilder.nsdgraph.VirtualLinkProfileVertex;
import it.cnit.blueprint.expbuilder.nsdgraph.VnfProfileVertex;
import it.cnit.blueprint.expbuilder.rest.CtxComposeResource;
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
import org.apache.commons.lang3.NotImplementedException;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.SimpleGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ComposableNsd extends Nsd {

  private Logger LOG = LoggerFactory.getLogger(this.getClass());

  @JsonIgnore
  Map<DfIlKey, Graph<ProfileVertex, String>> graphMap = new HashMap<>();
  @JsonIgnore
  NsdGraphExporter graphExporter = new GraphVizExporter();

  private void buildGraphs() throws NotExistingEntityException {
    for (NsDf df : getNsDf()) {
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
        for (Sapd s : getSapd()) {
          SapVertex v = new SapVertex(s);
          sapVertices.add(v);
          g.addVertex(v);
        }

        // edges
        for (VnfProfileVertex v1 : vnfPVertices) {
          for (NsVirtualLinkConnectivity vlc : v1.getVnfProfile().getNsVirtualLinkConnectivity()) {
            for (VirtualLinkProfileVertex v2 : vlPVertices) {
              if (vlc.getVirtualLinkProfileId()
                  .equals(v2.getVlProfile().getVirtualLinkProfileId())) {
                g.addEdge(v1, v2, vlc.getCpdId().get(0));
              }
            }
          }
        }
        for (PnfProfileVertex v1 : pnfPVertices) {
          for (NsVirtualLinkConnectivity vlc : v1.getPnfProfile().getNsVirtualLinkConnectivity()) {
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
  }

  public void composeWith(CtxComposeResource[] ctxRArray) throws NotExistingEntityException {
    if (graphMap.isEmpty()) {
      buildGraphs();
    }
    for (CtxComposeResource ctxR : ctxRArray) {
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

  public void composeWithConnect(CtxComposeResource ctxR) throws NotExistingEntityException {
    if (graphMap.isEmpty()) {
      buildGraphs();
    }

    if (ctxR.getStrat() != CompositionStrat.CONNECT) {
      LOG.error("Composition strategy is not 'CONNECT'. Doing nothing.");
      throw new IllegalArgumentException();
    }
    // TODO handle other exceptions here

    for (Map.Entry<DfIlKey, Graph<ProfileVertex, String>> entry : graphMap.entrySet()) {
      LOG.info("Compose '{}' with '{}' for nsDfId '{}' and nsLevelId '{}' using CONNECT",
          getNsdIdentifier(), ctxR.getNsd().getNsdIdentifier(), entry.getKey().nsDfId,
          entry.getKey().nsIlId);
      LOG.debug("GraphViz export before:\n{}", graphExporter.export(entry.getValue()));
      //TODO  Move composition code here.
      LOG.debug("GraphViz export after:\n{}", graphExporter.export(entry.getValue()));
    }
  }

  public void composeWithPassthrough(CtxComposeResource ctxR) throws NotExistingEntityException {
    if (graphMap.isEmpty()) {
      buildGraphs();
    }

    if (ctxR.getStrat() != CompositionStrat.PASSTHROUGH) {
      LOG.error("Composition strategy is not 'PASSTHROUGH'. Doing nothing.");
      throw new IllegalArgumentException();
    }
    // TODO handle other exceptions here

    for (Map.Entry<DfIlKey, Graph<ProfileVertex, String>> entry : graphMap.entrySet()) {
      LOG.info("Compose '{}' with '{}' for nsDfId '{}' and nsLevelId '{}' using CONNECT",
          getNsdIdentifier(), ctxR.getNsd().getNsdIdentifier(), entry.getKey().nsDfId,
          entry.getKey().nsIlId);
      LOG.debug("GraphViz export before:\n{}", graphExporter.export(entry.getValue()));

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
              this.getNsDeploymentFlavour(entry.getKey().nsDfId),
              "vl_profile_" + contextV.getVnfProfile().getVnfProfileId(),
              "vl_" + contextV.getVnfProfile().getVnfProfileId(),
              "vl_df_" + contextV.getVnfProfile().getVnfProfileId(), null, null,
              new LinkBitrateRequirements("1", "1"), new LinkBitrateRequirements("1", "1")));

      // Add vertices
      // TODO remove commented as probably unneded
//      entry.getValue().getVnfPVertices().add(contextV);
      entry.getValue().addVertex(contextV);
//      entry.getValue().getVlPVertices().add(vlVnew);
      entry.getValue().addVertex(vlVnew);

      // Modify edges
      entry.getValue().addEdge(sapV, vlVnew, edgeOld + "_new");
      entry.getValue().addEdge(vlVnew, contextV,
          String.format("cp_%s_in", contextV.getVnfProfile().getVnfProfileId()));
      entry.getValue().addEdge(contextV, vlV,
          String.format("cp_%s_out", contextV.getVnfProfile().getVnfProfileId()));
      entry.getValue().removeEdge(edgeOld);

      LOG.debug("GraphViz export after:\n{}", graphExporter.export(entry.getValue()));
    }
  }

  private static class DfIlKey {

    private final String nsDfId;
    private final String nsIlId;

    private DfIlKey(String nsDfId, String nsIlId) {
      this.nsDfId = nsDfId;
      this.nsIlId = nsIlId;
    }
  }

  public enum CompositionStrat {
    CONNECT,
    PASSTHROUGH
  }
}
