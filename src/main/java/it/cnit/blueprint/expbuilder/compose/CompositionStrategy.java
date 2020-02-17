package it.cnit.blueprint.expbuilder.compose;

import it.cnit.blueprint.expbuilder.nsdgraph.ProfileVertex;
import it.cnit.blueprint.expbuilder.rest.CtxComposeInfo;
import it.cnit.blueprint.expbuilder.rest.InvalidCtxComposeInfo;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Nsd;
import org.jgrapht.Graph;

public interface CompositionStrategy {

  void compose(Nsd nsd, String nsDfId, String nsLevelId, CtxComposeInfo composeInfo,
      Graph<ProfileVertex, String> graph) throws InvalidCtxComposeInfo;

}
