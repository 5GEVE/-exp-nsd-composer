package it.cnit.blueprint.expbuilder.rest;

import it.cnit.blueprint.expbuilder.compose.CompositionStrategy;
import it.cnit.blueprint.expbuilder.nsdgraph.NsdGraphService;
import it.cnit.blueprint.expbuilder.nsdgraph.NsdGraphService.DfIlKey;
import it.cnit.blueprint.expbuilder.nsdgraph.ProfileVertex;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Nsd;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.jgrapht.Graph;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

@Service
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
@Slf4j
public class Composer {
  //TODO rename to NsdComposer

  private NsdGraphService nsdGraphService;

  @Qualifier("connect")
  private CompositionStrategy connectStrategy;

  @Qualifier("passthrough")
  private CompositionStrategy passThroughStrategy;

  public enum CompositionStrat {
    CONNECT,
    PASSTHROUGH
  }

  public Composer(NsdGraphService nsdGraphService,
      CompositionStrategy connectStrategy,
      CompositionStrategy passThroughStrategy) {
    this.nsdGraphService = nsdGraphService;
    this.connectStrategy = connectStrategy;
    this.passThroughStrategy = passThroughStrategy;
  }


  public void composeWith(Nsd nsd, CtxComposeInfo[] ctxRArray) throws InvalidCtxComposeInfo {
    Map<DfIlKey, Graph<ProfileVertex, String>> graphMap = nsdGraphService.buildGraphs(nsd);
    for (CtxComposeInfo ctxR : ctxRArray) {
      for (Map.Entry<DfIlKey, Graph<ProfileVertex, String>> entry : graphMap.entrySet()) {
        log.debug("Graph export before:\n{}", nsdGraphService.export(entry.getValue()));
        if (ctxR.getStrat() == CompositionStrat.CONNECT) {
          connectStrategy.compose(nsd, entry.getKey().getNsDfId(), entry.getKey().getNsIlId(),
              ctxR, entry.getValue());
        } else if (ctxR.getStrat() == CompositionStrat.PASSTHROUGH) {
          passThroughStrategy.compose(nsd, entry.getKey().getNsDfId(), entry.getKey().getNsIlId(),
              ctxR, entry.getValue());
        } else {
          throw new NotImplementedException(
              String.format("Composition strategy %s not implemented", ctxR.getStrat()));
        }
        log.debug("Graph export after:\n{}", nsdGraphService.export(entry.getValue()));
      }
    }
  }

}
