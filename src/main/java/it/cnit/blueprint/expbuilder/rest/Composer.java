package it.cnit.blueprint.expbuilder.rest;

import it.cnit.blueprint.expbuilder.compose.CompositionStrategy;
import it.cnit.blueprint.expbuilder.compose.ConnectStrategy;
import it.cnit.blueprint.expbuilder.compose.PassThroughStrategy;
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
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.VnfProfile;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.VnfToLevelMapping;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.SimpleGraph;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

@Service
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
@Slf4j
public class Composer {

  //TODO check for uninitialized Nsd.
  private Nsd nsd;

  @Getter
  private Map<DfIlKey, Graph<ProfileVertex, String>> graphMap = new HashMap<>();

  @Setter
  private GraphExporter graphExporter;

  @Qualifier("connect")
  private CompositionStrategy connectStrategy;

  @Qualifier("passthrough")
  private CompositionStrategy passThroughStrategy;

  public Composer(GraphExporter graphExporter,
      CompositionStrategy connectStrategy,
      CompositionStrategy passThroughStrategy) {
    this.graphExporter = graphExporter;
    this.connectStrategy = connectStrategy;
    this.passThroughStrategy = passThroughStrategy;
  }

  public void init(Nsd nsd) {
    this.nsd = nsd;
    buildGraphs();
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
      log.error("Error: {}", e.getMessage());
    }
  }

  public void composeWith(CtxComposeInfo[] ctxRArray)
      throws NotExistingEntityException, InvalidCtxComposeInfo {
    for (CtxComposeInfo ctxR : ctxRArray) {
      for (Map.Entry<DfIlKey, Graph<ProfileVertex, String>> entry : graphMap.entrySet()) {
        if (ctxR.getStrat() == CompositionStrat.CONNECT) {
          connectStrategy.compose(nsd, graphMap, ctxR);
        } else if (ctxR.getStrat() == CompositionStrat.PASSTHROUGH) {
          passThroughStrategy.compose(nsd, graphMap, ctxR);
        } else {
          throw new NotImplementedException(
              String.format("Composition strategy %s not implemented", ctxR.getStrat()));
        }
      }
    }
  }

  public void composeWithPassthrough(CtxComposeInfo ctxR) throws NotExistingEntityException {
    if (ctxR.getStrat() != CompositionStrat.PASSTHROUGH) {
      log.error("Composition strategy is not 'PASSTHROUGH'. Doing nothing.");
      throw new IllegalArgumentException();
    }
    // TODO handle other exceptions here

    for (Map.Entry<DfIlKey, Graph<ProfileVertex, String>> entry : graphMap.entrySet()) {
      log.info("Compose '{}' with '{}' for nsDfId '{}' and nsLevelId '{}' using PASSTHROUGH",
          nsd.getNsdIdentifier(), ctxR.getNsd().getNsdIdentifier(), entry.getKey().nsDfId,
          entry.getKey().nsIlId);
      log.debug("Export before:\n{}", export(entry.getKey()));

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

      log.debug("Export after:\n{}", export(entry.getKey()));
    }
  }

  public String export(DfIlKey key) {
    if (!graphMap.containsKey(key)) {
      log.error("Graph key '{}' not found.", key.toString());
      throw new IllegalArgumentException("");
    }
    return graphExporter.export(graphMap.get(key));
  }

  public Set<DfIlKey> getGraphMapKeys() {
    return graphMap.keySet();
  }


  @AllArgsConstructor
  public static class DfIlKey {

    @Getter
    private final String nsDfId;
    @Getter
    private final String nsIlId;

  }

  public enum CompositionStrat {
    CONNECT,
    PASSTHROUGH
  }

}
