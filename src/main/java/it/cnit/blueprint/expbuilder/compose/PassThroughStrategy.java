package it.cnit.blueprint.expbuilder.compose;

import it.cnit.blueprint.expbuilder.nsdgraph.ProfileVertex;
import it.cnit.blueprint.expbuilder.rest.Composer.DfIlKey;
import it.cnit.blueprint.expbuilder.rest.CtxComposeInfo;
import it.cnit.blueprint.expbuilder.rest.InvalidCtxComposeInfo;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Nsd;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.jgrapht.Graph;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Qualifier("passthrough")
@Slf4j
public class PassThroughStrategy implements CompositionStrategy{

  @Override
  public void compose(Nsd nsd, Map<DfIlKey, Graph<ProfileVertex, String>> graphMap,
      CtxComposeInfo ctxR) throws InvalidCtxComposeInfo {

  }
}
