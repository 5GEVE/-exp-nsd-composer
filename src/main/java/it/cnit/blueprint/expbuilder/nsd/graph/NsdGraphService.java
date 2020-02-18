package it.cnit.blueprint.expbuilder.nsd.graph;

import it.cnit.blueprint.expbuilder.rest.InvalidNsd;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.NotExistingEntityException;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsDf;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsLevel;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsVirtualLinkConnectivity;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.PnfProfile;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Sapd;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.VirtualLinkToLevelMapping;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.VnfToLevelMapping;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jgrapht.Graph;
import org.jgrapht.graph.SimpleGraph;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class NsdGraphService {

  @Setter
  private GraphExporter graphExporter;

  public Graph<ProfileVertex, String> buildGraph(List<Sapd> sapdList, NsDf nsDf, NsLevel nsLevel)
      throws InvalidNsd {
    Graph<ProfileVertex, String> g = new SimpleGraph<>(String.class);
    List<VnfProfileVertex> vnfPVertices = new ArrayList<>();
    List<PnfProfileVertex> pnfPVertices = new ArrayList<>();
    List<VirtualLinkProfileVertex> vlPVertices = new ArrayList<>();
    List<SapVertex> sapVertices = new ArrayList<>();

    //vertices
    for (VnfToLevelMapping vnfToLevelMapping : nsLevel.getVnfToLevelMapping()) {
      VnfProfileVertex v;
      try {
        v = new VnfProfileVertex(
            nsDf.getVnfProfile(vnfToLevelMapping.getVnfProfileId()));
      } catch (NotExistingEntityException e) {
        String message = MessageFormatter.arrayFormat(
            "vnfProfileId='{}' not found for nsDfId='{}' and nsLevelId='{}'.",
            new String[]{vnfToLevelMapping.getVnfProfileId(), nsDf.getNsDfId(),
                nsLevel.getNsLevelId()}).getMessage();
        log.error(message);
        throw new InvalidNsd(message);
      }
      // TODO handle the number of instances to build the graph
      vnfPVertices.add(v);
      g.addVertex(v);
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
        throw new InvalidNsd(message);
      }
      vlPVertices.add(v);
      g.addVertex(v);
    }
    for (Sapd s : sapdList) {
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
    return g;
  }

  public String export(Graph<ProfileVertex, String> graph) {
    return graphExporter.export(graph);
  }

}
