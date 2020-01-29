package it.cnit.blueprint.expbuilder.composer;

import it.cnit.blueprint.expbuilder.rest.CtxComposeResource;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Nsd;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Composer {

  private static Logger LOG = LoggerFactory
      .getLogger(MethodHandles.lookup().lookupClass().getSimpleName());

  private Nsd serviceNsd;
  private CtxCompose[] ctxComposes;

  public Composer(Nsd serviceNsd, CtxComposeResource[] ctxComposeResources) {
    this.serviceNsd = serviceNsd;
    this.ctxComposes = new CtxCompose[ctxComposeResources.length];
    for (int i = 0; i < ctxComposeResources.length; i++) {
      if (ctxComposeResources[i].getStrat() == CompositionStrat.CONNECT) {
        ctxComposes[i] = new CtxComposeConnect(
            ctxComposeResources[i].getNsd(), ctxComposeResources[i].getVirtualLinkIds());
      } else if (ctxComposeResources[i].getStrat() == CompositionStrat.PASSTHROUG) {
        ctxComposes[i] = new CtxComposePassthrough(
            ctxComposeResources[i].getNsd(), ctxComposeResources[i].getSapId());
      }
    }
  }

  public Nsd composeExperiment() {
    // TODO
    return null;
  }

  //  public static NsdGraph buildExperiment(Nsd service, Nsd[] contexts, )
//      throws NotExistingEntityException {
//    NsdGraph expNsdGraph = new NsdGraph(verticalNsdGraph.getNsd(), "nsDfId", "nsLevelId");
//    for (NsdGraph c : contextNsdGraphs) {
//      // TODO compositionStrategy should depend on the context.
//      if (strat == CompositionStrat.CONNECT) {
//        for (VnfProfileVertex vnfP : c.getVnfPVertices()) {
//          // TODO ask user to select a VitualLinkProfileVertex
//          VirtualLinkProfileVertex vlP;
//          if (vnfP.getId().contains("Src")) {
//            vlP = UserMock.getVLPVertex1(expNsdGraph);
//          } else { //if (vnfP.getProfileId().contains("Dst")) {
//            vlP = UserMock.getVLPVertex2(expNsdGraph);
//          }
//          expNsdGraph.addVnfProfileVertex(vnfP, vlP);
//        }
//      } else if (strat == CompositionStrat.PASS) {
//        boolean first = true;
//        for (VnfProfileVertex vnfP : c.getVnfPVertices()) {
//          // TODO ask user to select an edge
//          String e = "";
//          if (first) {
//            e = UserMock.getEdge1(expNsdGraph);
//            first = false;
//          } else {
//            e = UserMock.getEdge2(expNsdGraph);
//          }
//          expNsdGraph.addVnfProfileVertex(vnfP, e);
//        }
//      } else {
//        throw new UnsupportedOperationException("Not Implemented yet.");
//      }
//    }
//    return expNsdGraph;
//  }

  public enum CompositionStrat {
    CONNECT,
    PASSTHROUG
  }
}
