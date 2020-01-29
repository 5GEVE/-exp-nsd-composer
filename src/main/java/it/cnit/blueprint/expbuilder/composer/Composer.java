package it.cnit.blueprint.expbuilder.composer;

import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Composer {

  private static Logger LOG = LoggerFactory
      .getLogger(MethodHandles.lookup().lookupClass().getSimpleName());

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
    PASS
  }
}
